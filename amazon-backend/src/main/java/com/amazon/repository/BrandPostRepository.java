package com.amazon.repository;

import com.amazon.entity.BrandPost;
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
 * Spring Data JPA repository for {@link BrandPost} entities.
 */
@Repository
public interface BrandPostRepository extends JpaRepository<BrandPost, UUID> {

    @EntityGraph(attributePaths = {"brand", "authorUser"})
    @Query("SELECT bp FROM BrandPost bp WHERE bp.brand.id = :brandId")
    Page<BrandPost> findByBrandId(@Param("brandId") UUID brandId, Pageable pageable);

    @EntityGraph(attributePaths = {"brand", "authorUser"})
    @Query("SELECT bp FROM BrandPost bp WHERE bp.id = :id AND bp.brand.id = :brandId")
    Optional<BrandPost> findByIdAndBrandId(@Param("id") UUID id, @Param("brandId") UUID brandId);

    @EntityGraph(attributePaths = {"brand", "authorUser"})
    @Query("SELECT bp FROM BrandPost bp WHERE bp.id = :id")
    Optional<BrandPost> findWithDetailsById(@Param("id") UUID id);
}
