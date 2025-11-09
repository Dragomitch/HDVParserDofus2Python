package com.dofusretro.pricetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for the latest price of an item.
 * Represents the most recent price observation for a specific quantity.
 *
 * @author AGENT-DATA
 * @version 1.0
 * @since Wave 1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LatestPriceDTO {

    /**
     * The observed price in kamas (game currency).
     */
    private Long price;

    /**
     * The quantity this price is for (1, 10, or 100).
     */
    private Integer quantity;

    /**
     * When this price was observed.
     */
    private LocalDateTime timestamp;
}
