package com.amazon.repository;

import com.amazon.entity.BrandUpdateRequest;
import com.amazon.enums.BrandUpdateRequestStatus;
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
 * Spring Data JPA repository for {@link BrandUpdateRequest} entities.
 */
@Repository
public interface BrandUpdateRequestRepository extends JpaRepository<BrandUpdateRequest, UUID> {

    @EntityGraph(attributePaths = {"brand", "requestedByUser"})
    Page<BrandUpdateRequest> findByStatus(BrandUpdateRequestStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"brand", "requestedByUser"})
    @Query("SELECT r FROM BrandUpdateRequest r")
    Page<BrandUpdateRequest> findAllWithDetails(Pageable pageable);

    @EntityGraph(attributePaths = {"brand", "requestedByUser"})
    @Query("SELECT r FROM BrandUpdateRequest r WHERE r.id = :id")
    Optional<BrandUpdateRequest> findWithDetailsById(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"brand", "requestedByUser"})
    @Query("SELECT r FROM BrandUpdateRequest r WHERE r.brand.id = :brandId AND r.status = :status")
    List<BrandUpdateRequest> findByBrandIdAndStatus(@Param("brandId") UUID brandId, @Param("status") BrandUpdateRequestStatus status);
}
