package com.dofusretro.pricetracker.service;

import com.dofusretro.pricetracker.protocol.*;
import com.dofusretro.pricetracker.protocol.MessageDefinitions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for DofusRetroProtocolParser.
 * <p>
 * Tests message parsing including:
 * - Header parsing
 * - HDV price messages
 * - HDV category messages
 * - Unknown messages
 * - Error handling
 * - Compressed messages (future)
 * </p>
 *
 * @author AGENT-PROTOCOL
 * @version 1.0
 */
@DisplayName("DofusRetroProtocolParser Tests")
class DofusRetroProtocolParserTest {

    private DofusRetroProtocolParser parser;

    @BeforeEach
    void setUp() {
        parser = new DofusRetroProtocolParser();
    }

    // ===================================================================
    // HDV PRICE MESSAGE TESTS
    // ===================================================================

    @Test
    @DisplayName("Should parse HDV price message with single item")
    void shouldParseHdvPriceMessageSingleItem() throws IOException {
        // Create test packet for ExchangeTypesItemsExchangerDescriptionForUserMessage
        byte[] packet = createHdvPricePacket(
                new ItemData(289, 48, new long[]{15000, 140000, 1300000})  // Wheat
        );

        ParsedMessage result = parser.parse(packet);

        assertThat(result).isNotNull();
        assertThat(result.isParseSuccess()).isTrue();
        assertThat(result.getType()).isEqualTo(MessageType.EXCHANGE_TYPES_ITEMS_EXCHANGER);
        assertThat(result.isHdvPriceMessage()).isTrue();

        ExchangeTypesItemsMessage hdvMsg = result.getPayloadAs(ExchangeTypesItemsMessage.class);
        assertThat(hdvMsg).isNotNull();
        assertThat(hdvMsg.itemTypeDescriptions()).hasSize(1);

        ItemTypeDescription item = hdvMsg.itemTypeDescriptions().get(0);
        assertThat(item.objectGid()).isEqualTo(289);
        assertThat(item.objectType()).isEqualTo(48);
        assertThat(item.prices()).containsExactly(15000L, 140000L, 1300000L);
    }

    @Test
    @DisplayName("Should parse HDV price message with multiple items")
    void shouldParseHdvPriceMessageMultipleItems() throws IOException {
        byte[] packet = createHdvPricePacket(
                new ItemData(289, 48, new long[]{15000, 140000, 1300000}),  // Wheat
                new ItemData(290, 48, new long[]{20000, 180000, 1600000}),  // Barley
                new ItemData(291, 48, new long[]{18000, 160000, 1400000})   // Oats
        );

        ParsedMessage result = parser.parse(packet);

        assertThat(result).isNotNull();
        assertThat(result.isParseSuccess()).isTrue();

        ExchangeTypesItemsMessage hdvMsg = result.getPayloadAs(ExchangeTypesItemsMessage.class);
        assertThat(hdvMsg.itemTypeDescriptions()).hasSize(3);

        // Verify each item
        assertThat(hdvMsg.itemTypeDescriptions().get(0).objectGid()).isEqualTo(289);
        assertThat(hdvMsg.itemTypeDescriptions().get(1).objectGid()).isEqualTo(290);
        assertThat(hdvMsg.itemTypeDescriptions().get(2).objectGid()).isEqualTo(291);
    }

    @Test
    @DisplayName("Should extract price data from HDV message")
    void shouldExtractPriceData() throws IOException {
        byte[] packet = createHdvPricePacket(
                new ItemData(289, 48, new long[]{15000, 140000, 1300000})
        );

        ParsedMessage result = parser.parse(packet);
        List<PriceData> prices = parser.extractPriceData(result);

        assertThat(prices).hasSize(3);

        // Verify quantity 1 price
        PriceData price1 = prices.get(0);
        assertThat(price1.itemGid()).isEqualTo(289);
        assertThat(price1.quantity()).isEqualTo(1);
        assertThat(price1.price()).isEqualTo(15000);

        // Verify quantity 10 price
        PriceData price10 = prices.get(1);
        assertThat(price10.quantity()).isEqualTo(10);
        assertThat(price10.price()).isEqualTo(140000);

        // Verify quantity 100 price
        PriceData price100 = prices.get(2);
        assertThat(price100.quantity()).isEqualTo(100);
        assertThat(price100.price()).isEqualTo(1300000);
    }

