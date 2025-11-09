package com.dofusretro.pricetracker.repository;

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
 * Repository tests for SubCategory entity.
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
@DisplayName("SubCategory Repository Tests")
class SubCategoryRepositoryTest {

    @Autowired
    private SubCategoryRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private SubCategory testCategory;

    @BeforeEach
    void setUp() {
        testCategory = SubCategory.builder()
            .dofusId(48)
            .name("Cereals")
            .build();
    }

    @Test
    @DisplayName("Should save sub-category successfully")
    void shouldSaveSubCategory() {
        // When
        SubCategory saved = repository.save(testCategory);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDofusId()).isEqualTo(48);
        assertThat(saved.getName()).isEqualTo("Cereals");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find sub-category by Dofus ID")
    void shouldFindByDofusId() {
        // Given
        repository.save(testCategory);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<SubCategory> found = repository.findByDofusId(48);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Cereals");
        assertThat(found.get().getDofusId()).isEqualTo(48);
    }

    @Test
    @DisplayName("Should return empty when Dofus ID not found")
    void shouldReturnEmptyWhenDofusIdNotFound() {
        // When
        Optional<SubCategory> found = repository.findByDofusId(999);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check if sub-category exists by Dofus ID")
    void shouldCheckExistsByDofusId() {
        // Given
        repository.save(testCategory);
        entityManager.flush();

        // When & Then
        assertThat(repository.existsByDofusId(48)).isTrue();
        assertThat(repository.existsByDofusId(999)).isFalse();
    }

    @Test
    @DisplayName("Should find sub-categories by name containing (case-insensitive)")
    void shouldFindByNameContainingIgnoreCase() {
        // Given
        repository.save(testCategory);
        repository.save(SubCategory.builder().dofusId(49).name("Fish").build());
        repository.save(SubCategory.builder().dofusId(50).name("Precious Minerals").build());
        entityManager.flush();
        entityManager.clear();

        // When
        List<SubCategory> cerealsResults = repository.findByNameContainingIgnoreCase("cere");
        List<SubCategory> mineralsResults = repository.findByNameContainingIgnoreCase("MINERAL");
        List<SubCategory> emptyResults = repository.findByNameContainingIgnoreCase("xyz");

        // Then
        assertThat(cerealsResults).hasSize(1);
        assertThat(cerealsResults.get(0).getName()).isEqualTo("Cereals");

        assertThat(mineralsResults).hasSize(1);
        assertThat(mineralsResults.get(0).getName()).isEqualTo("Precious Minerals");

        assertThat(emptyResults).isEmpty();
    }

    @Test
    @DisplayName("Should find all sub-categories")
    void shouldFindAllSubCategories() {
        // Given
        repository.save(testCategory);
        repository.save(SubCategory.builder().dofusId(49).name("Fish").build());
        repository.save(SubCategory.builder().dofusId(50).name("Minerals").build());
        entityManager.flush();

        // When
        List<SubCategory> all = repository.findAll();

        // Then
        assertThat(all).hasSize(3);
    }

    @Test
    @DisplayName("Should update sub-category")
    void shouldUpdateSubCategory() {
        // Given
        SubCategory saved = repository.save(testCategory);
        entityManager.flush();
        entityManager.clear();

        // When
        saved.setName("Updated Cereals");
        SubCategory updated = repository.save(saved);
        entityManager.flush();

        // Then
        assertThat(updated.getName()).isEqualTo("Updated Cereals");
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should delete sub-category")
    void shouldDeleteSubCategory() {
        // Given
        SubCategory saved = repository.save(testCategory);
        entityManager.flush();
        Long id = saved.getId();

        // When
        repository.delete(saved);
        entityManager.flush();

        // Then
        assertThat(repository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Should maintain unique constraint on Dofus ID")
    void shouldMaintainUniqueDofusId() {
        // Given
        repository.save(testCategory);
        entityManager.flush();

        // When - Try to save another category with the same Dofus ID
        SubCategory duplicate = SubCategory.builder()
            .dofusId(48)  // Same Dofus ID
            .name("Duplicate")
            .build();

        // Then - Should throw exception due to unique constraint
        try {
            repository.save(duplicate);
            entityManager.flush();
            assertThat(false).as("Should have thrown exception").isTrue();
        } catch (Exception e) {
            // Expected - unique constraint violation
            assertThat(e).isNotNull();
        }
    }
}
