package com.amazon.repository;

import com.amazon.entity.ProductListing;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ProductListing} entities.
 */
@Repository
public interface ProductListingRepository extends JpaRepository<ProductListing, UUID> {

    List<ProductListing> findByProductVariantId(UUID productVariantId);

    Optional<ProductListing> findByProductVariantIdAndIsBuyboxWinnerTrue(UUID productVariantId);

    List<ProductListing> findBySellerId(UUID sellerId);

    @EntityGraph(attributePaths = {"productVariant", "productVariant.product", "seller"})
    @Query("SELECT pl FROM ProductListing pl WHERE pl.id = :id")
    Optional<ProductListing> findWithDetailsById(@Param("id") UUID id);
}
