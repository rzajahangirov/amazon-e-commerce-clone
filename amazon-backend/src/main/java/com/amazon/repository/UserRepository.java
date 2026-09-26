package com.amazon.repository;

import com.amazon.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link User} entities.
 * Implements fetch joins / @EntityGraph to eliminate N+1 queries.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = {"roles"})
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    boolean existsByEmail(String email);
}
