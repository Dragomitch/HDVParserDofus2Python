package com.dofusretro.pricetracker.exception;

/**
 * Exception thrown when packet parsing fails.
 * <p>
 * This exception is thrown when the protocol parser encounters data that
 * it cannot parse correctly. This could be due to:
 * <ul>
 *   <li>Malformed packet data</li>
 *   <li>Unknown message types</li>
 *   <li>Incomplete packet data</li>
 *   <li>Decompression failures</li>
 *   <li>Protocol version mismatches</li>
 * </ul>
 * </p>
 * <p>
 * Parsing exceptions are expected in network packet processing and should
 * be handled gracefully without crashing the application.
 * </p>
 *
 * @author AGENT-BUSINESS
 * @version 1.0
 * @since Wave 2
 */
public class ParsingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * The raw packet data that failed to parse (first 100 bytes).
     */
    private final byte[] packetPreview;

    /**
     * The message ID that was being parsed (if known).
     */
    private final Integer messageId;

    /**
     * Constructs a new parsing exception with the specified detail message.
     *
     * @param message the detail message
     */
    public ParsingException(String message) {
        super(message);
        this.packetPreview = null;
        this.messageId = null;
    }

    /**
     * Constructs a new parsing exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public ParsingException(String message, Throwable cause) {
        super(message, cause);
        this.packetPreview = null;
        this.messageId = null;
    }

    /**
     * Constructs a new parsing exception with message, cause, and packet data.
     *
     * @param message    the detail message
     * @param cause      the cause of the exception
     * @param rawPacket  the raw packet data that failed
     * @param messageId  the message ID (if known)
     */
    public ParsingException(String message, Throwable cause, byte[] rawPacket, Integer messageId) {
        super(message, cause);
        this.packetPreview = rawPacket != null ? copyPacketPreview(rawPacket) : null;
        this.messageId = messageId;
    }

    /**
     * Gets a preview of the packet data (first 100 bytes).
     *
     * @return the packet preview, or null if not available
     */
    public byte[] getPacketPreview() {
        return packetPreview != null ? packetPreview.clone() : null;
    }

    /**
     * Gets the message ID that was being parsed.
     *
     * @return the message ID, or null if not known
     */
    public Integer getMessageId() {
        return messageId;
    }

    /**
     * Gets a hex dump of the packet preview for debugging.
     *
     * @return hex string representation, or empty string if no data
     */
    public String getPacketPreviewHex() {
        if (packetPreview == null || packetPreview.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(packetPreview.length, 50); i++) {
            sb.append(String.format("%02X ", packetPreview[i]));
        }
        if (packetPreview.length > 50) {
            sb.append("...");
        }
        return sb.toString().trim();
    }

    /**
     * Copy the first 100 bytes of packet data for the preview.
     */
    private static byte[] copyPacketPreview(byte[] packet) {
        int length = Math.min(packet.length, 100);
        byte[] preview = new byte[length];
        System.arraycopy(packet, 0, preview, 0, length);
        return preview;
    }

    /**
     * Creates a parsing exception for malformed packet data.
     *
     * @param messageId the message ID
     * @param rawPacket the raw packet data
     * @param cause     the underlying cause
     * @return a ParsingException
     */
    public static ParsingException malformedPacket(Integer messageId, byte[] rawPacket, Throwable cause) {
        return new ParsingException(
                String.format("Malformed packet data for message ID %d", messageId),
                cause,
                rawPacket,
                messageId
        );
    }

    /**
     * Creates a parsing exception for unknown message types.
     *
     * @param messageId the unknown message ID
     * @param rawPacket the raw packet data
     * @return a ParsingException
     */
    public static ParsingException unknownMessageType(int messageId, byte[] rawPacket) {
        return new ParsingException(
                String.format("Unknown message type: %d", messageId),
                null,
                rawPacket,
                messageId
        );
    }

    /**
     * Creates a parsing exception for incomplete packet data.
     *
     * @param messageId     the message ID
     * @param expectedSize  the expected packet size
     * @param actualSize    the actual packet size
     * @param rawPacket     the raw packet data
     * @return a ParsingException
     */
    public static ParsingException incompletePacket(Integer messageId, int expectedSize,
                                                     int actualSize, byte[] rawPacket) {
        return new ParsingException(
                String.format("Incomplete packet: expected %d bytes, got %d bytes (message ID: %d)",
                        expectedSize, actualSize, messageId),
                null,
                rawPacket,
                messageId
        );
    }

    /**
     * Creates a parsing exception for decompression failures.
     *
     * @param rawPacket the compressed data
     * @param cause     the decompression error
     * @return a ParsingException
     */
    public static ParsingException decompressionFailed(byte[] rawPacket, Throwable cause) {
        return new ParsingException(
                "Failed to decompress packet data",
                cause,
                rawPacket,
                2  // NetworkDataContainer message ID
        );
    }
}
