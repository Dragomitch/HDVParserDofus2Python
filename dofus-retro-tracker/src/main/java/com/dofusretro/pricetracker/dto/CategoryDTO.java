package com.dofusretro.pricetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for SubCategory entity.
 * Used for API responses to transfer category data without exposing JPA entities.
 *
 * @author AGENT-DATA
 * @version 1.0
 * @since Wave 1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    /**
     * Internal database ID.
     */
    private Long id;

    /**
     * The Dofus game's unique identifier for this sub-category.
     */
    private Integer dofusId;

    /**
     * Human-readable name of the sub-category (e.g., "Cereals", "Fish").
     */
    private String name;

    /**
     * Number of items in this category.
     * Useful for displaying category statistics.
     */
    private Long itemCount;
}
