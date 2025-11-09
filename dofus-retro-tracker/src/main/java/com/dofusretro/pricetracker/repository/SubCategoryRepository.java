package com.dofusretro.pricetracker.repository;

import com.dofusretro.pricetracker.model.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for SubCategory entity operations.
 * Provides CRUD operations and custom query methods for sub-categories.
 *
 * @author AGENT-DATA
 * @version 1.0
 * @since Wave 1
 */
@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {

    /**
     * Find a sub-category by its Dofus ID.
     *
     * @param dofusId the Dofus game's unique identifier for the sub-category
     * @return Optional containing the sub-category if found
     */
    Optional<SubCategory> findByDofusId(Integer dofusId);

    /**
     * Search for sub-categories by name (case-insensitive partial match).
     *
     * @param name the name or partial name to search for
     * @return list of matching sub-categories
     */
    List<SubCategory> findByNameContainingIgnoreCase(String name);

    /**
     * Check if a sub-category exists with the given Dofus ID.
     *
     * @param dofusId the Dofus ID to check
     * @return true if a sub-category with this ID exists
     */
    boolean existsByDofusId(Integer dofusId);
}
