package com.amazon.repository;

import com.amazon.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Review} entities.
 * Uses @EntityGraph to eliminate N+1 queries when loading reviews with user details.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByProductIdAndUserId(UUID productId, UUID userId);

    @EntityGraph(attributePaths = {"user", "product"})
    Optional<Review> findByProductIdAndUserId(UUID productId, UUID userId);

    @EntityGraph(attributePaths = {"user", "product"})
    Page<Review> findByProductId(UUID productId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "product"})
    @Query("SELECT r FROM Review r WHERE r.id = :id")
    Optional<Review> findWithDetailsById(@Param("id") UUID id);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double calculateAverageRatingByProductId(@Param("productId") UUID productId);

    long countByProductId(UUID productId);
}
