package com.dofusretro.pricetracker.protocol;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Dofus Retro protocol message definitions.
 * <p>
 * This class contains message ID constants and record classes for
 * structured message data. Based on the Dofus 2.x protocol structure.
 * </p>
 * <p>
 * <strong>Note:</strong> Message IDs are placeholders and should be updated
 * based on actual packet captures or protocol definition files for Dofus Retro.
 * </p>
 *
 * @author AGENT-PROTOCOL
 * @version 1.0
 * @since 2025-11-09
 */
public final class MessageDefinitions {

    private MessageDefinitions() {
        // Utility class, no instantiation
    }

    // ===================================================================
    // MESSAGE ID CONSTANTS
    // ===================================================================

    /**
     * Compressed message container (confirmed ID from Dofus protocol).
     * Contains zlib-compressed data with inner message.
     */
    public static final int MSG_NETWORK_DATA_CONTAINER = 2;

    /**
     * Authentication ticket message (login).
     * <strong>Placeholder ID - needs verification for Dofus Retro</strong>
     */
    public static final int MSG_AUTHENTICATION_TICKET = 110;

    /**
     * Exchange types exchanger description for user (HDV categories).
     * <strong>Placeholder ID - needs verification for Dofus Retro</strong>
     */
    public static final int MSG_EXCHANGE_TYPES_EXCHANGER_DESC = 5905;

    /**
     * Exchange types items exchanger description for user (HDV items and prices).
     * This is the primary message for auction house price data.
     * <strong>Placeholder ID - needs verification for Dofus Retro</strong>
     */
    public static final int MSG_EXCHANGE_TYPES_ITEMS_EXCHANGER_DESC = 5904;

    // ===================================================================
    // MESSAGE RECORD CLASSES
    // ===================================================================

    /**
     * Item type description within an auction house message.
     * <p>
     * Represents a single item in the HDV with its ID, category, and prices
     * for different quantities (1, 10, 100).
     * </p>
     * <p>
     * Example from Python reference:
     * <pre>
     * {
     *   "objectGID": 289,      // Wheat
     *   "objectType": 48,       // Cereals category
     *   "prices": [15000, 140000, 1300000]
     * }
     * </pre>
     * </p>
     *
     * @param objectGid  the global item identifier (e.g., 289 for Wheat)
     * @param objectType the item category ID (e.g., 48 for Cereals)
     * @param prices     array of prices for quantities [1, 10, 100]
     */
    public record ItemTypeDescription(
            int objectGid,
            int objectType,
            long[] prices
    ) {
        /**
         * Convert to individual PriceData entries.
         * <p>
         * Expands the compact array representation into separate entries
         * for each quantity level (1, 10, 100).
         * </p>
         *
         * @return list of price data entries
         */
        public List<PriceData> toPriceDataList() {
            List<PriceData> priceList = new ArrayList<>();

            for (int i = 0; i < prices.length; i++) {
                if (prices[i] > 0) {  // Skip zero prices (unavailable)
                    int quantity = (int) Math.pow(10, i);  // 1, 10, 100
                    priceList.add(new PriceData(
                            objectGid,
                            objectType,
                            quantity,
                            prices[i],
                            Instant.now()
                    ));
                }
            }

            return priceList;
        }

        /**
         * Get price for a specific quantity.
         *
         * @param quantity the quantity (1, 10, or 100)
         * @return the price, or 0 if not available
         */
        public long getPriceForQuantity(int quantity) {
            return switch (quantity) {
                case 1 -> prices.length > 0 ? prices[0] : 0;
                case 10 -> prices.length > 1 ? prices[1] : 0;
                case 100 -> prices.length > 2 ? prices[2] : 0;
                default -> 0;
            };
        }
    }

    /**
     * Exchange types items exchanger description message.
     * <p>
     * Sent by the server when the client views auction house items and prices.
     * Contains a list of items with their prices at different quantity levels.
     * </p>
     * <p>
     * This is the main message we parse for price tracking.
     * </p>
     * <p>
     * Python reference structure:
     * <pre>
     * {
     *   "__type__": "ExchangeTypesItemsExchangerDescriptionForUserMessage",
     *   "itemTypeDescriptions": [
     *     { "objectGID": 289, "objectType": 48, "prices": [...] },
     *     { "objectGID": 290, "objectType": 48, "prices": [...] },
     *     ...
     *   ]
     * }
     * </pre>
     * </p>
     *
     * @param messageId            the protocol message ID
     * @param itemTypeDescriptions list of items with prices
     * @param receivedAt           when the message was received
     */
    public record ExchangeTypesItemsMessage(
            int messageId,
            List<ItemTypeDescription> itemTypeDescriptions,
            Instant receivedAt
    ) {
        /**
         * Convert all items to flat price data list.
         *
         * @return list of all price entries
         */
        public List<PriceData> toAllPriceData() {
            List<PriceData> allPrices = new ArrayList<>();

            for (ItemTypeDescription item : itemTypeDescriptions) {
                allPrices.addAll(item.toPriceDataList());
            }

            return allPrices;
        }

        /**
         * Get count of items in this message.
         *
         * @return number of items
         */
        public int getItemCount() {
            return itemTypeDescriptions.size();
        }

        /**
         * Get count of total price entries (after expansion).
         *
         * @return total number of price data points
         */
        public int getTotalPriceEntries() {
            return itemTypeDescriptions.stream()
                    .mapToInt(item -> item.toPriceDataList().size())
                    .sum();
        }
    }

