package com.amazon.repository;

import com.amazon.entity.ProductVariant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ProductVariant} entities.
 */
@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    Optional<ProductVariant> findByAsin(String asin);

    boolean existsByAsin(String asin);

    List<ProductVariant> findByProductId(UUID productId);

    @EntityGraph(attributePaths = {"listings", "listings.seller"})
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.id = :id")
    Optional<ProductVariant> findWithListingsById(@Param("id") UUID id);
}
