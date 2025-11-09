package com.dofusretro.pricetracker.repository;

import com.dofusretro.pricetracker.model.Item;
import com.dofusretro.pricetracker.model.PriceEntry;
import com.dofusretro.pricetracker.model.SubCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for PriceEntry entity.
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
@DisplayName("PriceEntry Repository Tests")
class PriceEntryRepositoryTest {

    @Autowired
    private PriceEntryRepository priceRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private SubCategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Item item;

    @BeforeEach
    void setUp() {
        SubCategory category = categoryRepository.save(
            SubCategory.builder().dofusId(48).name("Cereals").build()
        );

        item = itemRepository.save(
            Item.builder()
                .itemGid(289)
                .itemName("Wheat")
                .subCategory(category)
                .build()
        );
    }

    @Test
    @DisplayName("Should save price entry successfully")
    void shouldSavePriceEntry() {
        // Given
        PriceEntry entry = PriceEntry.builder()
            .item(item)
            .price(15000L)
            .quantity(1)
            .serverTimestamp(System.currentTimeMillis())
            .build();

        // When
        PriceEntry saved = priceRepository.save(entry);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPrice()).isEqualTo(15000L);
        assertThat(saved.getQuantity()).isEqualTo(1);
        assertThat(saved.getItem()).isEqualTo(item);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getServerTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should find price entries by item ordered by createdAt DESC")
    void shouldFindByItemOrderByCreatedAtDesc() throws InterruptedException {
        // Given
        priceRepository.save(PriceEntry.builder()
            .item(item).price(15000L).quantity(1).build());
        Thread.sleep(10); // Small delay to ensure different timestamps
        priceRepository.save(PriceEntry.builder()
            .item(item).price(16000L).quantity(1).build());
        Thread.sleep(10);
        priceRepository.save(PriceEntry.builder()
            .item(item).price(14000L).quantity(1).build());
        entityManager.flush();
        entityManager.clear();

        // When
        List<PriceEntry> entries = priceRepository.findByItemOrderByCreatedAtDesc(item);

        // Then
        assertThat(entries).hasSize(3);
        assertThat(entries.get(0).getPrice()).isEqualTo(14000L);  // Most recent
        assertThat(entries.get(2).getPrice()).isEqualTo(15000L);  // Oldest
        assertThat(entries).isSortedAccordingTo(
            Comparator.comparing(PriceEntry::getCreatedAt).reversed()
        );
    }

    @Test
    @DisplayName("Should find price entries by item and quantity")
    void shouldFindByItemAndQuantity() {
        // Given
        priceRepository.save(PriceEntry.builder()
            .item(item).price(15000L).quantity(1).build());
        priceRepository.save(PriceEntry.builder()
            .item(item).price(140000L).quantity(10).build());
        priceRepository.save(PriceEntry.builder()
            .item(item).price(16000L).quantity(1).build());
        priceRepository.save(PriceEntry.builder()
            .item(item).price(1300000L).quantity(100).build());
        entityManager.flush();
        entityManager.clear();

        // When
        List<PriceEntry> quantity1 = priceRepository.findByItemAndQuantity(item, 1);
        List<PriceEntry> quantity10 = priceRepository.findByItemAndQuantity(item, 10);
        List<PriceEntry> quantity100 = priceRepository.findByItemAndQuantity(item, 100);

        // Then
        assertThat(quantity1).hasSize(2);
        assertThat(quantity10).hasSize(1);
        assertThat(quantity100).hasSize(1);
    }

