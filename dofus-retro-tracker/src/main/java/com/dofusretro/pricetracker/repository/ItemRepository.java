package com.dofusretro.pricetracker.repository;

import com.dofusretro.pricetracker.model.Item;
import com.dofusretro.pricetracker.model.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Item entity operations.
 * Provides CRUD operations and custom query methods for items.
 *
 * @author AGENT-DATA
 * @version 1.0
 * @since Wave 1
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Find an item by its Dofus game ID (itemGid).
     *
     * @param itemGid the game's unique identifier for the item
     * @return Optional containing the item if found
     */
    Optional<Item> findByItemGid(Integer itemGid);

    /**
     * Find all items belonging to a specific sub-category.
     *
     * @param subCategory the sub-category entity
     * @return list of items in the sub-category
     */
    List<Item> findBySubCategory(SubCategory subCategory);

    /**
     * Find all items belonging to a sub-category by its ID.
     *
     * @param subCategoryId the sub-category ID
     * @return list of items in the sub-category
     */
    List<Item> findBySubCategoryId(Long subCategoryId);

    /**
     * Search for items by name (case-insensitive partial match).
     *
     * @param name the name or partial name to search for
     * @return list of matching items
     */
    List<Item> findByItemNameContainingIgnoreCase(String name);

    /**
     * Find an item by ID with its price history eagerly loaded.
     * Uses a JOIN FETCH to avoid N+1 query problems.
     *
     * @param id the item ID
     * @return Optional containing the item with prices if found
     */
    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.prices WHERE i.id = :id")
    Optional<Item> findByIdWithPrices(@Param("id") Long id);

    /**
     * Check if an item exists with the given Dofus game ID.
     *
     * @param itemGid the game ID to check
     * @return true if an item with this ID exists
     */
    boolean existsByItemGid(Integer itemGid);

    /**
     * Count the number of items in a specific sub-category.
     *
     * @param subCategoryId the sub-category ID
     * @return count of items in the sub-category
     */
    long countBySubCategoryId(Long subCategoryId);
}
