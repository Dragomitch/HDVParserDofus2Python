package com.dofusretro.pricetracker.controller;

import com.dofusretro.pricetracker.dto.CategoryDTO;
import com.dofusretro.pricetracker.dto.ItemDTO;
import com.dofusretro.pricetracker.dto.PagedResponse;
import com.dofusretro.pricetracker.exception.ResourceNotFoundException;
import com.dofusretro.pricetracker.mapper.CategoryMapper;
import com.dofusretro.pricetracker.mapper.ItemMapper;
import com.dofusretro.pricetracker.model.Item;
import com.dofusretro.pricetracker.model.SubCategory;
import com.dofusretro.pricetracker.repository.ItemRepository;
import com.dofusretro.pricetracker.repository.SubCategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for Category-related endpoints.
 * Provides API access to category data and items within categories.
 *
 * @author AGENT-API
 * @version 1.0
 * @since Wave 2
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryController {

    private final SubCategoryRepository subCategoryRepository;
    private final ItemRepository itemRepository;
    private final CategoryMapper categoryMapper;
    private final ItemMapper itemMapper;

    /**
     * Get all categories.
     *
     * @return list of all categories
     */
    @Operation(summary = "Get all categories", description = "Retrieve a list of all item categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved categories",
                    content = @Content(schema = @Schema(implementation = CategoryDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        log.debug("GET /api/v1/categories");

        List<CategoryDTO> categories = subCategoryRepository.findAll(Sort.by("name")).stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(categories);
    }

    /**
     * Get a specific category by ID.
     *
     * @param id the category ID
     * @return the category DTO
     */
    @Operation(summary = "Get category by ID", description = "Retrieve a specific category by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved category",
                    content = @Content(schema = @Schema(implementation = CategoryDTO.class))),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable Long id) {

        log.debug("GET /api/v1/categories/{}", id);

        SubCategory category = subCategoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forCategory(id));

        return ResponseEntity.ok(categoryMapper.toDto(category));
    }

    /**
     * Get all items in a specific category.
     *
     * @param id        the category ID
     * @param page      page number (0-indexed)
     * @param size      page size
     * @param sortBy    field to sort by
     * @param direction sort direction (ASC or DESC)
     * @return paginated list of items in the category
     */
    @Operation(summary = "Get items in category",
            description = "Retrieve a paginated list of items belonging to a specific category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved items",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{id}/items")
    public ResponseEntity<PagedResponse<ItemDTO>> getItemsByCategory(
            @Parameter(description = "Category ID") @PathVariable Long id,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "itemName") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") String direction) {

        log.debug("GET /api/v1/categories/{}/items - page: {}, size: {}", id, page, size);

        // Verify category exists
        SubCategory category = subCategoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forCategory(id));

        // Get all items in category
        List<Item> allItems = itemRepository.findBySubCategoryId(id);

        // Sort items
        if ("itemName".equals(sortBy)) {
            if ("DESC".equalsIgnoreCase(direction)) {
                allItems.sort((a, b) -> {
                    if (a.getItemName() == null) return 1;
                    if (b.getItemName() == null) return -1;
                    return b.getItemName().compareToIgnoreCase(a.getItemName());
                });
            } else {
                allItems.sort((a, b) -> {
                    if (a.getItemName() == null) return 1;
                    if (b.getItemName() == null) return -1;
                    return a.getItemName().compareToIgnoreCase(b.getItemName());
                });
            }
        } else if ("id".equals(sortBy)) {
            if ("DESC".equalsIgnoreCase(direction)) {
                allItems.sort((a, b) -> b.getId().compareTo(a.getId()));
            } else {
                allItems.sort((a, b) -> a.getId().compareTo(b.getId()));
            }
        }

        // Manual pagination
        int start = page * size;
        int end = Math.min(start + size, allItems.size());
        List<Item> paginatedItems = allItems.subList(
                Math.min(start, allItems.size()),
                end
        );

        List<ItemDTO> itemDTOs = paginatedItems.stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) allItems.size() / size);

        PagedResponse<ItemDTO> response = PagedResponse.<ItemDTO>builder()
                .content(itemDTOs)
                .pageNumber(page)
                .pageSize(size)
                .totalElements((long) allItems.size())
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();

        return ResponseEntity.ok(response);
    }
}
