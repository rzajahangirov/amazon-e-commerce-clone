package com.amazon.repository;

import com.amazon.entity.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Category} entities.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByNameIgnoreCase(String name);

    Optional<Category> findByNameIgnoreCase(String name);

    Optional<Category> findByIdAndIsApprovedTrue(UUID id);

    Optional<Category> findBySlugAndIsApprovedTrue(String slug);

    List<Category> findByParentIsNull();

    List<Category> findByParentIsNullAndIsApprovedTrue();

    List<Category> findByParentIdAndIsApprovedTrue(UUID parentId);

    List<Category> findByIsApproved(Boolean isApproved);

    Page<Category> findByIsApprovedFalse(Pageable pageable);

    List<Category> findByIsApprovedFalseAndRejectionReasonIsNull();

    @Query("SELECT c.id, c.name, SUM(oi.subtotal) FROM OrderItem oi JOIN oi.order o JOIN oi.productVariant pv JOIN pv.product p JOIN p.category c WHERE o.status NOT IN (com.amazon.enums.OrderStatus.CANCELLED, com.amazon.enums.OrderStatus.REFUNDED) GROUP BY c.id, c.name ORDER BY SUM(oi.subtotal) DESC")
    List<Object[]> findTopRevenueCategories(Pageable pageable);

    @EntityGraph(attributePaths = {"subCategories"})
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.isApproved = true")
    List<Category> findApprovedRootCategoriesWithChildren();

    @EntityGraph(attributePaths = {"subCategories"})
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL")
    List<Category> findRootCategoriesWithChildren();
}
