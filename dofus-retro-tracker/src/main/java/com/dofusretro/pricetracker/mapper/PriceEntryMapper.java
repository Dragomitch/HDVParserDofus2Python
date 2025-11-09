package com.dofusretro.pricetracker.mapper;

import com.dofusretro.pricetracker.dto.PriceEntryDTO;
import com.dofusretro.pricetracker.model.PriceEntry;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between PriceEntry entity and PriceEntryDTO.
 *
 * @author AGENT-API
 * @version 1.0
 * @since Wave 2
 */
@Component
public class PriceEntryMapper {

    /**
     * Converts a PriceEntry entity to a PriceEntryDTO.
     *
     * @param priceEntry the entity to convert
     * @return the DTO representation, or null if input is null
     */
    public PriceEntryDTO toDto(PriceEntry priceEntry) {
        if (priceEntry == null) {
            return null;
        }

        return PriceEntryDTO.builder()
                .id(priceEntry.getId())
                .itemId(priceEntry.getItem() != null ? priceEntry.getItem().getId() : null)
                .itemName(priceEntry.getItem() != null ? priceEntry.getItem().getItemName() : null)
                .price(priceEntry.getPrice())
                .quantity(priceEntry.getQuantity())
                .createdAt(priceEntry.getCreatedAt())
                .serverTimestamp(priceEntry.getServerTimestamp())
                .formattedPrice(priceEntry.getFormattedPrice())
                .build();
    }

    /**
     * Converts a PriceEntry entity to a PriceEntryDTO without item information.
     * Useful when we want to avoid triggering lazy loading.
     *
     * @param priceEntry the entity to convert
     * @return the DTO representation, or null if input is null
     */
    public PriceEntryDTO toDtoWithoutItem(PriceEntry priceEntry) {
        if (priceEntry == null) {
            return null;
        }

        return PriceEntryDTO.builder()
                .id(priceEntry.getId())
                .price(priceEntry.getPrice())
                .quantity(priceEntry.getQuantity())
                .createdAt(priceEntry.getCreatedAt())
                .serverTimestamp(priceEntry.getServerTimestamp())
                .formattedPrice(priceEntry.getFormattedPrice())
                .build();
    }
}
