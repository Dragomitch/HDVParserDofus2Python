package com.dofusretro.pricetracker.service;

import com.dofusretro.pricetracker.protocol.*;
import com.dofusretro.pricetracker.protocol.MessageDefinitions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.BufferUnderflowException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Dofus Retro protocol parser service.
 * <p>
 * This service parses raw network packets from the Dofus Retro client/server
 * communication into structured message objects. It handles message framing,
 * decompression, and type-specific parsing.
 * </p>
 * <p>
 * Primary focus: Extract auction house (HDV) price data from
 * ExchangeTypesItemsExchangerDescriptionForUserMessage packets.
 * </p>
 * <p>
 * Based on the Python LaBot implementation:
 * https://github.com/louisabraham/LaBot
 * </p>
 *
 * @author AGENT-PROTOCOL
 * @version 1.0
 * @since 2025-11-09
 */
@Service
public class DofusRetroProtocolParser {

    private static final Logger log = LoggerFactory.getLogger(DofusRetroProtocolParser.class);

    // ===================================================================
    // PUBLIC INTERFACE
    // ===================================================================

    /**
     * Parse a raw network packet into a structured message.
     * <p>
     * This is the main entry point for packet parsing. It handles:
     * <ul>
     *   <li>Message header extraction</li>
     *   <li>Message type identification</li>
     *   <li>Type-specific payload parsing</li>
     *   <li>Error handling and logging</li>
     * </ul>
     * </p>
     *
     * @param rawPacket the raw packet bytes
     * @return parsed message, or null if parsing failed
     */
    public ParsedMessage parse(byte[] rawPacket) {
        if (rawPacket == null || rawPacket.length < 2) {
            log.warn("Packet too small or null: {} bytes",
                    rawPacket != null ? rawPacket.length : 0);
            return null;
        }

        try {
            BinaryReader reader = new BinaryReader(rawPacket);

            // Parse message header
            MessageHeader header = parseHeader(reader);

            log.debug("Parsing message: id={}, type={}, payloadSize={}",
                    header.messageId(), header.type(), header.payloadLength());

            // Handle compressed container
            if (header.type().isContainer()) {
                return parseCompressedContainer(header, reader, rawPacket);
            }

            // Parse payload based on type
            Object payload = parsePayload(header.type(), reader);

            return ParsedMessage.builder()
                    .type(header.type())
                    .messageId(header.messageId())
                    .rawData(rawPacket)
                    .payload(payload)
                    .parsedAt(Instant.now())
                    .build();

        } catch (BufferUnderflowException e) {
            log.error("Incomplete packet data (buffer underflow)", e);
            return ParsedMessage.builder()
                    .messageId(-1)
                    .rawData(rawPacket)
                    .errorMessage("Incomplete packet: " + e.getMessage())
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse packet", e);
            return ParsedMessage.builder()
                    .messageId(-1)
                    .rawData(rawPacket)
                    .errorMessage("Parse error: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Quick check if a packet contains HDV-related data.
     * <p>
     * This is a lightweight check that only reads the message ID
     * without full parsing. Useful for filtering packets.
     * </p>
     *
     * @param rawPacket the raw packet bytes
     * @return true if this is an HDV message
     */
    public boolean isHdvPacket(byte[] rawPacket) {
        if (rawPacket == null || rawPacket.length < 2) {
            return false;
        }

        try {
            BinaryReader reader = new BinaryReader(rawPacket);
            MessageHeader header = parseHeader(reader);
            return header.type().isHdvMessage();

        } catch (Exception e) {
            log.debug("Failed to check packet type", e);
            return false;
        }
    }

    /**
     * Check if a packet contains price data.
     *
     * @param rawPacket the raw packet bytes
     * @return true if this packet has item prices
     */
    public boolean containsPriceData(byte[] rawPacket) {
        if (rawPacket == null || rawPacket.length < 2) {
            return false;
        }

        try {
            BinaryReader reader = new BinaryReader(rawPacket);
            MessageHeader header = parseHeader(reader);
            return header.type().containsPriceData();

        } catch (Exception e) {
            log.debug("Failed to check packet type", e);
            return false;
        }
    }

    // ===================================================================
    // MESSAGE HEADER PARSING
    // ===================================================================

    /**
     * Parse the message header.
     * <p>
     * Dofus message header structure:
     * <pre>
     * Bytes 0-1: Header (2 bytes)
     *   Bits 2-15: Message ID (14 bits)
     *   Bits 0-1: Length encoding (2 bits)
     *
     * Bytes 2-5: Count (4 bytes, client messages only) - SKIPPED
     *
     * Bytes N+: Length (1-3 bytes based on encoding bits)
     *
     * Bytes M+: Payload (length bytes)
     * </pre>
     * </p>
     *
     * @param reader the binary reader
     * @return parsed header information
     */
    private MessageHeader parseHeader(BinaryReader reader) {
        // Read header (2 bytes)
        int header = reader.readUnsignedShort();

        // Extract message ID (bits 2-15)
        int messageId = header >> 2;

        // Extract length encoding (bits 0-1)
        int lengthBytes = header & 0x03;

        // Determine message type
        MessageType type = MessageType.fromId(messageId);

        // Skip count field if present (we don't use it)
        // Note: We can't reliably detect if count is present without
        // knowing if this is from client or server. For now, we assume
        // the payload length encoding is sufficient.

        // Read payload length
        int payloadLength = switch (lengthBytes) {
            case 0 -> 0;  // No payload
            case 1 -> reader.readUnsignedByte();  // 1 byte length
            case 2 -> reader.readUnsignedShort();  // 2 byte length
            case 3 -> (int) (reader.readUnsignedInt() & 0xFFFFFF);  // 3 byte length
            default -> throw new IllegalStateException("Invalid length encoding: " + lengthBytes);
        };

        log.trace("Header parsed: msgId={}, type={}, lenBytes={}, payloadLen={}",
                messageId, type, lengthBytes, payloadLength);

        return new MessageHeader(messageId, type, lengthBytes, payloadLength);
    }

    /**
     * Internal record for message header data.
     */
    private record MessageHeader(
            int messageId,
            MessageType type,
            int lengthEncoding,
            int payloadLength
    ) {
    }

    // ===================================================================
    // PAYLOAD PARSING BY TYPE
    // ===================================================================

    /**
     * Parse payload based on message type.
     *
     * @param type   the message type
     * @param reader the binary reader positioned at payload start
     * @return the parsed payload object, or null if not supported
     */
    private Object parsePayload(MessageType type, BinaryReader reader) {
        return switch (type) {
            case EXCHANGE_TYPES_ITEMS_EXCHANGER -> parseExchangeTypesItemsMessage(reader);
            case EXCHANGE_TYPES_EXCHANGER -> parseExchangeTypesMessage(reader);
            case UNKNOWN -> parseUnknownMessage(type.getId(), reader);
            default -> {
                log.debug("Message type {} not implemented for parsing", type);
                yield null;
            }
        };
    }

    /**
     * Parse ExchangeTypesItemsExchangerDescriptionForUserMessage.
     * <p>
     * This is the PRIMARY message for auction house price data.
     * </p>
     * <p>
     * Structure:
     * <pre>
     * {
     *   itemTypeDescriptions: [
     *     {
     *       objectGID: VarInt,
     *       objectType: VarInt,
     *       prices: [VarLong, VarLong, VarLong]  // qty 1, 10, 100
     *     },
     *     ...
     *   ]
     * }
     * </pre>
     * </p>
     *
     * @param reader the binary reader
     * @return the parsed message
     */
    private ExchangeTypesItemsMessage parseExchangeTypesItemsMessage(BinaryReader reader) {
        try {
            // Read count of item type descriptions
            int itemCount = reader.readVarInt();

            log.debug("Parsing {} item type descriptions", itemCount);

            List<ItemTypeDescription> items = new ArrayList<>(itemCount);

            for (int i = 0; i < itemCount; i++) {
                // Read item GID (global ID)
                int objectGid = reader.readVarInt();

                // Read object type (category)
                int objectType = reader.readVarInt();

                // Read prices array length
                int priceCount = reader.readVarInt();

                // Read prices (typically 3: for qty 1, 10, 100)
                long[] prices = new long[priceCount];
                for (int j = 0; j < priceCount; j++) {
                    prices[j] = reader.readVarLong();
                }

                items.add(new ItemTypeDescription(objectGid, objectType, prices));

                log.trace("Item {}: gid={}, type={}, prices={}",
                        i, objectGid, objectType, prices);
            }

            log.info("Parsed HDV price message: {} items, {} total price entries",
                    items.size(),
                    items.stream().mapToInt(item -> item.prices().length).sum());

            return new ExchangeTypesItemsMessage(
                    MessageDefinitions.MSG_EXCHANGE_TYPES_ITEMS_EXCHANGER_DESC,
                    items,
                    Instant.now()
            );

        } catch (Exception e) {
            log.error("Failed to parse ExchangeTypesItemsMessage", e);
            throw e;
        }
    }

    /**
     * Parse ExchangeTypesExchangerDescriptionForUserMessage.
     * <p>
     * Contains auction house category information.
     * </p>
     *
     * @param reader the binary reader
     * @return the parsed message
     */
    private ExchangeTypesMessage parseExchangeTypesMessage(BinaryReader reader) {
        try {
            // Read object type (category ID)
            int objectType = reader.readVarInt();

            // Optional: description (if present)
            String description = null;
            if (reader.remaining() > 0) {
                try {
                    description = reader.readUTF();
                } catch (Exception e) {
                    log.debug("No description available for category {}", objectType);
                }
            }

            log.debug("Parsed HDV category message: type={}, desc={}",
                    objectType, description);

            return new ExchangeTypesMessage(
                    MessageDefinitions.MSG_EXCHANGE_TYPES_EXCHANGER_DESC,
                    objectType,
                    description,
                    Instant.now()
            );

        } catch (Exception e) {
            log.error("Failed to parse ExchangeTypesMessage", e);
            throw e;
        }
    }

    /**
     * Handle unknown message type.
     *
     * @param messageId the message ID
     * @param reader    the binary reader
     * @return unknown message record
     */
    private UnknownMessage parseUnknownMessage(int messageId, BinaryReader reader) {
        byte[] remaining = new byte[reader.remaining()];
        reader.readBytes(reader.remaining());

        log.warn("Unknown message ID: {} (size: {} bytes)", messageId, remaining.length);

        return new UnknownMessage(messageId, remaining, Instant.now());
    }

    // ===================================================================
    // COMPRESSION HANDLING
    // ===================================================================

    /**
     * Parse a compressed NetworkDataContainerMessage.
     * <p>
     * These messages contain zlib-compressed data with an inner message.
     * We need to decompress and then parse the inner message.
     * </p>
     *
     * @param header    the message header
     * @param reader    the binary reader
     * @param rawPacket the original raw packet
     * @return parsed message (either the inner message or the container itself)
     */
    private ParsedMessage parseCompressedContainer(MessageHeader header,
                                                    BinaryReader reader,
                                                    byte[] rawPacket) {
        try {
            log.debug("Parsing compressed container message");

            // Read compressed data (as byte array with VarInt length)
            byte[] compressedData = reader.readByteArray();

            // Decompress
            byte[] uncompressed = decompress(compressedData);

            log.debug("Decompressed {} bytes to {} bytes",
                    compressedData.length, uncompressed.length);

            // Parse the inner message
            ParsedMessage innerMessage = parse(uncompressed);

            if (innerMessage != null) {
                log.debug("Inner message type: {}", innerMessage.getType());
                return innerMessage;  // Return the inner message directly
            } else {
                log.warn("Failed to parse inner message of container");
                return ParsedMessage.builder()
                        .type(MessageType.NETWORK_DATA_CONTAINER)
                        .messageId(header.messageId())
                        .rawData(rawPacket)
                        .payload(new NetworkDataContainer(compressedData))
                        .build();
            }

        } catch (Exception e) {
            log.error("Failed to decompress container message", e);
            return ParsedMessage.builder()
                    .messageId(header.messageId())
                    .rawData(rawPacket)
                    .errorMessage("Decompression failed: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Decompress zlib-compressed data.
     *
     * @param compressedData the compressed bytes
     * @return the decompressed bytes
     * @throws DataFormatException if decompression fails
     */
    private byte[] decompress(byte[] compressedData) throws DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(compressedData);

        // Estimate output size (typically 2-10x compressed size)
        byte[] buffer = new byte[compressedData.length * 10];

        int resultLength = inflater.inflate(buffer);
        inflater.end();

        // Trim to actual size
        byte[] result = new byte[resultLength];
        System.arraycopy(buffer, 0, result, 0, resultLength);

        return result;
    }

    // ===================================================================
    // UTILITY METHODS
    // ===================================================================

    /**
     * Get statistics about a parsed message.
     * <p>
     * Useful for logging and debugging.
     * </p>
     *
     * @param message the parsed message
     * @return formatted statistics string
     */
    public String getMessageStats(ParsedMessage message) {
        if (message == null) {
            return "null message";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Type: %s, ", message.getType()));
        sb.append(String.format("ID: %d, ", message.getMessageId()));
        sb.append(String.format("Size: %d bytes", message.getPacketSize()));

        if (message.isHdvPriceMessage()) {
            var hdvMsg = message.tryGetPayloadAs(ExchangeTypesItemsMessage.class);
            hdvMsg.ifPresent(msg ->
                    sb.append(String.format(", Items: %d, Prices: %d",
                            msg.getItemCount(), msg.getTotalPriceEntries()))
            );
        }

        return sb.toString();
    }

    /**
     * Extract all price data from a parsed message.
     * <p>
     * If the message contains price data, extracts and returns it as a list.
     * Otherwise returns an empty list.
     * </p>
     *
     * @param message the parsed message
     * @return list of price data (empty if no price data)
     */
    public List<PriceData> extractPriceData(ParsedMessage message) {
        if (message == null || !message.containsPriceData()) {
            return List.of();
        }

        try {
            ExchangeTypesItemsMessage hdvMsg =
                    message.getPayloadAs(ExchangeTypesItemsMessage.class);
            return hdvMsg.toAllPriceData();

        } catch (Exception e) {
            log.error("Failed to extract price data from message", e);
            return List.of();
        }
    }
}
