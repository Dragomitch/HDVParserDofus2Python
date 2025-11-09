package com.dofusretro.pricetracker.repository;

import com.dofusretro.pricetracker.model.Item;
import com.dofusretro.pricetracker.model.PriceEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for PriceEntry entity operations.
 * Provides CRUD operations and custom query methods for price history data.
 *
 * @author AGENT-DATA
 * @version 1.0
 * @since Wave 1
 */
@Repository
public interface PriceEntryRepository extends JpaRepository<PriceEntry, Long> {

    /**
     * Find all price entries for an item, ordered by creation time (newest first).
     *
     * @param item the item entity
     * @return list of price entries ordered by creation time descending
     */
    List<PriceEntry> findByItemOrderByCreatedAtDesc(Item item);

    /**
     * Find all price entries for a specific item at a specific quantity.
     *
     * @param item the item entity
     * @param quantity the quantity (1, 10, or 100)
     * @return list of price entries for the item at the specified quantity
     */
    List<PriceEntry> findByItemAndQuantity(Item item, Integer quantity);

    /**
     * Get price history for an item starting from a specific date.
     * Returns entries ordered by creation time ascending (oldest first).
     *
     * @param itemId the item ID
     * @param startDate the start date for the history
     * @return list of price entries ordered chronologically
     */
    @Query("SELECT p FROM PriceEntry p WHERE p.item.id = :itemId " +
           "AND p.createdAt >= :startDate " +
           "ORDER BY p.createdAt ASC")
    List<PriceEntry> findPriceHistory(
        @Param("itemId") Long itemId,
        @Param("startDate") LocalDateTime startDate
    );

    /**
     * Get price history for an item at a specific quantity within a date range.
     * Useful for generating price trend charts for a specific quantity.
     *
     * @param itemId the item ID
     * @param quantity the quantity (1, 10, or 100)
     * @param startDate the start date for the history
     * @param endDate the end date for the history
     * @return list of price entries ordered chronologically
     */
    @Query("SELECT p FROM PriceEntry p WHERE p.item.id = :itemId " +
           "AND p.quantity = :quantity " +
           "AND p.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY p.createdAt ASC")
    List<PriceEntry> findPriceHistoryByQuantity(
        @Param("itemId") Long itemId,
        @Param("quantity") Integer quantity,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Count recent price entries for an item since a specific time.
     * Useful for determining data freshness and collection frequency.
     *
     * @param itemId the item ID
     * @param since the timestamp to count from
     * @return count of price entries since the specified time
     */
    @Query("SELECT COUNT(p) FROM PriceEntry p WHERE p.item.id = :itemId " +
           "AND p.createdAt >= :since")
    long countRecentEntries(
        @Param("itemId") Long itemId,
        @Param("since") LocalDateTime since
    );

    /**
     * Delete price entries older than a specific date.
     * Used for data retention and cleanup operations.
     *
     * @param date the cutoff date; entries before this date will be deleted
     */
    void deleteByCreatedAtBefore(LocalDateTime date);
}
