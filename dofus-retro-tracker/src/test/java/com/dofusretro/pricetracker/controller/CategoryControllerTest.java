package com.dofusretro.pricetracker.controller;

import com.dofusretro.pricetracker.dto.CategoryDTO;
import com.dofusretro.pricetracker.mapper.CategoryMapper;
import com.dofusretro.pricetracker.mapper.ItemMapper;
import com.dofusretro.pricetracker.model.SubCategory;
import com.dofusretro.pricetracker.repository.ItemRepository;
import com.dofusretro.pricetracker.repository.SubCategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for CategoryController using @WebMvcTest.
 *
 * @author AGENT-API
 * @version 1.0
 * @since Wave 2
 */
@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubCategoryRepository subCategoryRepository;

    @MockBean
    private ItemRepository itemRepository;

    @MockBean
    private CategoryMapper categoryMapper;

    @MockBean
    private ItemMapper itemMapper;

    @Test
    void getAllCategories_ShouldReturnAllCategories() throws Exception {
        // Given
        SubCategory category1 = SubCategory.builder().id(1L).dofusId(100).name("Cereals").build();
        SubCategory category2 = SubCategory.builder().id(2L).dofusId(101).name("Fish").build();
        List<SubCategory> categories = Arrays.asList(category1, category2);

        CategoryDTO dto1 = CategoryDTO.builder().id(1L).dofusId(100).name("Cereals").build();
        CategoryDTO dto2 = CategoryDTO.builder().id(2L).dofusId(101).name("Fish").build();

        when(subCategoryRepository.findAll(any(Sort.class))).thenReturn(categories);
        when(categoryMapper.toDto(category1)).thenReturn(dto1);
        when(categoryMapper.toDto(category2)).thenReturn(dto2);

        // When & Then
        mockMvc.perform(get("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Cereals"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Fish"));
    }

    @Test
    void getCategoryById_WhenCategoryExists_ShouldReturnCategory() throws Exception {
        // Given
        Long categoryId = 1L;
        SubCategory category = SubCategory.builder().id(categoryId).dofusId(100).name("Cereals").build();
        CategoryDTO categoryDTO = CategoryDTO.builder().id(categoryId).dofusId(100).name("Cereals").build();

        when(subCategoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryMapper.toDto(category)).thenReturn(categoryDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.name").value("Cereals"));
    }

    @Test
    void getCategoryById_WhenCategoryNotFound_ShouldReturn404() throws Exception {
        // Given
        Long categoryId = 999L;
        when(subCategoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void getItemsByCategory_ShouldReturnPagedItems() throws Exception {
        // Given
        Long categoryId = 1L;
        SubCategory category = SubCategory.builder().id(categoryId).dofusId(100).name("Cereals").build();

        when(subCategoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(itemRepository.findBySubCategoryId(categoryId)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/categories/{id}/items", categoryId)
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageNumber").value(0));
    }
}