    @Test
    @DisplayName("Should handle items with zero prices (unavailable quantities)")
    void shouldHandleZeroPrices() throws IOException {
        // Item with only quantity 1 available, others at 0
        byte[] packet = createHdvPricePacket(
                new ItemData(289, 48, new long[]{15000, 0, 0})
        );

        ParsedMessage result = parser.parse(packet);
        List<PriceData> prices = parser.extractPriceData(result);

        // Should only have 1 price entry (zeros are filtered out)
        assertThat(prices).hasSize(1);
        assertThat(prices.get(0).quantity()).isEqualTo(1);
        assertThat(prices.get(0).price()).isEqualTo(15000);
    }

    @Test
    @DisplayName("Should calculate total price entries correctly")
    void shouldCalculateTotalPriceEntries() throws IOException {
        byte[] packet = createHdvPricePacket(
                new ItemData(289, 48, new long[]{15000, 140000, 1300000}),
                new ItemData(290, 48, new long[]{20000, 0, 0})  // Only 1 price
        );

        ParsedMessage result = parser.parse(packet);
        ExchangeTypesItemsMessage hdvMsg = result.getPayloadAs(ExchangeTypesItemsMessage.class);

        // 3 prices from first item + 1 price from second = 4 total
        assertThat(hdvMsg.getTotalPriceEntries()).isEqualTo(4);
    }

    // ===================================================================
    // HDV CATEGORY MESSAGE TESTS
    // ===================================================================

    @Test
    @DisplayName("Should parse HDV category message")
    void shouldParseHdvCategoryMessage() throws IOException {
        byte[] packet = createHdvCategoryPacket(48, "Cereals");

        ParsedMessage result = parser.parse(packet);

        assertThat(result).isNotNull();
        assertThat(result.isParseSuccess()).isTrue();
        assertThat(result.getType()).isEqualTo(MessageType.EXCHANGE_TYPES_EXCHANGER);
        assertThat(result.isHdvCategoryMessage()).isTrue();

        ExchangeTypesMessage categoryMsg = result.getPayloadAs(ExchangeTypesMessage.class);
        assertThat(categoryMsg.objectType()).isEqualTo(48);
    }

    // ===================================================================
    // PACKET DETECTION TESTS
    // ===================================================================

    @Test
    @DisplayName("Should detect HDV packets")
    void shouldDetectHdvPackets() throws IOException {
        byte[] hdvPacket = createHdvPricePacket(
                new ItemData(289, 48, new long[]{15000, 0, 0})
        );

        boolean isHdv = parser.isHdvPacket(hdvPacket);

        assertThat(isHdv).isTrue();
    }

    @Test
    @DisplayName("Should detect packets with price data")
    void shouldDetectPriceDataPackets() throws IOException {
        byte[] pricePacket = createHdvPricePacket(
                new ItemData(289, 48, new long[]{15000, 0, 0})
        );

        boolean hasPrice = parser.containsPriceData(pricePacket);

        assertThat(hasPrice).isTrue();
    }

    @Test
    @DisplayName("Should return false for non-HDV packets")
    void shouldReturnFalseForNonHdvPackets() throws IOException {
        byte[] nonHdvPacket = createUnknownPacket(999);

        boolean isHdv = parser.isHdvPacket(nonHdvPacket);

        assertThat(isHdv).isFalse();
    }

    // ===================================================================
    // ERROR HANDLING TESTS
    // ===================================================================

