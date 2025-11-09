package com.dofusretro.pricetracker.mapper;

import com.dofusretro.pricetracker.dto.ItemDTO;
import com.dofusretro.pricetracker.model.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Item entity and ItemDTO.
 *
 * @author AGENT-API
 * @version 1.0
 * @since Wave 2
 */
@Component
@RequiredArgsConstructor
public class ItemMapper {

    private final CategoryMapper categoryMapper;

    /**
     * Converts an Item entity to an ItemDTO.
     *
     * @param item the entity to convert
     * @return the DTO representation, or null if input is null
     */
    public ItemDTO toDto(Item item) {
        if (item == null) {
            return null;
        }

        return ItemDTO.builder()
                .id(item.getId())
                .itemGid(item.getItemGid())
                .itemName(item.getItemName())
                .subCategory(categoryMapper.toDtoWithoutItems(item.getSubCategory()))
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    /**
     * Converts an Item entity to an ItemDTO without category.
     * Useful when Item is lazy-loaded and we don't want to trigger a query.
     *
     * @param item the entity to convert
     * @return the DTO representation, or null if input is null
     */
    public ItemDTO toDtoWithoutCategory(Item item) {
        if (item == null) {
            return null;
        }

        return ItemDTO.builder()
                .id(item.getId())
                .itemGid(item.getItemGid())
                .itemName(item.getItemName())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
