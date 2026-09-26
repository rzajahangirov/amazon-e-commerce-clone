package com.amazon.repository;

import com.amazon.entity.WishlistItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link WishlistItem} entities.
 * Uses @EntityGraph to eliminate N+1 queries and batch queries for isFavorited checks.
 */
@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, UUID> {

    boolean existsByUserIdAndProductId(UUID userId, UUID productId);

    Optional<WishlistItem> findByUserIdAndProductId(UUID userId, UUID productId);

    @EntityGraph(attributePaths = {"product", "product.category", "product.seller", "product.brand", "product.variants"})
    Page<WishlistItem> findByUserId(UUID userId, Pageable pageable);

    /**
     * Batch query to check which product IDs from a given collection are in the user's wishlist.
     * Prevents N+1 when populating isFavorited across paginated product lists.
     */
    @Query("SELECT w.product.id FROM WishlistItem w WHERE w.user.id = :userId AND w.product.id IN :productIds")
    Set<UUID> findFavoritedProductIdsByUserIdAndProductIdIn(
            @Param("userId") UUID userId,
            @Param("productIds") Collection<UUID> productIds);

    @Modifying
    @Query("DELETE FROM WishlistItem w WHERE w.user.id = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);

    long countByUserId(UUID userId);
}
