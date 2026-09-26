package com.amazon.repository;

import com.amazon.entity.Brand;
import com.amazon.enums.BrandStatus;
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
 * Spring Data JPA repository for {@link Brand} entities.
 * Adheres to Senior Backend Developer Guidelines Section 5.
 */
@Repository
public interface BrandRepository extends JpaRepository<Brand, UUID> {

    @EntityGraph(attributePaths = {"ownerUser"})
    Optional<Brand> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    @EntityGraph(attributePaths = {"ownerUser"})
    Page<Brand> findByStatus(BrandStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"ownerUser"})
    Page<Brand> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"ownerUser"})
    @Query("SELECT b FROM Brand b WHERE b.id = :id")
    Optional<Brand> findWithDetailsById(@Param("id") UUID id);
}