    /**
     * Individual price data entry.
     * <p>
     * Represents a single price observation for an item at a specific quantity.
     * This is what gets stored in the database.
     * </p>
     *
     * @param itemGid    the global item identifier
     * @param category   the item category ID
     * @param quantity   the quantity this price applies to (1, 10, or 100)
     * @param price      the price in kamas
     * @param observedAt when this price was observed
     */
    public record PriceData(
            int itemGid,
            int category,
            int quantity,
            long price,
            Instant observedAt
    ) {
        /**
         * Calculate unit price (price per single item).
         *
         * @return the price per item
         */
        public long getUnitPrice() {
            return quantity > 0 ? price / quantity : 0;
        }

        /**
         * Check if this is a bulk discount price.
         *
         * @return true if quantity is 10 or 100
         */
        public boolean isBulkPrice() {
            return quantity >= 10;
        }

        /**
         * Format price with quantity.
         *
         * @return formatted string (e.g., "15000 kamas (x1)")
         */
        public String formatPrice() {
            return String.format("%d kamas (x%d)", price, quantity);
        }
    }

    /**
     * Exchange types exchanger description message (HDV categories).
     * <p>
     * Contains information about available auction house categories.
     * This is less critical for price tracking but useful for UI.
     * </p>
     *
     * @param messageId   the protocol message ID
     * @param objectType  the category type ID
     * @param description optional category description
     * @param receivedAt  when the message was received
     */
    public record ExchangeTypesMessage(
            int messageId,
            int objectType,
            String description,
            Instant receivedAt
    ) {
        /**
         * Create with default description.
         *
         * @param messageId  the message ID
         * @param objectType the object type
         * @param receivedAt when received
         */
        public ExchangeTypesMessage(int messageId, int objectType, Instant receivedAt) {
            this(messageId, objectType, null, receivedAt);
        }
    }

    /**
     * Network data container message (compressed).
     * <p>
     * A wrapper message that contains zlib-compressed data.
     * The compressed data itself contains another message.
     * </p>
     *
     * @param messageId        the message ID (always 2)
     * @param compressedData   the raw compressed bytes
     * @param uncompressedData the decompressed bytes (if decompressed)
     * @param innerMessage     the parsed inner message (if parsed)
     */
    public record NetworkDataContainer(
            int messageId,
            byte[] compressedData,
            byte[] uncompressedData,
            Object innerMessage
    ) {
        /**
         * Create with just compressed data.
         *
         * @param compressedData the compressed bytes
         */
        public NetworkDataContainer(byte[] compressedData) {
            this(MSG_NETWORK_DATA_CONTAINER, compressedData, null, null);
        }

        /**
         * Check if this container has been decompressed.
         *
         * @return true if uncompressedData is available
         */
        public boolean isDecompressed() {
            return uncompressedData != null;
        }

        /**
         * Check if inner message has been parsed.
         *
         * @return true if innerMessage is available
         */
        public boolean isParsed() {
            return innerMessage != null;
        }
    }

    /**
     * Unknown or unsupported message.
     * <p>
     * Used when we encounter a message ID we don't recognize.
     * Allows graceful degradation and logging.
     * </p>
     *
     * @param messageId  the unknown message ID
     * @param rawData    the raw message data
     * @param receivedAt when the message was received
     */
    public record UnknownMessage(
            int messageId,
            byte[] rawData,
            Instant receivedAt
    ) {
        /**
         * Get the size of the unknown message.
         *
         * @return byte count
         */
        public int getSize() {
            return rawData != null ? rawData.length : 0;
        }

        /**
         * Get hex dump of data (first 50 bytes).
         *
         * @return hex string for debugging
         */
        public String getDataPreview() {
            if (rawData == null || rawData.length == 0) {
                return "(empty)";
            }

            StringBuilder sb = new StringBuilder();
            int limit = Math.min(rawData.length, 50);

            for (int i = 0; i < limit; i++) {
                sb.append(String.format("%02X ", rawData[i]));
            }

            if (rawData.length > limit) {
                sb.append("...");
            }

            return sb.toString().trim();
        }
    }
}
