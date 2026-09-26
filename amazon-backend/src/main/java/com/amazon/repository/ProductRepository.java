package com.amazon.repository;

import com.amazon.entity.Product;
import com.amazon.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Product} entities.
 * Uses @EntityGraph to eliminate N+1 queries when fetching product details.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p SET p.viewCount = COALESCE(p.viewCount, 0) + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") UUID id);

    @Override
    @EntityGraph(attributePaths = {"category", "seller", "variants", "brand"})
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "seller", "variants", "brand"})
    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.status = com.amazon.enums.ProductStatus.ACTIVE AND p.category.isApproved = true AND p.brand.status = com.amazon.enums.BrandStatus.ACTIVE")
    Optional<Product> findPublishedWithDetailsById(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"category", "seller", "variants", "brand"})
    @Query("SELECT p FROM Product p WHERE p.status = com.amazon.enums.ProductStatus.ACTIVE AND p.category.isApproved = true AND p.brand.status = com.amazon.enums.BrandStatus.ACTIVE AND (:categoryId IS NULL OR p.category.id = :categoryId)")
    Page<Product> findPublished(@Param("categoryId") UUID categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "seller", "variants", "brand"})
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findWithDetailsById(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"category", "seller", "brand"})
    Page<Product> findByCategoryIdAndStatus(UUID categoryId, ProductStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "seller", "brand"})
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "seller", "brand"})
    Page<Product> findBySellerId(UUID sellerId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "seller", "variants", "brand"})
    @Query("SELECT p FROM Product p WHERE p.brand.id = :brandId")
    Page<Product> findByBrandId(@Param("brandId") UUID brandId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "seller", "variants", "brand"})
    @Query("SELECT p FROM Product p WHERE p.brand.id = :brandId")
    List<Product> findByBrandId(@Param("brandId") UUID brandId);
}
