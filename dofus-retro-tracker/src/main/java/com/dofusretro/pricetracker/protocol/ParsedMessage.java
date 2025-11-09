package com.dofusretro.pricetracker.protocol;

import java.time.Instant;
import java.util.Optional;

/**
 * Wrapper for a parsed Dofus Retro protocol message.
 * <p>
 * This class encapsulates the result of parsing a raw network packet,
 * providing type-safe access to the message payload and metadata.
 * </p>
 * <p>
 * Usage example:
 * <pre>
 * ParsedMessage msg = parser.parse(rawPacket);
 * if (msg.isHdvPriceMessage()) {
 *     ExchangeTypesItemsMessage hdvMsg =
 *         msg.getPayloadAs(ExchangeTypesItemsMessage.class);
 *     List&lt;PriceData&gt; prices = hdvMsg.toAllPriceData();
 * }
 * </pre>
 * </p>
 *
 * @author AGENT-PROTOCOL
 * @version 1.0
 * @since 2025-11-09
 */
public class ParsedMessage {

    private final MessageType type;
    private final int messageId;
    private final byte[] rawData;
    private final Object payload;
    private final Instant parsedAt;
    private final boolean parseSuccess;
    private final String errorMessage;

    // ===================================================================
    // CONSTRUCTORS
    // ===================================================================

    /**
     * Create a successfully parsed message.
     *
     * @param type      the message type
     * @param messageId the protocol message ID
     * @param rawData   the raw packet data
     * @param payload   the parsed payload object
     * @param parsedAt  when the message was parsed
     */
    public ParsedMessage(MessageType type, int messageId, byte[] rawData,
                         Object payload, Instant parsedAt) {
        this.type = type;
        this.messageId = messageId;
        this.rawData = rawData;
        this.payload = payload;
        this.parsedAt = parsedAt;
        this.parseSuccess = true;
        this.errorMessage = null;
    }

    /**
     * Create a failed parse result.
     *
     * @param messageId    the message ID (if readable)
     * @param rawData      the raw packet data
     * @param errorMessage the error description
     */
    public ParsedMessage(int messageId, byte[] rawData, String errorMessage) {
        this.type = MessageType.UNKNOWN;
        this.messageId = messageId;
        this.rawData = rawData;
        this.payload = null;
        this.parsedAt = Instant.now();
        this.parseSuccess = false;
        this.errorMessage = errorMessage;
    }

    // ===================================================================
    // BUILDER PATTERN
    // ===================================================================

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private MessageType type;
        private int messageId;
        private byte[] rawData;
        private Object payload;
        private Instant parsedAt = Instant.now();
        private boolean parseSuccess = true;
        private String errorMessage;

        public Builder type(MessageType type) {
            this.type = type;
            return this;
        }

        public Builder messageId(int messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder rawData(byte[] rawData) {
            this.rawData = rawData;
            return this;
        }

        public Builder payload(Object payload) {
            this.payload = payload;
            return this;
        }

        public Builder parsedAt(Instant parsedAt) {
            this.parsedAt = parsedAt;
            return this;
        }

        public Builder parseSuccess(boolean parseSuccess) {
            this.parseSuccess = parseSuccess;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            this.parseSuccess = false;
            return this;
        }

        public ParsedMessage build() {
            if (!parseSuccess) {
                return new ParsedMessage(messageId, rawData, errorMessage);
            }
            return new ParsedMessage(type, messageId, rawData, payload, parsedAt);
        }
    }

    // ===================================================================
    // ACCESSOR METHODS
    // ===================================================================

    /**
     * Get the message type.
     *
     * @return the message type
     */
    public MessageType getType() {
        return type;
    }

    /**
     * Get the protocol message ID.
     *
     * @return the message ID
     */
    public int getMessageId() {
        return messageId;
    }

    /**
     * Get the raw packet data.
     *
     * @return the raw bytes
     */
    public byte[] getRawData() {
        return rawData;
    }

    /**
     * Get the parsed payload object.
     *
     * @return the payload, or null if parsing failed
     */
    public Object getPayload() {
        return payload;
    }

    /**
     * Get the timestamp when this message was parsed.
     *
     * @return the parse timestamp
     */
    public Instant getParsedAt() {
        return parsedAt;
    }

    /**
     * Check if parsing was successful.
     *
     * @return true if the message was successfully parsed
     */
    public boolean isParseSuccess() {
        return parseSuccess;
    }

