package com.dofusretro.pricetracker.controller;

import com.dofusretro.pricetracker.dto.ItemDTO;
import com.dofusretro.pricetracker.dto.PagedResponse;
import com.dofusretro.pricetracker.dto.PriceEntryDTO;
import com.dofusretro.pricetracker.exception.ResourceNotFoundException;
import com.dofusretro.pricetracker.mapper.ItemMapper;
import com.dofusretro.pricetracker.mapper.PriceEntryMapper;
import com.dofusretro.pricetracker.model.Item;
import com.dofusretro.pricetracker.model.PriceEntry;
import com.dofusretro.pricetracker.repository.ItemRepository;
import com.dofusretro.pricetracker.repository.PriceEntryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for Item-related endpoints.
 * Provides API access to item data and price history.
 *
 * @author AGENT-API
 * @version 1.0
 * @since Wave 2
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
@Tag(name = "Items", description = "Item management and price history endpoints")
public class ItemController {

    private final ItemRepository itemRepository;
    private final PriceEntryRepository priceEntryRepository;
    private final ItemMapper itemMapper;
    private final PriceEntryMapper priceEntryMapper;

    /**
     * Get all items with pagination, sorting, and optional filtering.
     *
     * @param page       page number (0-indexed)
     * @param size       page size
     * @param sortBy     field to sort by
     * @param direction  sort direction (ASC or DESC)
     * @param categoryId optional category filter
     * @param search     optional name search filter
     * @return paginated list of items
     */
    @Operation(summary = "Get all items", description = "Retrieve a paginated list of items with optional filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved items",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PagedResponse<ItemDTO>> getAllItems(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") String direction,
            @Parameter(description = "Filter by category ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Search by item name") @RequestParam(required = false) String search) {

        log.debug("GET /api/v1/items - page: {}, size: {}, categoryId: {}, search: {}",
                page, size, categoryId, search);

        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Item> itemPage;

        if (categoryId != null) {
            itemPage = itemRepository.findAll(pageable)
                    .map(item -> item.getSubCategory() != null && item.getSubCategory().getId().equals(categoryId) ? item : null)
                    .map(item -> item);
            // Filter by category - using simple filtering since we don't have a custom query
            List<Item> filteredItems = itemRepository.findBySubCategoryId(categoryId);
            itemPage = new org.springframework.data.domain.PageImpl<>(
                    filteredItems.subList(
                            Math.min(page * size, filteredItems.size()),
                            Math.min((page + 1) * size, filteredItems.size())
                    ),
                    pageable,
                    filteredItems.size()
            );
        } else if (search != null && !search.isBlank()) {
            // Filter by name search
            List<Item> filteredItems = itemRepository.findByItemNameContainingIgnoreCase(search);
            itemPage = new org.springframework.data.domain.PageImpl<>(
                    filteredItems.subList(
                            Math.min(page * size, filteredItems.size()),
                            Math.min((page + 1) * size, filteredItems.size())
                    ),
                    pageable,
                    filteredItems.size()
            );
        } else {
            itemPage = itemRepository.findAll(pageable);
        }

        List<ItemDTO> itemDTOs = itemPage.getContent().stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());

        PagedResponse<ItemDTO> response = PagedResponse.<ItemDTO>builder()
                .content(itemDTOs)
                .pageNumber(itemPage.getNumber())
                .pageSize(itemPage.getSize())
                .totalElements(itemPage.getTotalElements())
                .totalPages(itemPage.getTotalPages())
                .first(itemPage.isFirst())
                .last(itemPage.isLast())
                .hasNext(itemPage.hasNext())
                .hasPrevious(itemPage.hasPrevious())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific item by ID.
     *
     * @param id the item ID
     * @return the item DTO
     */
    @Operation(summary = "Get item by ID", description = "Retrieve a specific item by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved item",
                    content = @Content(schema = @Schema(implementation = ItemDTO.class))),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getItemById(
            @Parameter(description = "Item ID") @PathVariable Long id) {

        log.debug("GET /api/v1/items/{}", id);

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forItem(id));

        return ResponseEntity.ok(itemMapper.toDto(item));
    }

    /**
     * Get price history for a specific item.
     *
     * @param id        the item ID
     * @param page      page number (0-indexed)
     * @param size      page size
     * @param startDate optional start date filter (ISO format)
     * @param endDate   optional end date filter (ISO format)
     * @return paginated list of price entries
     */
    @Operation(summary = "Get item price history",
            description = "Retrieve paginated price history for a specific item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved price history",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @GetMapping("/{id}/prices")
    public ResponseEntity<PagedResponse<PriceEntryDTO>> getItemPrices(
            @Parameter(description = "Item ID") @PathVariable Long id,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Start date (ISO format)") @RequestParam(required = false) String startDate,
            @Parameter(description = "End date (ISO format)") @RequestParam(required = false) String endDate) {

        log.debug("GET /api/v1/items/{}/prices - page: {}, size: {}", id, page, size);

        // Verify item exists
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forItem(id));

        // Get price entries
        List<PriceEntry> priceEntries;
        if (startDate != null) {
            LocalDateTime start = LocalDateTime.parse(startDate);
            priceEntries = priceEntryRepository.findPriceHistory(id, start);
        } else {
            priceEntries = priceEntryRepository.findByItemOrderByCreatedAtDesc(item);
        }

        // Manual pagination
        int start = page * size;
        int end = Math.min(start + size, priceEntries.size());
        List<PriceEntry> paginatedEntries = priceEntries.subList(
                Math.min(start, priceEntries.size()),
                end
        );

        List<PriceEntryDTO> priceEntryDTOs = paginatedEntries.stream()
                .map(priceEntryMapper::toDto)
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) priceEntries.size() / size);

        PagedResponse<PriceEntryDTO> response = PagedResponse.<PriceEntryDTO>builder()
                .content(priceEntryDTOs)
                .pageNumber(page)
                .pageSize(size)
                .totalElements((long) priceEntries.size())
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();

        return ResponseEntity.ok(response);
    }
}
