package com.dofusretro.pricetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Item entity.
 * Used for API responses to transfer item data without exposing JPA entities.
 *
 * @author AGENT-DATA
 * @version 1.0
 * @since Wave 1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDTO {

    /**
     * Internal database ID.
     */
    private Long id;

    /**
     * The Dofus game's unique identifier for this item.
     */
    private Integer itemGid;

    /**
     * Human-readable name of the item.
     */
    private String itemName;

    /**
     * The category this item belongs to.
     */
    private CategoryDTO category;

    /**
     * The most recent price observation for this item.
     */
    private LatestPriceDTO latestPrice;

    /**
     * When this item was first added to the database.
     */
    private LocalDateTime createdAt;
}