    /**
     * Get the error message if parsing failed.
     *
     * @return the error message, or null if parsing succeeded
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    // ===================================================================
    // TYPE-SAFE PAYLOAD ACCESS
    // ===================================================================

    /**
     * Get the payload cast to a specific type.
     * <p>
     * Throws IllegalStateException if the payload is not of the expected type.
     * </p>
     *
     * @param clazz the expected payload class
     * @param <T>   the payload type
     * @return the typed payload
     * @throws IllegalStateException if payload type doesn't match
     */
    public <T> T getPayloadAs(Class<T> clazz) {
        if (payload == null) {
            throw new IllegalStateException("Payload is null (parsing may have failed)");
        }

        if (clazz.isInstance(payload)) {
            return clazz.cast(payload);
        }

        throw new IllegalStateException(
                String.format("Payload is not of type %s (actual: %s)",
                        clazz.getName(),
                        payload.getClass().getName())
        );
    }

    /**
     * Try to get the payload cast to a specific type.
     * <p>
     * Returns Optional.empty() if the payload is null or wrong type.
     * </p>
     *
     * @param clazz the expected payload class
     * @param <T>   the payload type
     * @return Optional containing the typed payload, or empty
     */
    public <T> Optional<T> tryGetPayloadAs(Class<T> clazz) {
        if (payload != null && clazz.isInstance(payload)) {
            return Optional.of(clazz.cast(payload));
        }
        return Optional.empty();
    }

    // ===================================================================
    // CONVENIENCE CHECKS
    // ===================================================================

    /**
     * Check if this is an HDV price message.
     *
     * @return true if this contains auction house price data
     */
    public boolean isHdvPriceMessage() {
        return type == MessageType.EXCHANGE_TYPES_ITEMS_EXCHANGER;
    }

    /**
     * Check if this is an HDV category message.
     *
     * @return true if this contains auction house category data
     */
    public boolean isHdvCategoryMessage() {
        return type == MessageType.EXCHANGE_TYPES_EXCHANGER;
    }

    /**
     * Check if this is any HDV-related message.
     *
     * @return true if this is an auction house message
     */
    public boolean isHdvMessage() {
        return type.isHdvMessage();
    }

    /**
     * Check if this is a compressed container message.
     *
     * @return true if this is a network data container
     */
    public boolean isContainerMessage() {
        return type == MessageType.NETWORK_DATA_CONTAINER;
    }

    /**
     * Check if this is an unknown message type.
     *
     * @return true if the message type was not recognized
     */
    public boolean isUnknown() {
        return type == MessageType.UNKNOWN;
    }

    /**
     * Check if this message contains price data.
     *
     * @return true if this message has item prices
     */
    public boolean containsPriceData() {
        return type.containsPriceData();
    }

    // ===================================================================
    // DATA ACCESS
    // ===================================================================

    /**
     * Get the size of the raw packet.
     *
     * @return byte count
     */
    public int getPacketSize() {
        return rawData != null ? rawData.length : 0;
    }

    /**
     * Get hex preview of raw data (first 32 bytes).
     *
     * @return hex string
     */
    public String getRawDataPreview() {
        if (rawData == null || rawData.length == 0) {
            return "(empty)";
        }

        StringBuilder sb = new StringBuilder();
        int limit = Math.min(rawData.length, 32);

        for (int i = 0; i < limit; i++) {
            sb.append(String.format("%02X ", rawData[i]));
        }

        if (rawData.length > limit) {
            sb.append("...");
        }

        return sb.toString().trim();
    }

    // ===================================================================
    // STRING REPRESENTATION
    // ===================================================================

    @Override
    public String toString() {
        if (!parseSuccess) {
            return String.format("ParsedMessage[FAILED: msgId=%d, error=%s]",
                    messageId, errorMessage);
        }

        return String.format("ParsedMessage[type=%s, msgId=%d, size=%d bytes, payload=%s]",
                type, messageId, getPacketSize(),
                payload != null ? payload.getClass().getSimpleName() : "null");
    }

    /**
     * Get detailed string representation.
     *
     * @return formatted string with all details
     */
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ParsedMessage {\n");
        sb.append(String.format("  Type: %s\n", type));
        sb.append(String.format("  Message ID: %d\n", messageId));
        sb.append(String.format("  Parse Success: %b\n", parseSuccess));

        if (!parseSuccess) {
            sb.append(String.format("  Error: %s\n", errorMessage));
        }

        sb.append(String.format("  Packet Size: %d bytes\n", getPacketSize()));
        sb.append(String.format("  Parsed At: %s\n", parsedAt));
        sb.append(String.format("  Payload Type: %s\n",
                payload != null ? payload.getClass().getName() : "null"));
        sb.append(String.format("  Raw Data Preview: %s\n", getRawDataPreview()));
        sb.append("}");

        return sb.toString();
    }
}
