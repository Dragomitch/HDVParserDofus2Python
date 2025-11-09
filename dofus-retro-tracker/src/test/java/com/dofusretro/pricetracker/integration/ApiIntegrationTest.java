package com.dofusretro.pricetracker.integration;

import com.dofusretro.pricetracker.model.Item;
import com.dofusretro.pricetracker.model.PriceEntry;
import com.dofusretro.pricetracker.model.SubCategory;
import com.dofusretro.pricetracker.repository.ItemRepository;
import com.dofusretro.pricetracker.repository.PriceEntryRepository;
import com.dofusretro.pricetracker.repository.SubCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for REST API endpoints using @SpringBootTest.
 * Tests the full application stack including database integration.
 *
 * @author AGENT-API
 * @version 1.0
 * @since Wave 2
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Autowired
    private PriceEntryRepository priceEntryRepository;

    private SubCategory testCategory;
    private Item testItem;

    @BeforeEach
    void setUp() {
        // Clean up
        priceEntryRepository.deleteAll();
        itemRepository.deleteAll();
        subCategoryRepository.deleteAll();

        // Create test data
        testCategory = SubCategory.builder()
                .dofusId(100)
                .name("Cereals")
                .build();
        testCategory = subCategoryRepository.save(testCategory);

        testItem = Item.builder()
                .itemGid(289)
                .itemName("Wheat")
                .subCategory(testCategory)
                .build();
        testItem = itemRepository.save(testItem);

        PriceEntry priceEntry = PriceEntry.builder()
                .item(testItem)
                .price(1000L)
                .quantity(1)
                .serverTimestamp(System.currentTimeMillis())
                .build();
        priceEntryRepository.save(priceEntry);
    }

    @Test
    void testGetAllItems_ReturnsPagedItems() throws Exception {
        mockMvc.perform(get("/api/v1/items")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].itemName", is("Wheat")))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)));
    }

    @Test
    void testGetItemById_ReturnsItem() throws Exception {
        mockMvc.perform(get("/api/v1/items/{id}", testItem.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testItem.getId().intValue())))
                .andExpect(jsonPath("$.itemName", is("Wheat")))
                .andExpect(jsonPath("$.itemGid", is(289)));
    }

    @Test
    void testGetItemById_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/items/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    void testGetItemPrices_ReturnsPriceHistory() throws Exception {
        mockMvc.perform(get("/api/v1/items/{id}/prices", testItem.getId())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].price", is(1000)))
                .andExpect(jsonPath("$.content[0].quantity", is(1)));
    }

    @Test
    void testGetAllCategories_ReturnsAllCategories() throws Exception {
        mockMvc.perform(get("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name", is("Cereals")))
                .andExpect(jsonPath("$[0].dofusId", is(100)));
    }

    @Test
    void testGetCategoryById_ReturnsCategory() throws Exception {
        mockMvc.perform(get("/api/v1/categories/{id}", testCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testCategory.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Cereals")));
    }

    @Test
    void testGetItemsByCategory_ReturnsPagedItems() throws Exception {
        mockMvc.perform(get("/api/v1/categories/{id}/items", testCategory.getId())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].itemName", is("Wheat")));
    }

    @Test
    void testGetAllItems_WithCategoryFilter_ReturnsFilteredItems() throws Exception {
        mockMvc.perform(get("/api/v1/items")
                        .param("categoryId", testCategory.getId().toString())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].itemName", is("Wheat")));
    }

    @Test
    void testGetAllItems_WithSearchFilter_ReturnsMatchingItems() throws Exception {
        mockMvc.perform(get("/api/v1/items")
                        .param("search", "wheat")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].itemName", is("Wheat")));
    }

    @Test
    void testGetHealth_ReturnsHealthStatus() throws Exception {
        mockMvc.perform(get("/api/v1/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", notNullValue()));
    }
}
