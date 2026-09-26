package com.amazon.repository;

import com.amazon.entity.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    List<Category> findByIsApproved(Boolean isApproved);

    @EntityGraph(attributePaths = {"subCategories"})
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.isApproved = true")
    List<Category> findApprovedRootCategoriesWithChildren();

    @EntityGraph(attributePaths = {"subCategories"})
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL")
    List<Category> findRootCategoriesWithChildren();
}
