package com.amazon.repository;

import com.amazon.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link CartItem} entities.
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByCartIdAndListingId(UUID cartId, UUID listingId);

    List<CartItem> findByCartId(UUID cartId);

    void deleteByCartId(UUID cartId);
}
