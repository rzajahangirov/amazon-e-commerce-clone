package com.amazon.repository;

import com.amazon.entity.ProductListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    Page<ProductListing> findBySellerId(UUID sellerId, Pageable pageable);

    @EntityGraph(attributePaths = {"productVariant", "productVariant.product", "seller"})
    @Query("SELECT pl FROM ProductListing pl WHERE pl.id = :id")
    Optional<ProductListing> findWithDetailsById(@Param("id") UUID id);

    @Query("SELECT pl FROM ProductListing pl WHERE pl.productVariant.product.brand.id = :brandId")
    List<ProductListing> findByBrandId(@Param("brandId") UUID brandId);

    @Query("SELECT MIN(pl.price) FROM ProductListing pl WHERE pl.productVariant.product.id = :productId AND pl.status = com.amazon.enums.ListingStatus.ACTIVE AND pl.isBuyboxWinner = true")
    Optional<java.math.BigDecimal> findBuyBoxPriceByProductId(@Param("productId") UUID productId);

    @Query("SELECT pl.productVariant.product.id, MIN(pl.price) FROM ProductListing pl WHERE pl.productVariant.product.id IN :productIds AND pl.status = com.amazon.enums.ListingStatus.ACTIVE AND pl.isBuyboxWinner = true GROUP BY pl.productVariant.product.id")
    List<Object[]> findBuyBoxPricesByProductIds(@Param("productIds") java.util.Collection<UUID> productIds);

    @Query("SELECT pl.productVariant.product.id, MIN(pl.price) FROM ProductListing pl WHERE pl.productVariant.product.id IN :productIds AND pl.status = com.amazon.enums.ListingStatus.ACTIVE GROUP BY pl.productVariant.product.id")
    List<Object[]> findLowestActivePricesByProductIds(@Param("productIds") java.util.Collection<UUID> productIds);

    @EntityGraph(attributePaths = {"productVariant", "productVariant.product", "seller"})
    @Query("SELECT pl FROM ProductListing pl WHERE pl.productVariant.product.id = :productId AND pl.status = com.amazon.enums.ListingStatus.ACTIVE ORDER BY pl.isBuyboxWinner DESC, pl.price ASC")
    List<ProductListing> findActiveListingsByProductIdWithDetails(@Param("productId") UUID productId);

    @EntityGraph(attributePaths = {"productVariant", "productVariant.product", "seller"})
    @Query("SELECT pl FROM ProductListing pl WHERE pl.productVariant.id = :variantId AND pl.status = com.amazon.enums.ListingStatus.ACTIVE ORDER BY pl.isBuyboxWinner DESC, pl.price ASC")
    List<ProductListing> findActiveListingsByVariantIdWithDetails(@Param("variantId") UUID variantId);
}

