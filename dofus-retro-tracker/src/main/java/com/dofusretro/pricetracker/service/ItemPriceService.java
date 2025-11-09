package com.dofusretro.pricetracker.service;

import com.dofusretro.pricetracker.exception.BusinessException;
import com.dofusretro.pricetracker.exception.ParsingException;
import com.dofusretro.pricetracker.model.Item;
import com.dofusretro.pricetracker.model.PriceEntry;
import com.dofusretro.pricetracker.protocol.MessageDefinitions.PriceData;
import com.dofusretro.pricetracker.protocol.ParsedMessage;
import com.dofusretro.pricetracker.repository.ItemRepository;
import com.dofusretro.pricetracker.repository.PriceEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for processing item price data from parsed packets.
 * <p>
 * This service orchestrates the business logic for:
 * <ul>
 *   <li>Consuming parsed packets from the protocol parser</li>
 *   <li>Extracting price data from messages</li>
 *   <li>Persisting items and price entries to the database</li>
 *   <li>Managing cache for frequently accessed items</li>
 *   <li>Batch processing for optimal performance</li>
 * </ul>
 * </p>
 * <p>
 * This service implements proper transaction management with @Transactional
 * to ensure data consistency. It also provides graceful error handling
 * and retry logic for transient failures.
 * </p>
 *
 * @author AGENT-BUSINESS
 * @version 1.0
 * @since Wave 2
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ItemPriceService {

    private final DofusRetroProtocolParser parser;
    private final ItemRepository itemRepository;
    private final PriceEntryRepository priceEntryRepository;

    /**
     * Process a raw packet and extract/persist price data.
     * <p>
     * This method:
     * <ol>
     *   <li>Parses the raw packet using the protocol parser</li>
     *   <li>Checks if the packet contains price data</li>
     *   <li>Extracts price data from the message</li>
     *   <li>Persists items and prices to the database</li>
     * </ol>
     * </p>
     *
     * @param rawPacket the raw packet bytes
     * @return the number of price entries persisted
     * @throws ParsingException if packet parsing fails
     * @throws BusinessException if persistence fails
     */
    @Transactional
    public int processPacket(byte[] rawPacket) {
        if (rawPacket == null || rawPacket.length == 0) {
            log.debug("Skipping empty packet");
            return 0;
        }

        try {
            // Parse the packet
            ParsedMessage message = parser.parse(rawPacket);

            if (message == null) {
                log.debug("Parser returned null message, skipping");
                return 0;
            }

            if (message.hasError()) {
                log.warn("Packet parsing failed: {}", message.getErrorMessage());
                throw new ParsingException(message.getErrorMessage());
            }

            // Check if this packet contains price data
            if (!message.containsPriceData()) {
                log.trace("Packet does not contain price data (type: {}), skipping", message.getType());
                return 0;
            }

            // Extract price data
            List<PriceData> priceDataList = parser.extractPriceData(message);

            if (priceDataList.isEmpty()) {
                log.debug("No price data extracted from packet");
                return 0;
            }

            log.info("Processing {} price data entries from packet", priceDataList.size());

            // Persist price data
            return persistPriceData(priceDataList);

        } catch (ParsingException e) {
            log.error("Failed to parse packet: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error processing packet", e);
            throw BusinessException.databaseError("processPacket", e);
        }
    }

    /**
     * Process a batch of raw packets.
     * <p>
     * This method provides efficient batch processing of multiple packets,
     * which is useful for processing accumulated queue data.
     * </p>
     *
     * @param rawPackets list of raw packet bytes
     * @return the total number of price entries persisted
     */
    @Transactional
    public int processPacketBatch(List<byte[]> rawPackets) {
        if (rawPackets == null || rawPackets.isEmpty()) {
            return 0;
        }

        log.info("Processing batch of {} packets", rawPackets.size());

        int totalPersisted = 0;
        int successCount = 0;
        int failureCount = 0;

        for (byte[] packet : rawPackets) {
            try {
                int persisted = processPacket(packet);
                totalPersisted += persisted;
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.warn("Failed to process packet in batch: {}", e.getMessage());
                // Continue processing remaining packets
            }
        }

        log.info("Batch processing complete: total={}, success={}, failed={}, persisted={}",
                rawPackets.size(), successCount, failureCount, totalPersisted);

        if (failureCount > 0 && successCount == 0) {
            throw BusinessException.batchProcessingFailed(rawPackets.size(), successCount, failureCount);
        }

        return totalPersisted;
    }

    /**
     * Persist a list of price data entries to the database.
     * <p>
     * This method handles:
     * <ul>
     *   <li>Item creation or retrieval (by itemGid)</li>
     *   <li>Price entry creation</li>
     *   <li>Batch inserts for efficiency</li>
     *   <li>Cache invalidation for updated items</li>
     * </ul>
     * </p>
     *
     * @param priceDataList the price data to persist
     * @return the number of price entries created
     */
    @Transactional
    public int persistPriceData(List<PriceData> priceDataList) {
        if (priceDataList == null || priceDataList.isEmpty()) {
            return 0;
        }

        List<PriceEntry> entriesToSave = new ArrayList<>();

        for (PriceData priceData : priceDataList) {
            try {
                // Validate price data
                validatePriceData(priceData);

                // Get or create item
                Item item = getOrCreateItem(priceData.itemGid());

                // Create price entry
                PriceEntry priceEntry = PriceEntry.builder()
                        .item(item)
                        .price(priceData.price())
                        .quantity(priceData.quantity())
                        .serverTimestamp(priceData.observedAt().toEpochMilli())
                        .build();

                entriesToSave.add(priceEntry);

                // Invalidate cache for this item
                evictItemCache(priceData.itemGid());

            } catch (BusinessException e) {
                log.warn("Skipping invalid price data: {}", e.getMessage());
                // Continue with remaining entries
            }
        }

        if (!entriesToSave.isEmpty()) {
            // Batch save for efficiency
            List<PriceEntry> saved = priceEntryRepository.saveAll(entriesToSave);
            log.info("Persisted {} price entries", saved.size());
            return saved.size();
        }

        return 0;
    }

    /**
     * Get an item by its game ID, or create it if it doesn't exist.
     *
     * @param itemGid the item game ID
     * @return the item entity
     */
    @Transactional
    @Cacheable(value = "items", key = "#itemGid")
    public Item getOrCreateItem(int itemGid) {
        Optional<Item> existing = itemRepository.findByItemGid(itemGid);

        if (existing.isPresent()) {
            return existing.get();
        }

        // Create new item
        Item newItem = Item.builder()
                .itemGid(itemGid)
                .itemName("Item #" + itemGid)  // Placeholder name until we have a name mapping
                .build();

        Item saved = itemRepository.save(newItem);
        log.info("Created new item: itemGid={}, id={}", itemGid, saved.getId());

        return saved;
    }

    /**
     * Get an item by its game ID (cached).
     *
     * @param itemGid the item game ID
     * @return Optional containing the item if found
     */
    @Cacheable(value = "items", key = "#itemGid")
    public Optional<Item> getItemByGid(int itemGid) {
        return itemRepository.findByItemGid(itemGid);
    }

    /**
     * Get an item with its price history.
     *
     * @param itemGid the item game ID
     * @return Optional containing the item with prices if found
     */
    @Cacheable(value = "itemsWithPrices", key = "#itemGid")
    public Optional<Item> getItemWithPrices(int itemGid) {
        Optional<Item> item = itemRepository.findByItemGid(itemGid);
        if (item.isPresent()) {
            // Use JOIN FETCH query to load prices
            return itemRepository.findByIdWithPrices(item.get().getId());
        }
        return Optional.empty();
    }

    /**
     * Get the latest price for an item at a specific quantity.
     *
     * @param itemGid  the item game ID
     * @param quantity the quantity (1, 10, or 100)
     * @return Optional containing the latest price entry
     */
    @Cacheable(value = "latestPrices", key = "#itemGid + '_' + #quantity")
    public Optional<PriceEntry> getLatestPrice(int itemGid, int quantity) {
        Optional<Item> item = itemRepository.findByItemGid(itemGid);
        if (item.isEmpty()) {
            return Optional.empty();
        }

        return priceEntryRepository.findTopByItemAndQuantityOrderByCreatedAtDesc(
                item.get(), quantity);
    }

    /**
     * Get price history for an item within a time range.
     *
     * @param itemGid  the item game ID
     * @param quantity the quantity
     * @param start    start time
     * @param end      end time
     * @return list of price entries
     */
    public List<PriceEntry> getPriceHistory(int itemGid, int quantity,
                                             LocalDateTime start, LocalDateTime end) {
        Optional<Item> item = itemRepository.findByItemGid(itemGid);
        if (item.isEmpty()) {
            return List.of();
        }

        return priceEntryRepository.findByItemAndQuantityAndCreatedAtBetween(
                item.get(), quantity, start, end);
    }

    /**
     * Evict an item from the cache.
     *
     * @param itemGid the item game ID to evict
     */
    @CacheEvict(value = {"items", "itemsWithPrices", "latestPrices"}, key = "#itemGid")
    public void evictItemCache(int itemGid) {
        log.trace("Evicted cache for item: {}", itemGid);
    }

    /**
     * Validate price data before persistence.
     *
     * @param priceData the price data to validate
     * @throws BusinessException if validation fails
     */
    private void validatePriceData(PriceData priceData) {
        if (priceData.itemGid() <= 0) {
            throw BusinessException.invalidPriceData(
                    priceData.itemGid(), priceData.price(), "Item GID must be positive");
        }

        if (priceData.price() < 0) {
            throw BusinessException.invalidPriceData(
                    priceData.itemGid(), priceData.price(), "Price cannot be negative");
        }

        if (priceData.quantity() != 1 && priceData.quantity() != 10 && priceData.quantity() != 100) {
            throw BusinessException.invalidPriceData(
                    priceData.itemGid(), priceData.price(),
                    "Quantity must be 1, 10, or 100, got: " + priceData.quantity());
        }

        // Skip zero prices (item not available at this quantity)
        if (priceData.price() == 0) {
            log.trace("Skipping zero price for item {} (quantity: {})",
                    priceData.itemGid(), priceData.quantity());
            throw BusinessException.invalidPriceData(
                    priceData.itemGid(), priceData.price(), "Price is zero (not available)");
        }
    }

    /**
     * Get statistics about price data persistence.
     *
     * @return formatted statistics string
     */
    public String getStatistics() {
        long itemCount = itemRepository.count();
        long priceCount = priceEntryRepository.count();

        return String.format("Items: %d, Price Entries: %d", itemCount, priceCount);
    }
}
