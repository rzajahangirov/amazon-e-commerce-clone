package com.amazon.repository;

import com.amazon.entity.BrandMember;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link BrandMember} entities.
 */
@Repository
public interface BrandMemberRepository extends JpaRepository<BrandMember, UUID> {

    @EntityGraph(attributePaths = {"brand", "user"})
    @Query("SELECT bm FROM BrandMember bm WHERE bm.brand.id = :brandId")
    List<BrandMember> findByBrandId(@Param("brandId") UUID brandId);

    @EntityGraph(attributePaths = {"brand", "user"})
    @Query("SELECT bm FROM BrandMember bm WHERE bm.brand.id = :brandId AND bm.user.id = :userId")
    Optional<BrandMember> findByBrandIdAndUserId(@Param("brandId") UUID brandId, @Param("userId") UUID userId);

    @EntityGraph(attributePaths = {"brand", "user"})
    @Query("SELECT bm FROM BrandMember bm WHERE bm.user.id = :userId")
    Optional<BrandMember> findByUserId(@Param("userId") UUID userId);

    @EntityGraph(attributePaths = {"brand", "user"})
    @Query("SELECT bm FROM BrandMember bm WHERE bm.user.email = :email")
    Optional<BrandMember> findFirstByUserEmail(@Param("email") String email);

    @EntityGraph(attributePaths = {"brand", "user"})
    @Query("SELECT bm FROM BrandMember bm WHERE bm.user.id = :userId")
    List<BrandMember> findAllByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(bm) > 0 FROM BrandMember bm WHERE bm.brand.id = :brandId AND bm.user.id = :userId")
    boolean existsByBrandIdAndUserId(@Param("brandId") UUID brandId, @Param("userId") UUID userId);

    @EntityGraph(attributePaths = {"brand", "user"})
    @Query("SELECT bm FROM BrandMember bm WHERE bm.id = :id")
    Optional<BrandMember> findWithDetailsById(@Param("id") UUID id);
}
