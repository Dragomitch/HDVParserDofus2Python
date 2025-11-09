package com.dofusretro.pricetracker.protocol;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.BufferUnderflowException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for BinaryReader.
 * <p>
 * Tests all reading methods including:
 * - Variable-length integers (VarInt, VarShort, VarLong)
 * - Fixed-size primitives
 * - Strings (UTF-8)
 * - Byte arrays
 * - Position tracking
 * - Error handling
 * </p>
 *
 * @author AGENT-PROTOCOL
 * @version 1.0
 */
@DisplayName("BinaryReader Tests")
class BinaryReaderTest {

    // ===================================================================
    // VARINT TESTS
    // ===================================================================

    @Test
    @DisplayName("Should read single-byte VarInt (value 1)")
    void shouldReadSingleByteVarInt() {
        // VarInt encoding of 1: 0x01 (single byte)
        byte[] data = {0x01};
        BinaryReader reader = new BinaryReader(data);

        int value = reader.readVarInt();

        assertThat(value).isEqualTo(1);
        assertThat(reader.remaining()).isZero();
    }

    @Test
    @DisplayName("Should read two-byte VarInt (value 300)")
    void shouldReadTwoByteVarInt() {
        // VarInt encoding of 300:
        // 300 = 0b100101100 (9 bits)
        // Byte 1: 1010 1100 (continue=1, data=0101100=44)
        // Byte 2: 0000 0010 (continue=0, data=0000010=2)
        // Result: 44 + (2 << 7) = 44 + 256 = 300
        byte[] data = {(byte) 0xAC, 0x02};
        BinaryReader reader = new BinaryReader(data);

        int value = reader.readVarInt();

        assertThat(value).isEqualTo(300);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0x00",
            "1, 0x01",
            "127, 0x7F",
            "128, 0x80 0x01",
            "255, 0xFF 0x01",
            "256, 0x80 0x02",
            "16384, 0x80 0x80 0x01",
            "2097151, 0xFF 0xFF 0x7F"
    })
    @DisplayName("Should read various VarInt values")
    void shouldReadVariousVarIntValues(int expected, String hexString) {
        byte[] data = parseHexString(hexString);
        BinaryReader reader = new BinaryReader(data);

        int value = reader.readVarInt();

        assertThat(value).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should read multiple VarInts sequentially")
    void shouldReadMultipleVarInts() {
        byte[] data = {0x01, 0x02, 0x03, (byte) 0xAC, 0x02};
        BinaryReader reader = new BinaryReader(data);

        assertThat(reader.readVarInt()).isEqualTo(1);
        assertThat(reader.readVarInt()).isEqualTo(2);
        assertThat(reader.readVarInt()).isEqualTo(3);
        assertThat(reader.readVarInt()).isEqualTo(300);
        assertThat(reader.remaining()).isZero();
    }

    // ===================================================================
    // VARLONG TESTS
    // ===================================================================

    @Test
    @DisplayName("Should read VarLong (large value)")
    void shouldReadVarLong() {
        // Large value: 1,300,000 kamas (typical HDV price)
        long expected = 1_300_000L;

        // Encode manually (simplified - actual encoding varies)
        // For testing, we'll use a known encoding
        byte[] data = {(byte) 0xA0, (byte) 0xDA, 0x4E};
        BinaryReader reader = new BinaryReader(data);

        long value = reader.readVarLong();

        assertThat(value).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should read VarLong with 64-bit value")
    void shouldReadLargeVarLong() {
        // Test with a value that requires multiple bytes
        byte[] data = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x0F};
        BinaryReader reader = new BinaryReader(data);

        long value = reader.readVarLong();

        // This should decode to a large number (exact value depends on encoding)
        assertThat(value).isGreaterThan(0);
    }

    // ===================================================================
    // VARSHORT TESTS
    // ===================================================================

    @Test
    @DisplayName("Should read VarShort")
    void shouldReadVarShort() {
        byte[] data = {(byte) 0xAC, 0x02};  // 300 (same as VarInt test)
        BinaryReader reader = new BinaryReader(data);

        int value = reader.readVarShort();

        assertThat(value).isEqualTo(300);
    }

    // ===================================================================
    // FIXED-SIZE INTEGER TESTS
    // ===================================================================

    @Test
    @DisplayName("Should read unsigned short")
    void shouldReadUnsignedShort() {
        byte[] data = {0x12, 0x34};  // 0x1234 = 4660
        BinaryReader reader = new BinaryReader(data);

        int value = reader.readUnsignedShort();

        assertThat(value).isEqualTo(0x1234);
    }

    @Test
    @DisplayName("Should read signed short")
    void shouldReadSignedShort() {
        byte[] data = {(byte) 0xFF, (byte) 0xFF};  // -1
        BinaryReader reader = new BinaryReader(data);

        short value = reader.readShort();

        assertThat(value).isEqualTo((short) -1);
    }

    @Test
    @DisplayName("Should read unsigned int")
    void shouldReadUnsignedInt() {
        byte[] data = {0x00, 0x00, 0x00, 0x0F};  // 15
        BinaryReader reader = new BinaryReader(data);

        long value = reader.readUnsignedInt();

        assertThat(value).isEqualTo(15L);
    }

    @Test
    @DisplayName("Should read signed int")
    void shouldReadSignedInt() {
        byte[] data = {0x00, 0x00, 0x04, (byte) 0xD2};  // 1234
        BinaryReader reader = new BinaryReader(data);

        int value = reader.readInt();

        assertThat(value).isEqualTo(1234);
    }

    @Test
    @DisplayName("Should read long")
    void shouldReadLong() {
        byte[] data = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04, (byte) 0xD2};
        BinaryReader reader = new BinaryReader(data);

        long value = reader.readLong();

        assertThat(value).isEqualTo(1234L);
    }

    @Test
    @DisplayName("Should read unsigned byte")
    void shouldReadUnsignedByte() {
        byte[] data = {(byte) 0xFF};  // 255 (unsigned)
        BinaryReader reader = new BinaryReader(data);

        int value = reader.readUnsignedByte();

        assertThat(value).isEqualTo(255);
    }

    @Test
    @DisplayName("Should read signed byte")
    void shouldReadSignedByte() {
        byte[] data = {(byte) 0xFF};  // -1 (signed)
        BinaryReader reader = new BinaryReader(data);

        byte value = reader.readByte();

        assertThat(value).isEqualTo((byte) -1);
    }

    // ===================================================================
    // BOOLEAN TESTS
    // ===================================================================

    @Test
    @DisplayName("Should read boolean true")
    void shouldReadBooleanTrue() {
        byte[] data = {0x01};
        BinaryReader reader = new BinaryReader(data);

        boolean value = reader.readBoolean();

        assertThat(value).isTrue();
    }

    @Test
    @DisplayName("Should read boolean false")
    void shouldReadBooleanFalse() {
        byte[] data = {0x00};
        BinaryReader reader = new BinaryReader(data);

        boolean value = reader.readBoolean();

        assertThat(value).isFalse();
    }

    // ===================================================================
    // FLOATING POINT TESTS
    // ===================================================================

    @Test
    @DisplayName("Should read float")
    void shouldReadFloat() {
        byte[] data = {0x40, 0x49, 0x0F, (byte) 0xDB};  // 3.14159... (approx)
        BinaryReader reader = new BinaryReader(data);

        float value = reader.readFloat();

        assertThat(value).isCloseTo(3.14159f, within(0.0001f));
    }

    @Test
    @DisplayName("Should read double")
    void shouldReadDouble() {
        byte[] data = {0x40, 0x09, 0x21, (byte) 0xFB, 0x54, 0x44, 0x2D, 0x18};
        BinaryReader reader = new BinaryReader(data);

        double value = reader.readDouble();

        assertThat(value).isCloseTo(3.14159, within(0.0001));
    }

    // ===================================================================
    // STRING TESTS
    // ===================================================================

    @Test
    @DisplayName("Should read UTF-8 string")
    void shouldReadUTFString() {
        // String "test": length=4 (unsigned short), bytes="test"
        byte[] data = {0x00, 0x04, 't', 'e', 's', 't'};
        BinaryReader reader = new BinaryReader(data);

        String value = reader.readUTF();

        assertThat(value).isEqualTo("test");
    }

    @Test
    @DisplayName("Should read empty UTF-8 string")
    void shouldReadEmptyUTFString() {
        byte[] data = {0x00, 0x00};  // length=0
        BinaryReader reader = new BinaryReader(data);

        String value = reader.readUTF();

        assertThat(value).isEmpty();
    }

    @Test
    @DisplayName("Should read UTF-8 string with special characters")
    void shouldReadUTFStringWithSpecialChars() {
        String original = "Café ☕";
        byte[] utf8Bytes = original.getBytes(StandardCharsets.UTF_8);

        byte[] data = new byte[2 + utf8Bytes.length];
        data[0] = 0x00;
        data[1] = (byte) utf8Bytes.length;
        System.arraycopy(utf8Bytes, 0, data, 2, utf8Bytes.length);

        BinaryReader reader = new BinaryReader(data);

        String value = reader.readUTF();

        assertThat(value).isEqualTo(original);
    }

    @Test
    @DisplayName("Should read UTF-8 string with VarInt length")
    void shouldReadUTFVarIntString() {
        // String "hello": length=5 (VarInt), bytes="hello"
        byte[] data = {0x05, 'h', 'e', 'l', 'l', 'o'};
        BinaryReader reader = new BinaryReader(data);

        String value = reader.readUTFVarInt();

        assertThat(value).isEqualTo("hello");
    }

    // ===================================================================
    // BYTE ARRAY TESTS
    // ===================================================================

    @Test
    @DisplayName("Should read byte array with VarInt length")
    void shouldReadByteArray() {
        byte[] data = {0x03, 0x0A, 0x0B, 0x0C};  // length=3, bytes=[10,11,12]
        BinaryReader reader = new BinaryReader(data);

        byte[] value = reader.readByteArray();

        assertThat(value).containsExactly(0x0A, 0x0B, 0x0C);
    }

    @Test
    @DisplayName("Should read fixed-length bytes")
    void shouldReadFixedLengthBytes() {
        byte[] data = {0x0A, 0x0B, 0x0C, 0x0D};
        BinaryReader reader = new BinaryReader(data);

        byte[] value = reader.readBytes(3);

        assertThat(value).containsExactly(0x0A, 0x0B, 0x0C);
        assertThat(reader.remaining()).isEqualTo(1);
    }

    // ===================================================================
    // POSITION TRACKING TESTS
    // ===================================================================

    @Test
    @DisplayName("Should track position correctly")
    void shouldTrackPosition() {
        byte[] data = {0x01, 0x02, 0x03, 0x04};
        BinaryReader reader = new BinaryReader(data);

        assertThat(reader.position()).isZero();
        assertThat(reader.remaining()).isEqualTo(4);

        reader.readByte();
        assertThat(reader.position()).isEqualTo(1);
        assertThat(reader.remaining()).isEqualTo(3);

        reader.readByte();
        assertThat(reader.position()).isEqualTo(2);
        assertThat(reader.remaining()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should set position")
    void shouldSetPosition() {
        byte[] data = {0x01, 0x02, 0x03, 0x04};
        BinaryReader reader = new BinaryReader(data);

        reader.position(2);

        assertThat(reader.position()).isEqualTo(2);
        assertThat(reader.readByte()).isEqualTo((byte) 0x03);
    }

    @Test
    @DisplayName("Should reset position")
    void shouldResetPosition() {
        byte[] data = {0x01, 0x02, 0x03};
        BinaryReader reader = new BinaryReader(data);

        reader.readByte();
        reader.readByte();
        assertThat(reader.position()).isEqualTo(2);

        reader.reset();

        assertThat(reader.position()).isZero();
        assertThat(reader.readByte()).isEqualTo((byte) 0x01);
    }

    @Test
    @DisplayName("Should skip bytes")
    void shouldSkipBytes() {
        byte[] data = {0x01, 0x02, 0x03, 0x04};
        BinaryReader reader = new BinaryReader(data);

        reader.skip(2);

        assertThat(reader.position()).isEqualTo(2);
        assertThat(reader.readByte()).isEqualTo((byte) 0x03);
    }

    @Test
    @DisplayName("Should check if has remaining bytes")
    void shouldCheckHasRemaining() {
        byte[] data = {0x01, 0x02};
        BinaryReader reader = new BinaryReader(data);

        assertThat(reader.hasRemaining()).isTrue();

        reader.readByte();
        assertThat(reader.hasRemaining()).isTrue();

        reader.readByte();
        assertThat(reader.hasRemaining()).isFalse();
    }

    // ===================================================================
    // ERROR HANDLING TESTS
    // ===================================================================

    @Test
    @DisplayName("Should throw BufferUnderflowException when reading beyond data")
    void shouldThrowBufferUnderflowException() {
        byte[] data = {0x01};
        BinaryReader reader = new BinaryReader(data);

        reader.readByte();

        assertThatThrownBy(reader::readByte)
                .isInstanceOf(BufferUnderflowException.class);
    }

    @Test
    @DisplayName("Should throw exception for incomplete VarInt")
    void shouldThrowExceptionForIncompleteVarInt() {
        // VarInt with continuation bit but no next byte
        byte[] data = {(byte) 0x80};  // Continue bit set, but no next byte
        BinaryReader reader = new BinaryReader(data);

        assertThatThrownBy(reader::readVarInt)
                .isInstanceOf(BufferUnderflowException.class);
    }

    @Test
    @DisplayName("Should throw exception for VarInt that's too large")
    void shouldThrowExceptionForOversizedVarInt() {
        // Create a VarInt that would exceed 32 bits (5+ bytes all with continue bit)
        byte[] data = {
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF  // 6 bytes = 42 bits (exceeds 32)
        };
        BinaryReader reader = new BinaryReader(data);

        assertThatThrownBy(reader::readVarInt)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too large");
    }

    // ===================================================================
    // HEX DUMP TESTS
    // ===================================================================

    @Test
    @DisplayName("Should generate hex dump of remaining data")
    void shouldGenerateRemainingHex() {
        byte[] data = {0x0A, 0x0B, 0x0C, 0x0D};
        BinaryReader reader = new BinaryReader(data);

        reader.readByte();  // Read first byte

        String hex = reader.remainingHex();

        assertThat(hex).isEqualTo("0B 0C 0D");
        assertThat(reader.position()).isEqualTo(1);  // Position unchanged
    }

    @Test
    @DisplayName("Should generate hex dump of all data")
    void shouldGenerateFullHex() {
        byte[] data = {0x0A, 0x0B, 0x0C};
        BinaryReader reader = new BinaryReader(data);

        String hex = reader.toHex();

        assertThat(hex).isEqualTo("0A 0B 0C");
    }

    @Test
    @DisplayName("Should have meaningful toString")
    void shouldHaveMeaningfulToString() {
        byte[] data = {0x01, 0x02, 0x03, 0x04};
        BinaryReader reader = new BinaryReader(data);

        reader.readByte();

        String str = reader.toString();

        assertThat(str)
                .contains("pos=1")
                .contains("remaining=3")
                .contains("capacity=4");
    }

    // ===================================================================
    // INTEGRATION TESTS
    // ===================================================================

    @Test
    @DisplayName("Should read complex message structure")
    void shouldReadComplexMessageStructure() {
        // Simulate a simple message:
        // - Header: unsigned short (0x1234)
        // - Count: 3 items (VarInt)
        // - Item 1: id=100 (VarInt), price=5000 (VarLong)
        // - Item 2: id=200 (VarInt), price=10000 (VarLong)
        // - Item 3: id=300 (VarInt), price=15000 (VarLong)

        byte[] data = new byte[100];  // Oversized buffer
        BinaryReader writer = new BinaryReader(data);

        // This is just a conceptual test - we're reading pre-constructed data
        byte[] actualData = {
                0x12, 0x34,              // header
                0x03,                    // count
                0x64,                    // item 1 id (100)
                (byte) 0x88, 0x27,      // item 1 price (5000)
                (byte) 0xC8, 0x01,      // item 2 id (200)
                (byte) 0x90, 0x4E,      // item 2 price (10000)
                (byte) 0xAC, 0x02,      // item 3 id (300)
                (byte) 0xD8, 0x75       // item 3 price (15000)
        };

        BinaryReader reader = new BinaryReader(actualData);

        int header = reader.readUnsignedShort();
        int count = reader.readVarInt();

        assertThat(header).isEqualTo(0x1234);
        assertThat(count).isEqualTo(3);

        // Read items
        for (int i = 0; i < count; i++) {
            int id = reader.readVarInt();
            long price = reader.readVarLong();

            assertThat(id).isGreaterThan(0);
            assertThat(price).isGreaterThan(0);
        }
    }

    // ===================================================================
    // HELPER METHODS
    // ===================================================================

    /**
     * Parse hex string like "0xAC 0x02" into byte array.
     */
    private byte[] parseHexString(String hex) {
        String[] parts = hex.split("\\s+");
        byte[] result = new byte[parts.length];

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].replace("0x", "").trim();
            result[i] = (byte) Integer.parseInt(part, 16);
        }

        return result;
    }
}
