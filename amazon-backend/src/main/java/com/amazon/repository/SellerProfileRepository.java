package com.amazon.repository;

import com.amazon.entity.SellerProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link SellerProfile} entities.
 */
@Repository
public interface SellerProfileRepository extends JpaRepository<SellerProfile, UUID> {

    @EntityGraph(attributePaths = {"user", "brand"})
    @Query("SELECT sp FROM SellerProfile sp WHERE sp.user.id = :userId")
    Optional<SellerProfile> findByUserId(@Param("userId") UUID userId);

    @EntityGraph(attributePaths = {"user", "brand"})
    @Query("SELECT sp FROM SellerProfile sp WHERE sp.user.email = :email")
    Optional<SellerProfile> findByUserEmail(@Param("email") String email);

    @EntityGraph(attributePaths = {"user", "brand"})
    @Query("SELECT sp FROM SellerProfile sp WHERE sp.brand.id = :brandId")
    Optional<SellerProfile> findByBrandId(@Param("brandId") UUID brandId);
}
