package com.dofusretro.pricetracker.protocol;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Binary data reader for Dofus Retro protocol.
 * <p>
 * This class provides utilities for reading binary protocol data using
 * the Dofus encoding scheme, including variable-length integers (VarInt),
 * strings, and primitive types.
 * </p>
 * <p>
 * Based on the Python LaBot implementation:
 * https://github.com/louisabraham/LaBot/blob/master/labot/data/binrw.py
 * </p>
 *
 * @author AGENT-PROTOCOL
 * @version 1.0
 * @since 2025-11-09
 */
public class BinaryReader {

    private final ByteBuffer buffer;

    /**
     * Creates a new BinaryReader from raw byte array.
     *
     * @param data the raw byte data to read
     */
    public BinaryReader(byte[] data) {
        this.buffer = ByteBuffer.wrap(data);
        this.buffer.order(ByteOrder.BIG_ENDIAN); // Network byte order
    }

    /**
     * Creates a BinaryReader from an existing ByteBuffer.
     *
     * @param buffer the ByteBuffer to read from
     */
    public BinaryReader(ByteBuffer buffer) {
        this.buffer = buffer;
        this.buffer.order(ByteOrder.BIG_ENDIAN);
    }

    /**
     * Read a variable-length integer (VarInt).
     * <p>
     * Dofus uses a variable-length encoding where each byte contributes 7 bits
     * of data and 1 continuation bit. This is similar to Protocol Buffers encoding.
     * </p>
     * <p>
     * Algorithm:
     * <pre>
     * Each byte:
     *   Bit 7: Continuation bit (1 = more bytes, 0 = last byte)
     *   Bits 0-6: Data bits (7 bits per byte)
     *
     * Example (300):
     *   Binary: 0000 0001 0010 1100
     *   Encoded: 0xAC 0x02
     *     Byte 1: 1010 1100 (continue=1, data=0101100=44)
     *     Byte 2: 0000 0010 (continue=0, data=0000010=2)
     *   Result: 44 + (2 << 7) = 44 + 256 = 300
     * </pre>
     *
     * @return the decoded integer value
     * @throws BufferUnderflowException if not enough data is available
     */
    public int readVarInt() {
        int result = 0;
        int shift = 0;
        byte b;

        do {
            if (!buffer.hasRemaining()) {
                throw new BufferUnderflowException();
            }

            b = buffer.get();
            result |= (b & 0x7F) << shift;
            shift += 7;

            if (shift >= 32) {
                throw new IllegalStateException("VarInt too large (>32 bits)");
            }
        } while ((b & 0x80) != 0);

        return result;
    }

    /**
     * Read a variable-length long (VarLong).
     * <p>
     * Same encoding as VarInt but supports up to 64 bits.
     * Used for large values like prices in kamas.
     * </p>
     *
     * @return the decoded long value
     * @throws BufferUnderflowException if not enough data is available
     */
    public long readVarLong() {
        long result = 0;
        int shift = 0;
        byte b;

        do {
            if (!buffer.hasRemaining()) {
                throw new BufferUnderflowException();
            }

            b = buffer.get();
            result |= (long) (b & 0x7F) << shift;
            shift += 7;

            if (shift >= 64) {
                throw new IllegalStateException("VarLong too large (>64 bits)");
            }
        } while ((b & 0x80) != 0);

        return result;
    }

    /**
     * Read a variable-length short (VarShort).
     * <p>
     * Same encoding as VarInt but limited to 16 bits.
     * </p>
     *
     * @return the decoded short value (as int)
     * @throws BufferUnderflowException if not enough data is available
     */
    public int readVarShort() {
        int result = 0;
        int shift = 0;
        byte b;

        do {
            if (!buffer.hasRemaining()) {
                throw new BufferUnderflowException();
            }

            b = buffer.get();
            result |= (b & 0x7F) << shift;
            shift += 7;

            if (shift >= 16) {
                throw new IllegalStateException("VarShort too large (>16 bits)");
            }
        } while ((b & 0x80) != 0);

        return result;
    }

    /**
     * Read an unsigned short (2 bytes).
     *
     * @return the unsigned short value (as int, 0-65535)
     * @throws BufferUnderflowException if not enough data is available
     */
    public int readUnsignedShort() {
        return buffer.getShort() & 0xFFFF;
    }

    /**
     * Read a signed short (2 bytes).
     *
     * @return the signed short value
     * @throws BufferUnderflowException if not enough data is available
     */
    public short readShort() {
        return buffer.getShort();
    }

    /**
     * Read an unsigned int (4 bytes).
     *
     * @return the unsigned int value (as long, 0-4294967295)
     * @throws BufferUnderflowException if not enough data is available
     */
    public long readUnsignedInt() {
        return buffer.getInt() & 0xFFFFFFFFL;
    }

