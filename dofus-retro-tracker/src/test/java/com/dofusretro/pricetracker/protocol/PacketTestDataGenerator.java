package com.dofusretro.pricetracker.protocol;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Utility for generating test packet data.
 * <p>
 * This class creates binary test packets that can be used in unit tests.
 * Run this class to generate test fixture files.
 * </p>
 *
 * @author AGENT-PROTOCOL
 * @version 1.0
 */
public class PacketTestDataGenerator {

    private static final String OUTPUT_DIR = "src/test/resources/packets/";

    public static void main(String[] args) throws IOException {
        System.out.println("Generating test packet data...");

        generateHdvPriceWheatPacket();
        generateHdvPriceMultiplePacket();
        generateHdvCategoryPacket();

        System.out.println("Test packet generation complete!");
    }

    /**
     * Generate HDV price packet for Wheat.
     */
    private static void generateHdvPriceWheatPacket() throws IOException {
        byte[] packet = createHdvPricePacket(
                new ItemData(289, 48, new long[]{15000, 140000, 1300000})  // Wheat
        );

        String filename = OUTPUT_DIR + "hdv-price-wheat.bin";
        Files.write(Paths.get(filename), packet);

        System.out.println("Created: " + filename);
        System.out.println("  Size: " + packet.length + " bytes");
        System.out.println("  Hex: " + bytesToHex(packet));
    }

    /**
     * Generate HDV price packet with multiple items.
     */
    private static void generateHdvPriceMultiplePacket() throws IOException {
        byte[] packet = createHdvPricePacket(
                new ItemData(289, 48, new long[]{15000, 140000, 1300000}),  // Wheat
                new ItemData(290, 48, new long[]{20000, 180000, 1600000}),  // Barley
                new ItemData(291, 48, new long[]{18000, 160000, 1400000})   // Oats
        );

        String filename = OUTPUT_DIR + "hdv-price-multiple.bin";
        Files.write(Paths.get(filename), packet);

        System.out.println("Created: " + filename);
        System.out.println("  Size: " + packet.length + " bytes");
    }

    /**
     * Generate HDV category packet.
     */
    private static void generateHdvCategoryPacket() throws IOException {
        byte[] packet = createHdvCategoryPacket(48);  // Cereals

        String filename = OUTPUT_DIR + "hdv-category.bin";
        Files.write(Paths.get(filename), packet);

        System.out.println("Created: " + filename);
        System.out.println("  Size: " + packet.length + " bytes");
        System.out.println("  Hex: " + bytesToHex(packet));
    }

    // ===================================================================
    // PACKET CREATION METHODS
    // ===================================================================

    private static byte[] createHdvPricePacket(ItemData... items) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        int messageId = MessageDefinitions.MSG_EXCHANGE_TYPES_ITEMS_EXCHANGER_DESC;

        // Build payload first to know size
        ByteArrayOutputStream payloadStream = new ByteArrayOutputStream();
        DataOutputStream payloadDos = new DataOutputStream(payloadStream);

        // Item count
        writeVarInt(payloadDos, items.length);

        // Each item
        for (ItemData item : items) {
            writeVarInt(payloadDos, item.gid);
            writeVarInt(payloadDos, item.type);
            writeVarInt(payloadDos, item.prices.length);

            for (long price : item.prices) {
                writeVarLong(payloadDos, price);
            }
        }

        byte[] payload = payloadStream.toByteArray();

        // Write header
        int lengthBytes = calculateLengthBytes(payload.length);
        int header = (messageId << 2) | lengthBytes;
        dos.writeShort(header);

        // Write length
        writeLengthField(dos, payload.length, lengthBytes);

        // Write payload
        dos.write(payload);

        return baos.toByteArray();
    }

    private static byte[] createHdvCategoryPacket(int categoryId) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        int messageId = MessageDefinitions.MSG_EXCHANGE_TYPES_EXCHANGER_DESC;

        // Payload
        ByteArrayOutputStream payloadStream = new ByteArrayOutputStream();
        DataOutputStream payloadDos = new DataOutputStream(payloadStream);
        writeVarInt(payloadDos, categoryId);

        byte[] payload = payloadStream.toByteArray();

        // Header
        int lengthBytes = calculateLengthBytes(payload.length);
        int header = (messageId << 2) | lengthBytes;
        dos.writeShort(header);

        // Length
        writeLengthField(dos, payload.length, lengthBytes);

        // Payload
        dos.write(payload);

        return baos.toByteArray();
    }

    // ===================================================================
    // ENCODING UTILITIES
    // ===================================================================

    /**
     * Write VarInt using Dofus encoding.
     */
    private static void writeVarInt(DataOutputStream dos, int value) throws IOException {
        while ((value & ~0x7F) != 0) {
            dos.writeByte((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        dos.writeByte((byte) (value & 0x7F));
    }

    /**
     * Write VarLong using Dofus encoding.
     */
    private static void writeVarLong(DataOutputStream dos, long value) throws IOException {
        while ((value & ~0x7FL) != 0) {
            dos.writeByte((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        dos.writeByte((byte) (value & 0x7F));
    }

    /**
     * Calculate how many bytes needed for length encoding.
     */
    private static int calculateLengthBytes(int length) {
        if (length == 0) return 0;
        if (length <= 255) return 1;
        if (length <= 65535) return 2;
        return 3;
    }

    /**
     * Write length field based on encoding.
     */
    private static void writeLengthField(DataOutputStream dos, int length, int lengthBytes) throws IOException {
        switch (lengthBytes) {
            case 0 -> {
                // No length field
            }
            case 1 -> dos.writeByte(length);
            case 2 -> dos.writeShort(length);
            case 3 -> {
                dos.writeByte((length >> 16) & 0xFF);
                dos.writeShort(length & 0xFFFF);
            }
            default -> throw new IllegalArgumentException("Invalid lengthBytes: " + lengthBytes);
        }
    }

    /**
     * Convert bytes to hex string.
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    // ===================================================================
    // HELPER CLASSES
    // ===================================================================

    private record ItemData(int gid, int type, long[] prices) {
    }
}
