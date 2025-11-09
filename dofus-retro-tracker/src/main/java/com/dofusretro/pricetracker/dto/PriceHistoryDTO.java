package com.dofusretro.pricetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for price history data.
 * Contains historical price data and statistics for an item.
 *
 * @author AGENT-DATA
 * @version 1.0
 * @since Wave 1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistoryDTO {

    /**
     * The item's database ID.
     */
    private Long itemId;

    /**
     * The item's name for display purposes.
     */
    private String itemName;

    /**
     * List of historical price observations.
     */
    private List<PricePointDTO> prices;

    /**
     * Statistical summary of the price data.
     */
    private PriceStatisticsDTO statistics;
}

/**
 * Represents a single price data point in the history.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PricePointDTO {

    /**
     * When this price was observed.
     */
    private LocalDateTime timestamp;

    /**
     * The quantity this price is for (1, 10, or 100).
     */
    private Integer quantity;

    /**
     * The observed price in kamas.
     */
    private Long price;
}

/**
 * Statistical summary of price data.
 * Provides insights into price trends and volatility.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PriceStatisticsDTO {

    /**
     * Minimum price observed in the dataset.
     */
    private Long min;

    /**
     * Maximum price observed in the dataset.
     */
    private Long max;

    /**
     * Average price across all observations.
     */
    private Double avg;

    /**
     * Median price (50th percentile).
     */
    private Long median;
}
