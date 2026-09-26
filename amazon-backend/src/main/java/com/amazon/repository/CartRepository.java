package com.amazon.repository;

import com.amazon.entity.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Cart} entities.
 * Includes @EntityGraph methods to avoid N+1 queries during cart operations.
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findByUserId(UUID userId);

    Optional<Cart> findByUserEmail(String email);

    @EntityGraph(attributePaths = {"items", "items.listing", "items.listing.productVariant", "items.listing.productVariant.product"})
    @Query("SELECT c FROM Cart c WHERE c.user.email = :email")
    Optional<Cart> findWithItemsByUserEmail(@Param("email") String email);

    @EntityGraph(attributePaths = {"items", "items.listing", "items.listing.productVariant", "items.listing.productVariant.product"})
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId")
    Optional<Cart> findWithItemsByUserId(@Param("userId") UUID userId);
}
