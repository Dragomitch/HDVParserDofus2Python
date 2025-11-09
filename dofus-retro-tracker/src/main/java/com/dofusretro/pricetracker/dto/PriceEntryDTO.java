package com.dofusretro.pricetracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for PriceEntry entity.
 * Used to expose price entry data through the REST API.
 *
 * @author AGENT-API
 * @version 1.0
 * @since Wave 2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Represents a single price observation for an item at a specific quantity")
public class PriceEntryDTO {

    @Schema(description = "Unique identifier", example = "1")
    private Long id;

    @Schema(description = "Item ID this price entry is for", example = "1")
    private Long itemId;

    @Schema(description = "Item name (optional, for convenience)", example = "Wheat")
    private String itemName;

    @Schema(description = "Observed price in kamas", example = "15000")
    private Long price;

    @Schema(description = "Quantity this price is for (1, 10, or 100)", example = "1")
    private Integer quantity;

    @Schema(description = "Timestamp when this price entry was captured")
    private LocalDateTime createdAt;

    @Schema(description = "Server timestamp from the game server (milliseconds since epoch)", example = "1699564800000")
    private Long serverTimestamp;

    @Schema(description = "Formatted price string", example = "15 K")
    private String formattedPrice;
}
