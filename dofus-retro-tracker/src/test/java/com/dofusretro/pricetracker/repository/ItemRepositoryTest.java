package com.dofusretro.pricetracker.repository;

import com.dofusretro.pricetracker.model.Item;
import com.dofusretro.pricetracker.model.SubCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for Item entity.
 * Tests CRUD operations and custom query methods.
 *
 * @author AGENT-DATA
 * @version 1.0
 * @since Wave 1
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false"
})
@DisplayName("Item Repository Tests")
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private SubCategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private SubCategory category;
    private Item testItem;

    @BeforeEach
    void setUp() {
        category = categoryRepository.save(
            SubCategory.builder().dofusId(48).name("Cereals").build()
        );

        testItem = Item.builder()
            .itemGid(289)
            .itemName("Wheat")
            .subCategory(category)
            .build();
    }

    @Test
    @DisplayName("Should save item successfully")
    void shouldSaveItem() {
        // When
        Item saved = itemRepository.save(testItem);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getItemGid()).isEqualTo(289);
        assertThat(saved.getItemName()).isEqualTo("Wheat");
        assertThat(saved.getSubCategory()).isEqualTo(category);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find item by itemGid")
    void shouldFindByItemGid() {
        // Given
        itemRepository.save(testItem);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Item> found = itemRepository.findByItemGid(289);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getItemName()).isEqualTo("Wheat");
        assertThat(found.get().getItemGid()).isEqualTo(289);
    }

    @Test
    @DisplayName("Should return empty when itemGid not found")
    void shouldReturnEmptyWhenItemGidNotFound() {
        // When
        Optional<Item> found = itemRepository.findByItemGid(999);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check if item exists by itemGid")
    void shouldCheckExistsByItemGid() {
        // Given
        itemRepository.save(testItem);
        entityManager.flush();

        // When & Then
        assertThat(itemRepository.existsByItemGid(289)).isTrue();
        assertThat(itemRepository.existsByItemGid(999)).isFalse();
    }

    @Test
    @DisplayName("Should find items by sub-category")
    void shouldFindBySubCategory() {
        // Given
        itemRepository.save(testItem);
        itemRepository.save(Item.builder()
            .itemGid(290)
            .itemName("Barley")
            .subCategory(category)
            .build());

        SubCategory anotherCategory = categoryRepository.save(
            SubCategory.builder().dofusId(49).name("Fish").build()
        );
        itemRepository.save(Item.builder()
            .itemGid(350)
            .itemName("Trout")
            .subCategory(anotherCategory)
            .build());

        entityManager.flush();
        entityManager.clear();

        // When
        List<Item> cereals = itemRepository.findBySubCategory(category);
        List<Item> fish = itemRepository.findBySubCategory(anotherCategory);

        // Then
        assertThat(cereals).hasSize(2);
        assertThat(fish).hasSize(1);
    }

    @Test
    @DisplayName("Should find items by sub-category ID")
    void shouldFindBySubCategoryId() {
        // Given
        itemRepository.save(testItem);
        itemRepository.save(Item.builder()
            .itemGid(290)
            .itemName("Barley")
            .subCategory(category)
            .build());
        entityManager.flush();
        entityManager.clear();

        // When
        List<Item> items = itemRepository.findBySubCategoryId(category.getId());

        // Then
        assertThat(items).hasSize(2);
    }

    @Test
    @DisplayName("Should find items by name containing (case-insensitive)")
    void shouldFindByItemNameContainingIgnoreCase() {
        // Given
        itemRepository.save(testItem);
        itemRepository.save(Item.builder()
            .itemGid(290)
            .itemName("Barley")
            .subCategory(category)
            .build());
        itemRepository.save(Item.builder()
            .itemGid(291)
            .itemName("Wheat Flour")
            .subCategory(category)
            .build());
        entityManager.flush();
        entityManager.clear();

        // When
        List<Item> wheatResults = itemRepository.findByItemNameContainingIgnoreCase("wheat");
        List<Item> barleyResults = itemRepository.findByItemNameContainingIgnoreCase("BAR");
        List<Item> emptyResults = itemRepository.findByItemNameContainingIgnoreCase("xyz");

        // Then
        assertThat(wheatResults).hasSize(2);
        assertThat(barleyResults).hasSize(1);
        assertThat(emptyResults).isEmpty();
    }

    @Test
    @DisplayName("Should find item by ID with prices (JOIN FETCH)")
    void shouldFindByIdWithPrices() {
        // Given
        Item saved = itemRepository.save(testItem);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Item> found = itemRepository.findByIdWithPrices(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getItemName()).isEqualTo("Wheat");
        // Prices collection should be initialized (even if empty)
        assertThat(found.get().getPrices()).isNotNull();
    }

    @Test
    @DisplayName("Should count items by sub-category ID")
    void shouldCountBySubCategoryId() {
        // Given
        itemRepository.save(testItem);
        itemRepository.save(Item.builder()
            .itemGid(290)
            .itemName("Barley")
            .subCategory(category)
            .build());
        itemRepository.save(Item.builder()
            .itemGid(291)
            .itemName("Oat")
            .subCategory(category)
            .build());
        entityManager.flush();

        // When
        long count = itemRepository.countBySubCategoryId(category.getId());

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Should update item")
    void shouldUpdateItem() {
        // Given
        Item saved = itemRepository.save(testItem);
        entityManager.flush();
        entityManager.clear();

        // When
        saved.setItemName("Updated Wheat");
        Item updated = itemRepository.save(saved);
        entityManager.flush();

        // Then
        assertThat(updated.getItemName()).isEqualTo("Updated Wheat");
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should delete item")
    void shouldDeleteItem() {
        // Given
        Item saved = itemRepository.save(testItem);
        entityManager.flush();
        Long id = saved.getId();

        // When
        itemRepository.delete(saved);
        entityManager.flush();

        // Then
        assertThat(itemRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Should set category to null when category is deleted (ON DELETE SET NULL)")
    void shouldSetCategoryToNullWhenCategoryDeleted() {
        // Given
        Item saved = itemRepository.save(testItem);
        entityManager.flush();
        Long itemId = saved.getId();
        Long categoryId = category.getId();

        // When
        categoryRepository.deleteById(categoryId);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Item> found = itemRepository.findById(itemId);
        assertThat(found).isPresent();
        assertThat(found.get().getSubCategory()).isNull();
    }

    @Test
    @DisplayName("Should maintain unique constraint on itemGid")
    void shouldMaintainUniqueItemGid() {
        // Given
        itemRepository.save(testItem);
        entityManager.flush();

        // When - Try to save another item with the same itemGid
        Item duplicate = Item.builder()
            .itemGid(289)  // Same itemGid
            .itemName("Duplicate Wheat")
            .subCategory(category)
            .build();

        // Then - Should throw exception due to unique constraint
        try {
            itemRepository.save(duplicate);
            entityManager.flush();
            assertThat(false).as("Should have thrown exception").isTrue();
        } catch (Exception e) {
            // Expected - unique constraint violation
            assertThat(e).isNotNull();
        }
    }

    @Test
    @DisplayName("Should use equals and hashCode based on itemGid")
    void shouldUseEqualsAndHashCodeBasedOnItemGid() {
        // Given
        Item item1 = Item.builder().itemGid(289).itemName("Wheat").build();
        Item item2 = Item.builder().itemGid(289).itemName("Wheat Copy").build();
        Item item3 = Item.builder().itemGid(290).itemName("Barley").build();

        // When & Then
        assertThat(item1).isEqualTo(item2);  // Same itemGid
        assertThat(item1).isNotEqualTo(item3);  // Different itemGid
        assertThat(item1.hashCode()).isEqualTo(item2.hashCode());
    }
}