    /**
     * Read a signed int (4 bytes).
     *
     * @return the signed int value
     * @throws BufferUnderflowException if not enough data is available
     */
    public int readInt() {
        return buffer.getInt();
    }

    /**
     * Read a long (8 bytes).
     *
     * @return the long value
     * @throws BufferUnderflowException if not enough data is available
     */
    public long readLong() {
        return buffer.getLong();
    }

    /**
     * Read an unsigned byte.
     *
     * @return the unsigned byte value (as int, 0-255)
     * @throws BufferUnderflowException if not enough data is available
     */
    public int readUnsignedByte() {
        return buffer.get() & 0xFF;
    }

    /**
     * Read a signed byte.
     *
     * @return the signed byte value
     * @throws BufferUnderflowException if not enough data is available
     */
    public byte readByte() {
        return buffer.get();
    }

    /**
     * Read a boolean (1 byte, 0=false, 1=true).
     *
     * @return the boolean value
     * @throws BufferUnderflowException if not enough data is available
     */
    public boolean readBoolean() {
        return buffer.get() != 0;
    }

    /**
     * Read a float (4 bytes, IEEE 754).
     *
     * @return the float value
     * @throws BufferUnderflowException if not enough data is available
     */
    public float readFloat() {
        return buffer.getFloat();
    }

    /**
     * Read a double (8 bytes, IEEE 754).
     *
     * @return the double value
     * @throws BufferUnderflowException if not enough data is available
     */
    public double readDouble() {
        return buffer.getDouble();
    }

    /**
     * Read a UTF-8 string.
     * <p>
     * Dofus string format:
     * <pre>
     * [length: unsigned short (2 bytes)] [utf-8 bytes]
     * </pre>
     * </p>
     *
     * @return the decoded string
     * @throws BufferUnderflowException if not enough data is available
     */
    public String readUTF() {
        int length = readUnsignedShort();
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Read a UTF-8 string with VarInt length prefix.
     * <p>
     * Alternative string format used in some messages:
     * <pre>
     * [length: VarInt] [utf-8 bytes]
     * </pre>
     * </p>
     *
     * @return the decoded string
     * @throws BufferUnderflowException if not enough data is available
     */
    public String readUTFVarInt() {
        int length = readVarInt();
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Read a byte array with VarInt length prefix.
     * <p>
     * Format: [length: VarInt] [bytes]
     * </p>
     *
     * @return the byte array
     * @throws BufferUnderflowException if not enough data is available
     */
    public byte[] readByteArray() {
        int length = readVarInt();
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return bytes;
    }

    /**
     * Read a fixed number of bytes.
     *
     * @param length the number of bytes to read
     * @return the byte array
     * @throws BufferUnderflowException if not enough data is available
     */
    public byte[] readBytes(int length) {
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return bytes;
    }

    /**
     * Get the current read position in the buffer.
     *
     * @return the current position (0-based index)
     */
    public int position() {
        return buffer.position();
    }

    /**
     * Set the read position in the buffer.
     *
     * @param newPosition the new position
     * @throws IllegalArgumentException if position is invalid
     */
    public void position(int newPosition) {
        buffer.position(newPosition);
    }

    /**
     * Get the number of remaining bytes in the buffer.
     *
     * @return the number of unread bytes
     */
    public int remaining() {
        return buffer.remaining();
    }

    /**
     * Check if there are bytes remaining to read.
     *
     * @return true if more bytes are available
     */
    public boolean hasRemaining() {
        return buffer.hasRemaining();
    }

    /**
     * Skip a number of bytes.
     *
     * @param bytes the number of bytes to skip
     * @throws IllegalArgumentException if skipping would exceed buffer limits
     */
    public void skip(int bytes) {
        buffer.position(buffer.position() + bytes);
    }

    /**
     * Reset the read position to the beginning.
     */
    public void reset() {
        buffer.position(0);
    }

    /**
     * Get the total capacity of the buffer.
     *
     * @return the buffer capacity in bytes
     */
    public int capacity() {
        return buffer.capacity();
    }

    /**
     * Get a hex dump of remaining data (for debugging).
     * <p>
     * Example output: "A3 02 FF 00 ..."
     * </p>
     *
     * @return hex string representation of remaining bytes
     */
    public String remainingHex() {
        int pos = buffer.position();
        StringBuilder sb = new StringBuilder();

        while (buffer.hasRemaining()) {
            sb.append(String.format("%02X ", buffer.get()));
        }

        buffer.position(pos); // Reset position
        return sb.toString().trim();
    }

    /**
     * Get a hex dump of all data (for debugging).
     *
     * @return hex string representation of all bytes
     */
    public String toHex() {
        int pos = buffer.position();
        buffer.position(0);
        String hex = remainingHex();
        buffer.position(pos);
        return hex;
    }

    @Override
    public String toString() {
        return String.format("BinaryReader[pos=%d, remaining=%d, capacity=%d]",
                position(), remaining(), capacity());
    }
}
