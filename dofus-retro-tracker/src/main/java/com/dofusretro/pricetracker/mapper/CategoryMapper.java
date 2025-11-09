package com.dofusretro.pricetracker.mapper;

import com.dofusretro.pricetracker.dto.CategoryDTO;
import com.dofusretro.pricetracker.model.SubCategory;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between SubCategory entity and CategoryDTO.
 *
 * @author AGENT-API
 * @version 1.0
 * @since Wave 2
 */
@Component
public class CategoryMapper {

    /**
     * Converts a SubCategory entity to a CategoryDTO.
     *
     * @param subCategory the entity to convert
     * @return the DTO representation, or null if input is null
     */
    public CategoryDTO toDto(SubCategory subCategory) {
        if (subCategory == null) {
            return null;
        }

        return CategoryDTO.builder()
                .id(subCategory.getId())
                .dofusId(subCategory.getDofusId())
                .name(subCategory.getName())
                .createdAt(subCategory.getCreatedAt())
                .updatedAt(subCategory.getUpdatedAt())
                .itemCount(subCategory.getItems() != null ? (long) subCategory.getItems().size() : null)
                .build();
    }

    /**
     * Converts a SubCategory entity to a CategoryDTO without item count.
     * Useful when SubCategory is lazy-loaded and we don't want to trigger a query.
     *
     * @param subCategory the entity to convert
     * @return the DTO representation, or null if input is null
     */
    public CategoryDTO toDtoWithoutItems(SubCategory subCategory) {
        if (subCategory == null) {
            return null;
        }

        return CategoryDTO.builder()
                .id(subCategory.getId())
                .dofusId(subCategory.getDofusId())
                .name(subCategory.getName())
                .createdAt(subCategory.getCreatedAt())
                .updatedAt(subCategory.getUpdatedAt())
                .build();
    }
}
