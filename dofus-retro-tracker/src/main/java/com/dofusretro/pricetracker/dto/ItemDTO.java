package com.dofusretro.pricetracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Item entity.
 * Used to expose item data through the REST API without exposing the entity directly.
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
@Schema(description = "Represents a Dofus game item that can be traded in the auction house")
public class ItemDTO {

    @Schema(description = "Unique identifier", example = "1")
    private Long id;

    @Schema(description = "Dofus game item identifier (GID)", example = "289")
    private Integer itemGid;

    @Schema(description = "Item name", example = "Wheat")
    private String itemName;

    @Schema(description = "Sub-category this item belongs to")
    private CategoryDTO subCategory;

    @Schema(description = "Timestamp when this record was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when this record was last updated")
    private LocalDateTime updatedAt;
}
