package com.amazon.repository;

import com.amazon.entity.Product;
import com.amazon.enums.ProductStatus;
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
 * Spring Data JPA repository for {@link Product} entities.
 * Uses @EntityGraph to eliminate N+1 queries when fetching product details.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

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
