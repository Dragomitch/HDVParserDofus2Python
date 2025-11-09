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
 * Entity representing a Dofus game item that can be traded in the auction house.
 * Items are tracked for price history and analysis.
 *
 * @author AGENT-DATA
 * @version 1.0
 * @since Wave 1
 */
@Entity
@Table(name = "items", indexes = {
    @Index(name = "idx_item_gid", columnList = "item_gid")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    /**
     * Primary key, auto-generated.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The Dofus game's unique identifier for this item (GID = Game Item ID).
     * This ID is used in the game's protocol and data structures.
     */
    @Column(name = "item_gid", nullable = false, unique = true)
    private Integer itemGid;

    /**
     * Human-readable name of the item (e.g., "Wheat", "Iron Ore").
     * May be null if name hasn't been discovered yet.
     */
    @Column(name = "item_name", length = 255)
    private String itemName;

    /**
     * The sub-category this item belongs to.
     * Many-to-one relationship with lazy loading.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id")
    private SubCategory subCategory;

    /**
     * Historical price entries for this item.
     * One-to-many relationship with cascading deletes and orphan removal.
     */
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PriceEntry> prices = new ArrayList<>();

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

    /**
     * Override equals to use business key (itemGid) instead of primary key.
     * This ensures proper entity equality before persistence.
     *
     * @param o the object to compare
     * @return true if objects are equal based on itemGid
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return itemGid != null && itemGid.equals(item.getItemGid());
    }

    /**
     * Override hashCode to use class hash instead of field hash.
     * This ensures consistent hash code for entities.
     *
     * @return hash code based on class
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
