package com.dofusretro.pricetracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for SubCategory entity.
 * Note: Named CategoryDTO for API simplicity, but represents SubCategory entity.
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
@Schema(description = "Represents a Dofus item sub-category grouping related items")
public class CategoryDTO {

    @Schema(description = "Unique identifier", example = "1")
    private Long id;

    @Schema(description = "Dofus game category identifier", example = "100")
    private Integer dofusId;

    @Schema(description = "Category name", example = "Cereals")
    private String name;

    @Schema(description = "Timestamp when this record was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when this record was last updated")
    private LocalDateTime updatedAt;

    @Schema(description = "Number of items in this category (optional)")
    private Long itemCount;
}