    @Test
    @DisplayName("Should find price history since a start date")
    void shouldFindPriceHistory() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);

        for (int i = 0; i < 5; i++) {
            priceRepository.save(PriceEntry.builder()
                .item(item)
                .price(15000L + (i * 1000))
                .quantity(1)
                .build());
        }
        entityManager.flush();
        entityManager.clear();

        // When
        List<PriceEntry> history = priceRepository.findPriceHistory(
            item.getId(), startDate
        );

        // Then
        assertThat(history).hasSize(5);
        assertThat(history).isSortedAccordingTo(
            Comparator.comparing(PriceEntry::getCreatedAt)
        );
    }

    @Test
    @DisplayName("Should find price history by quantity within date range")
    void shouldFindPriceHistoryByQuantity() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // Add prices for different quantities
        priceRepository.save(PriceEntry.builder()
            .item(item).price(15000L).quantity(1).build());
        priceRepository.save(PriceEntry.builder()
            .item(item).price(140000L).quantity(10).build());
        priceRepository.save(PriceEntry.builder()
            .item(item).price(16000L).quantity(1).build());
        priceRepository.save(PriceEntry.builder()
            .item(item).price(145000L).quantity(10).build());
        entityManager.flush();
        entityManager.clear();

        // When
        List<PriceEntry> history = priceRepository.findPriceHistoryByQuantity(
            item.getId(), 10, startDate, endDate
        );

        // Then
        assertThat(history).hasSize(2);
        assertThat(history).allMatch(p -> p.getQuantity() == 10);
        assertThat(history).isSortedAccordingTo(
            Comparator.comparing(PriceEntry::getCreatedAt)
        );
    }

    @Test
    @DisplayName("Should count recent entries since a timestamp")
    void shouldCountRecentEntries() {
        // Given
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        // Add some entries
        for (int i = 0; i < 3; i++) {
            priceRepository.save(PriceEntry.builder()
                .item(item)
                .price(15000L + (i * 1000))
                .quantity(1)
                .build());
        }
        entityManager.flush();

        // When
        long count = priceRepository.countRecentEntries(item.getId(), oneHourAgo);

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Should delete entries before a specific date")
    void shouldDeleteByCreatedAtBefore() {
        // Given
        // This test is tricky with auto-generated timestamps
        // We'll save entries and then delete old ones
        for (int i = 0; i < 3; i++) {
            priceRepository.save(PriceEntry.builder()
                .item(item)
                .price(15000L)
                .quantity(1)
                .build());
        }
        entityManager.flush();

        // When
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);
        priceRepository.deleteByCreatedAtBefore(futureDate);
        entityManager.flush();

        // Then
        List<PriceEntry> remaining = priceRepository.findAll();
        assertThat(remaining).isEmpty();
    }

    @Test
    @DisplayName("Should cascade delete price entries when item is deleted")
    void shouldCascadeDeleteWhenItemDeleted() {
        // Given
        priceRepository.save(PriceEntry.builder()
            .item(item).price(15000L).quantity(1).build());
        priceRepository.save(PriceEntry.builder()
            .item(item).price(140000L).quantity(10).build());
        entityManager.flush();

        long priceCount = priceRepository.count();
        assertThat(priceCount).isEqualTo(2);

        // When
        itemRepository.delete(item);
        entityManager.flush();
        entityManager.clear();

        // Then
        long remainingPrices = priceRepository.count();
        assertThat(remainingPrices).isZero();
    }

    @Test
    @DisplayName("Should format price correctly")
    void shouldFormatPrice() {
        // Given
        PriceEntry entry = PriceEntry.builder()
            .item(item)
            .price(15000L)
            .quantity(1)
            .build();

        // When
        String formatted = entry.getFormattedPrice();

        // Then
        assertThat(formatted).isEqualTo("15 K");
    }

    @Test
    @DisplayName("Should save price entries with all valid quantities")
    void shouldSavePriceEntriesWithAllValidQuantities() {
        // Given
        PriceEntry entry1 = PriceEntry.builder()
            .item(item).price(15000L).quantity(1).build();
        PriceEntry entry10 = PriceEntry.builder()
            .item(item).price(140000L).quantity(10).build();
        PriceEntry entry100 = PriceEntry.builder()
            .item(item).price(1300000L).quantity(100).build();

        // When
        priceRepository.save(entry1);
        priceRepository.save(entry10);
        priceRepository.save(entry100);
        entityManager.flush();

        // Then
        List<PriceEntry> all = priceRepository.findAll();
        assertThat(all).hasSize(3);
        assertThat(all).extracting(PriceEntry::getQuantity)
            .containsExactlyInAnyOrder(1, 10, 100);
    }

    @Test
    @DisplayName("Should handle null server timestamp")
    void shouldHandleNullServerTimestamp() {
        // Given
        PriceEntry entry = PriceEntry.builder()
            .item(item)
            .price(15000L)
            .quantity(1)
            .serverTimestamp(null)  // Null timestamp
            .build();

        // When
        PriceEntry saved = priceRepository.save(entry);

        // Then
        assertThat(saved.getServerTimestamp()).isNull();
    }

    @Test
    @DisplayName("Should find price history excluding old data")
    void shouldFindPriceHistoryExcludingOldData() {
        // Given
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        // Add recent entries
        for (int i = 0; i < 3; i++) {
            priceRepository.save(PriceEntry.builder()
                .item(item)
                .price(15000L + (i * 1000))
                .quantity(1)
                .build());
        }
        entityManager.flush();

        // When - Query for data from 7 days ago
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<PriceEntry> history = priceRepository.findPriceHistory(
            item.getId(), sevenDaysAgo
        );

        // Then - Should find all entries since they're all recent
        assertThat(history).hasSize(3);

        // When - Query for data from the future
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);
        List<PriceEntry> emptyHistory = priceRepository.findPriceHistory(
            item.getId(), futureDate
        );

        // Then - Should find nothing
        assertThat(emptyHistory).isEmpty();
    }
}
