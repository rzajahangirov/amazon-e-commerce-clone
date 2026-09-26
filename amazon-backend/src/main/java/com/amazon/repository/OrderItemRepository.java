package com.amazon.repository;

import com.amazon.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link OrderItem} entities.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrderId(UUID orderId);

    List<OrderItem> findBySellerId(UUID sellerId);

    @org.springframework.data.jpa.repository.Query("SELECT oi FROM OrderItem oi WHERE oi.productVariant.product.brand.id = :brandId")
    List<OrderItem> findByBrandId(@org.springframework.data.repository.query.Param("brandId") UUID brandId);

    @org.springframework.data.jpa.repository.Query("SELECT oi FROM OrderItem oi WHERE oi.seller.id = :sellerId")
    List<OrderItem> findBySellerUserId(@org.springframework.data.repository.query.Param("sellerId") UUID sellerId);
}
