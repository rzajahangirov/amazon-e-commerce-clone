package com.amazon.repository;

import com.amazon.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Order} entities.
 * Uses @EntityGraph to eliminate N+1 queries when fetching orders with items.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = {"user", "items", "items.productVariant", "items.seller"})
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findWithDetailsById(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"user", "items", "items.productVariant", "items.seller"})
    @Query("SELECT o FROM Order o WHERE o.orderNumber = :orderNumber")
    Optional<Order> findWithDetailsByOrderNumber(@Param("orderNumber") String orderNumber);

    Page<Order> findByUserId(UUID userId, Pageable pageable);

    Page<Order> findByUserEmail(String email, Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status NOT IN (com.amazon.enums.OrderStatus.CANCELLED, com.amazon.enums.OrderStatus.REFUNDED)")
    java.math.BigDecimal calculateGmv();

    long countByStatusNotIn(java.util.Collection<com.amazon.enums.OrderStatus> statuses);
}
