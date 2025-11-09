package com.dofusretro.pricetracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a single price observation for an item at a specific quantity.
 * Each entry captures the price at which an item was listed in the auction house.
 *
 * The Dofus auction house shows prices for quantities of 1, 10, and 100 units.
 * Each price entry captures one of these data points.
 *
 * @author AGENT-DATA
 * @version 1.0
 * @since Wave 1
 */
@Entity
@Table(name = "price_entries", indexes = {
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_item_quantity", columnList = "item_id, quantity")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceEntry {

    /**
     * Primary key, auto-generated.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The item this price entry is for.
     * Many-to-one relationship with lazy loading.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    /**
     * The observed price in Dofus kamas (game currency).
     * Stored as a long to handle large values.
     */
    @Column(name = "price", nullable = false)
    private Long price;

    /**
     * The quantity this price is for.
     * Valid values are 1, 10, or 100 (enforced by database constraint).
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * Timestamp when this price entry was captured.
     * Automatically set on insert.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Server timestamp from the game server (milliseconds since epoch).
     * This is the timestamp from the Dofus game server when the price was observed.
     * Optional field, may be null if not available.
     */
    @Column(name = "server_timestamp")
    private Long serverTimestamp;

    /**
     * Helper method to format price in a human-readable format.
     * Converts price to kamas (K) for display purposes.
     *
     * @return formatted price string (e.g., "15 K" for 15000)
     */
    public String getFormattedPrice() {
        return String.format("%,d K", price / 1000);
    }
}
