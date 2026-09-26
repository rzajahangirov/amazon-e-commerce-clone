package com.amazon.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * WishlistItem entity representing a user's favorited product.
 * Maps the User-to-Product many-to-many relationship with a unique constraint
 * preventing duplicate entries per user per product.
 * Adheres to Senior Backend Developer Guidelines Section 5.
 */
@Entity
@Table(name = "wishlist_items", indexes = {
    @Index(name = "idx_wishlist_user_id", columnList = "user_id"),
    @Index(name = "idx_wishlist_product_id", columnList = "product_id"),
    @Index(name = "idx_wishlist_user_product_unique", columnList = "user_id, product_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "product"})
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WishlistItem other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