    @Test
    @DisplayName("Should handle null packet")
    void shouldHandleNullPacket() {
        ParsedMessage result = parser.parse(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle empty packet")
    void shouldHandleEmptyPacket() {
        ParsedMessage result = parser.parse(new byte[0]);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle packet too small")
    void shouldHandlePacketTooSmall() {
        byte[] tinyPacket = {0x01};  // Only 1 byte

        ParsedMessage result = parser.parse(tinyPacket);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle malformed packet")
    void shouldHandleMalformedPacket() {
        // Create a packet with invalid header/data
        byte[] malformed = {0x12, 0x34, 0x05};  // Header says 5 bytes payload, but none provided

        ParsedMessage result = parser.parse(malformed);

        // Should either return null or a failed parse result
        if (result != null) {
            assertThat(result.isParseSuccess()).isFalse();
            assertThat(result.getErrorMessage()).isNotNull();
        }
    }

    @Test
    @DisplayName("Should handle unknown message ID")
    void shouldHandleUnknownMessageId() throws IOException {
        byte[] unknownPacket = createUnknownPacket(9999);

        ParsedMessage result = parser.parse(unknownPacket);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(MessageType.UNKNOWN);
    }

    // ===================================================================
    // MESSAGE STATISTICS TESTS
    // ===================================================================

    @Test
    @DisplayName("Should generate message statistics")
    void shouldGenerateMessageStats() throws IOException {
        byte[] packet = createHdvPricePacket(
                new ItemData(289, 48, new long[]{15000, 140000, 1300000})
        );

        ParsedMessage result = parser.parse(packet);
        String stats = parser.getMessageStats(result);

        assertThat(stats)
                .contains("Type:")
                .contains("ID:")
                .contains("Size:")
                .contains("Items:");
    }

    @Test
    @DisplayName("Should handle null message in stats")
    void shouldHandleNullMessageInStats() {
        String stats = parser.getMessageStats(null);

        assertThat(stats).isEqualTo("null message");
    }

    // ===================================================================
    // INTEGRATION TESTS
    // ===================================================================

    @Test
    @DisplayName("Should parse realistic HDV session")
    void shouldParseRealisticHdvSession() throws IOException {
        // Simulate a realistic HDV browsing session with multiple items
        byte[] packet = createHdvPricePacket(
                // Cereals category
                new ItemData(289, 48, new long[]{15000, 140000, 1300000}),   // Wheat
                new ItemData(290, 48, new long[]{20000, 180000, 1600000}),   // Barley
                new ItemData(291, 48, new long[]{18000, 160000, 1400000}),   // Oats
                new ItemData(400, 48, new long[]{25000, 220000, 2000000}),   // Hop
                new ItemData(401, 48, new long[]{30000, 270000, 2500000}),   // Rice
                // Vegetables category
                new ItemData(539, 49, new long[]{12000, 110000, 1000000}),   // Flax
                new ItemData(674, 49, new long[]{50000, 450000, 4000000}),   // Hemp
                new ItemData(395, 49, new long[]{8000, 75000, 700000})       // Nettle
        );

        ParsedMessage result = parser.parse(packet);

        assertThat(result).isNotNull();
        assertThat(result.isParseSuccess()).isTrue();

        ExchangeTypesItemsMessage hdvMsg = result.getPayloadAs(ExchangeTypesItemsMessage.class);

        // Verify all items parsed
        assertThat(hdvMsg.getItemCount()).isEqualTo(8);

        // Verify price data extraction
        List<PriceData> allPrices = hdvMsg.toAllPriceData();
        assertThat(allPrices).hasSizeGreaterThanOrEqualTo(8 * 3);  // 8 items × 3 prices each

        // Verify some specific prices
        PriceData wheatPrice = allPrices.stream()
                .filter(p -> p.itemGid() == 289 && p.quantity() == 1)
                .findFirst()
                .orElse(null);

        assertThat(wheatPrice).isNotNull();
        assertThat(wheatPrice.price()).isEqualTo(15000);
        assertThat(wheatPrice.category()).isEqualTo(48);
    }

    @Test
    @DisplayName("Should handle large HDV message with many items")
    void shouldHandleLargeHdvMessage() throws IOException {
        // Create a large message with 50 items
        ItemData[] items = new ItemData[50];
        for (int i = 0; i < 50; i++) {
            items[i] = new ItemData(
                    1000 + i,  // itemGid
                    48,        // category
                    new long[]{10000 + i * 100, 90000 + i * 1000, 800000 + i * 10000}
            );
        }

        byte[] packet = createHdvPricePacket(items);

        ParsedMessage result = parser.parse(packet);

        assertThat(result).isNotNull();
        assertThat(result.isParseSuccess()).isTrue();

        ExchangeTypesItemsMessage hdvMsg = result.getPayloadAs(ExchangeTypesItemsMessage.class);
        assertThat(hdvMsg.getItemCount()).isEqualTo(50);
    }

    // ===================================================================
    // HELPER METHODS
    // ===================================================================

    /**
     * Helper record for test item data.
     */
    private record ItemData(int gid, int type, long[] prices) {
    }

    /**
     * Create a test HDV price packet.
     */
    private byte[] createHdvPricePacket(ItemData... items) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        // Message ID for ExchangeTypesItemsExchangerDescriptionForUserMessage
        int messageId = MessageDefinitions.MSG_EXCHANGE_TYPES_ITEMS_EXCHANGER_DESC;

        // Calculate payload size (approximate)
        ByteArrayOutputStream payloadStream = new ByteArrayOutputStream();
        DataOutputStream payloadDos = new DataOutputStream(payloadStream);

        // Write item count
        writeVarInt(payloadDos, items.length);

        // Write each item
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

        // Write payload length
        switch (lengthBytes) {
            case 1 -> dos.writeByte(payload.length);
            case 2 -> dos.writeShort(payload.length);
            case 3 -> {
                dos.writeByte((payload.length >> 16) & 0xFF);
                dos.writeShort(payload.length & 0xFFFF);
            }
        }

        // Write payload
        dos.write(payload);

        return baos.toByteArray();
    }

    /**
     * Create a test HDV category packet.
     */
    private byte[] createHdvCategoryPacket(int categoryId, String description) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        int messageId = MessageDefinitions.MSG_EXCHANGE_TYPES_EXCHANGER_DESC;

        // Payload
        ByteArrayOutputStream payloadStream = new ByteArrayOutputStream();
        DataOutputStream payloadDos = new DataOutputStream(payloadStream);

        writeVarInt(payloadDos, categoryId);
        // Note: Description handling may vary based on actual protocol

        byte[] payload = payloadStream.toByteArray();

        // Header
        int lengthBytes = calculateLengthBytes(payload.length);
        int header = (messageId << 2) | lengthBytes;
        dos.writeShort(header);

        // Length
        switch (lengthBytes) {
            case 1 -> dos.writeByte(payload.length);
            case 2 -> dos.writeShort(payload.length);
        }

        // Payload
        dos.write(payload);

        return baos.toByteArray();
    }

    /**
     * Create a packet with unknown message ID.
     */
    private byte[] createUnknownPacket(int unknownId) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        int header = (unknownId << 2) | 1;  // 1 byte length
        dos.writeShort(header);
        dos.writeByte(0);  // Empty payload

        return baos.toByteArray();
    }

    /**
     * Calculate length encoding bytes needed.
     */
    private int calculateLengthBytes(int length) {
        if (length == 0) return 0;
        if (length <= 255) return 1;
        if (length <= 65535) return 2;
        return 3;
    }

    /**
     * Write VarInt (Dofus encoding).
     */
    private void writeVarInt(DataOutputStream dos, int value) throws IOException {
        while ((value & ~0x7F) != 0) {
            dos.writeByte((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        dos.writeByte((byte) (value & 0x7F));
    }

    /**
     * Write VarLong (Dofus encoding).
     */
    private void writeVarLong(DataOutputStream dos, long value) throws IOException {
        while ((value & ~0x7FL) != 0) {
            dos.writeByte((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        dos.writeByte((byte) (value & 0x7F));
    }
}
