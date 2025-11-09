package com.dofusretro.pricetracker.protocol;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Enumeration of supported Dofus Retro protocol message types.
 * <p>
 * Each message type has an associated ID and description. The ID values
 * are based on Dofus 2.x protocol and may need adjustment for Dofus Retro.
 * </p>
 *
 * @author AGENT-PROTOCOL
 * @version 1.0
 * @since 2025-11-09
 */
public enum MessageType {

    /**
     * Compressed message container.
     * Contains zlib-compressed data with an inner message.
     */
    NETWORK_DATA_CONTAINER(
            MessageDefinitions.MSG_NETWORK_DATA_CONTAINER,
            "Network Data Container",
            "Compressed message wrapper"
    ),

    /**
     * Authentication ticket message.
     * Used during login sequence.
     */
    AUTHENTICATION_TICKET(
            MessageDefinitions.MSG_AUTHENTICATION_TICKET,
            "Authentication Ticket",
            "Login authentication message"
    ),

    /**
     * Exchange types exchanger description.
     * Contains HDV category information.
     */
    EXCHANGE_TYPES_EXCHANGER(
            MessageDefinitions.MSG_EXCHANGE_TYPES_EXCHANGER_DESC,
            "Exchange Types Exchanger Description",
            "Auction house category information"
    ),

    /**
     * Exchange types items exchanger description.
     * Contains HDV items with prices - PRIMARY MESSAGE FOR PRICE TRACKING.
     */
    EXCHANGE_TYPES_ITEMS_EXCHANGER(
            MessageDefinitions.MSG_EXCHANGE_TYPES_ITEMS_EXCHANGER_DESC,
            "Exchange Types Items Exchanger Description",
            "Auction house items and prices (PRIMARY)"
    ),

    /**
     * Unknown or unsupported message type.
     * Fallback for unrecognized message IDs.
     */
    UNKNOWN(-1, "Unknown Message", "Unrecognized message type");

    // ===================================================================
    // FIELDS
    // ===================================================================

    private final int id;
    private final String name;
    private final String description;

    // Static lookup map for fast ID -> MessageType resolution
    private static final Map<Integer, MessageType> ID_MAP =
            Arrays.stream(values())
                    .filter(type -> type.id != -1)  // Exclude UNKNOWN
                    .collect(Collectors.toMap(
                            MessageType::getId,
                            Function.identity()
                    ));

    // ===================================================================
    // CONSTRUCTOR
    // ===================================================================

    MessageType(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // ===================================================================
    // STATIC FACTORY METHODS
    // ===================================================================

    /**
     * Get MessageType from message ID.
     * <p>
     * Provides fast lookup using pre-computed map.
     * </p>
     *
     * @param id the message ID
     * @return the corresponding MessageType, or UNKNOWN if not found
     */
    public static MessageType fromId(int id) {
        return ID_MAP.getOrDefault(id, UNKNOWN);
    }

    /**
     * Check if a message ID is recognized.
     *
     * @param id the message ID
     * @return true if the ID is supported
     */
    public static boolean isKnownId(int id) {
        return ID_MAP.containsKey(id);
    }

    /**
     * Get all HDV-related message types.
     * <p>
     * Returns message types relevant to auction house price tracking.
     * </p>
     *
     * @return array of HDV message types
     */
    public static MessageType[] getHdvMessageTypes() {
        return new MessageType[]{
                EXCHANGE_TYPES_EXCHANGER,
                EXCHANGE_TYPES_ITEMS_EXCHANGER
        };
    }

    // ===================================================================
    // INSTANCE METHODS
    // ===================================================================

    /**
     * Get the protocol message ID.
     *
     * @return the message ID
     */
    public int getId() {
        return id;
    }

    /**
     * Get the human-readable message name.
     *
     * @return the message name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the message description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Check if this is an HDV-related message.
     * <p>
     * HDV messages contain auction house data we want to track.
     * </p>
     *
     * @return true if this is an auction house message
     */
    public boolean isHdvMessage() {
        return this == EXCHANGE_TYPES_EXCHANGER ||
                this == EXCHANGE_TYPES_ITEMS_EXCHANGER;
    }

    /**
     * Check if this message type contains price data.
     *
     * @return true if this message has item prices
     */
    public boolean containsPriceData() {
        return this == EXCHANGE_TYPES_ITEMS_EXCHANGER;
    }

    /**
     * Check if this is a compressed container message.
     *
     * @return true if this is a network data container
     */
    public boolean isContainer() {
        return this == NETWORK_DATA_CONTAINER;
    }

    /**
     * Check if this is an unknown message type.
     *
     * @return true if the message type is not recognized
     */
    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    /**
     * Get priority level for processing.
     * <p>
     * Higher priority messages are processed first.
     * </p>
     *
     * @return priority value (higher = more important)
     */
    public int getPriority() {
        return switch (this) {
            case EXCHANGE_TYPES_ITEMS_EXCHANGER -> 100;  // Highest - price data
            case EXCHANGE_TYPES_EXCHANGER -> 50;         // Medium - category data
            case NETWORK_DATA_CONTAINER -> 75;           // High - may contain price data
            case AUTHENTICATION_TICKET -> 25;            // Low - not price related
            case UNKNOWN -> 0;                           // Lowest - unknown
        };
    }

    @Override
    public String toString() {
        if (this == UNKNOWN) {
            return String.format("%s (id=unknown)", name);
        }
        return String.format("%s (id=%d)", name, id);
    }

    /**
     * Get detailed string representation.
     *
     * @return formatted string with all details
     */
    public String toDetailedString() {
        return String.format("%s [id=%d, hdv=%b, price=%b] - %s",
                name, id, isHdvMessage(), containsPriceData(), description);
    }
}
