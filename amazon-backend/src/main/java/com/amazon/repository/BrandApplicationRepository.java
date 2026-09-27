package com.amazon.repository;

import com.amazon.entity.BrandApplication;
import com.amazon.enums.BrandApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link BrandApplication} entities.
 */
@Repository
public interface BrandApplicationRepository extends JpaRepository<BrandApplication, UUID> {

    Page<BrandApplication> findByStatus(BrandApplicationStatus status, Pageable pageable);

    Optional<BrandApplication> findByApplicantEmail(String email);

    boolean existsByApplicantEmail(String email);

    boolean existsByBrandSlug(String brandSlug);

    boolean existsByBrandName(String brandName);

    long countByStatus(BrandApplicationStatus status);
}
