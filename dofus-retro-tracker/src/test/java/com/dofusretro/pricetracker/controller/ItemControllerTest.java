package com.dofusretro.pricetracker.controller;

import com.dofusretro.pricetracker.dto.ItemDTO;
import com.dofusretro.pricetracker.exception.ResourceNotFoundException;
import com.dofusretro.pricetracker.mapper.ItemMapper;
import com.dofusretro.pricetracker.mapper.PriceEntryMapper;
import com.dofusretro.pricetracker.model.Item;
import com.dofusretro.pricetracker.model.PriceEntry;
import com.dofusretro.pricetracker.repository.ItemRepository;
import com.dofusretro.pricetracker.repository.PriceEntryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ItemController using @WebMvcTest.
 *
 * @author AGENT-API
 * @version 1.0
 * @since Wave 2
 */
@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRepository itemRepository;

    @MockBean
    private PriceEntryRepository priceEntryRepository;

    @MockBean
    private ItemMapper itemMapper;

    @MockBean
    private PriceEntryMapper priceEntryMapper;

    @Test
    void getAllItems_ShouldReturnPagedItems() throws Exception {
        // Given
        Item item1 = Item.builder().id(1L).itemGid(100).itemName("Wheat").build();
        Item item2 = Item.builder().id(2L).itemGid(101).itemName("Barley").build();
        List<Item> items = Arrays.asList(item1, item2);
        Page<Item> itemPage = new PageImpl<>(items);

        ItemDTO dto1 = ItemDTO.builder().id(1L).itemGid(100).itemName("Wheat").build();
        ItemDTO dto2 = ItemDTO.builder().id(2L).itemGid(101).itemName("Barley").build();

        when(itemRepository.findAll(any(Pageable.class))).thenReturn(itemPage);
        when(itemMapper.toDto(item1)).thenReturn(dto1);
        when(itemMapper.toDto(item2)).thenReturn(dto2);

        // When & Then
        mockMvc.perform(get("/api/v1/items")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].itemName").value("Wheat"))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").exists())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    @Test
    void getItemById_WhenItemExists_ShouldReturnItem() throws Exception {
        // Given
        Long itemId = 1L;
        Item item = Item.builder().id(itemId).itemGid(100).itemName("Wheat").build();
        ItemDTO itemDTO = ItemDTO.builder().id(itemId).itemGid(100).itemName("Wheat").build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(itemDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/items/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.itemName").value("Wheat"));
    }

    @Test
    void getItemById_WhenItemNotFound_ShouldReturn404() throws Exception {
        // Given
        Long itemId = 999L;
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/items/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void getItemPrices_ShouldReturnPriceHistory() throws Exception {
        // Given
        Long itemId = 1L;
        Item item = Item.builder().id(itemId).itemGid(100).itemName("Wheat").build();
        PriceEntry price1 = PriceEntry.builder()
                .id(1L)
                .item(item)
                .price(1000L)
                .quantity(1)
                .createdAt(LocalDateTime.now())
                .build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(priceEntryRepository.findByItemOrderByCreatedAtDesc(item)).thenReturn(Arrays.asList(price1));

        // When & Then
        mockMvc.perform(get("/api/v1/items/{id}/prices", itemId)
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageNumber").value(0));
    }
}
