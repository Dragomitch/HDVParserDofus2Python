package com.dofusretro.pricetracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a Dofus item sub-category.
 * Sub-categories group related items (e.g., Cereals, Fish, Minerals).
 *
 * @author AGENT-DATA
 * @version 1.0
 * @since Wave 1
 */
@Entity
@Table(name = "sub_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubCategory {

    /**
     * Primary key, auto-generated.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The Dofus game's unique identifier for this sub-category.
     * This ID comes from the game's category system.
     */
    @Column(name = "dofus_id", unique = true, nullable = false)
    private Integer dofusId;

    /**
     * Human-readable name of the sub-category (e.g., "Cereals", "Fish").
     */
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    /**
     * All items that belong to this sub-category.
     * One-to-many relationship with cascading operations.
     */
    @OneToMany(mappedBy = "subCategory", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Item> items = new ArrayList<>();

    /**
     * Timestamp when this record was created.
     * Automatically set on insert.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when this record was last updated.
     * Automatically updated on modification.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
