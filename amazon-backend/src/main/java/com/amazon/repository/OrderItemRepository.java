package com.amazon.repository;

import com.amazon.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link OrderItem} entities.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID>, JpaSpecificationExecutor<OrderItem> {

    List<OrderItem> findByOrderId(UUID orderId);

    List<OrderItem> findBySellerId(UUID sellerId);

    @org.springframework.data.jpa.repository.Query("SELECT oi FROM OrderItem oi WHERE oi.productVariant.product.brand.id = :brandId")
    List<OrderItem> findByBrandId(@org.springframework.data.repository.query.Param("brandId") UUID brandId);

    @org.springframework.data.jpa.repository.Query("SELECT oi FROM OrderItem oi WHERE oi.seller.id = :sellerId")
    List<OrderItem> findBySellerUserId(@org.springframework.data.repository.query.Param("sellerId") UUID sellerId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(oi.quantity), 0L) FROM OrderItem oi WHERE oi.productVariant.product.id = :productId AND oi.itemStatus = com.amazon.enums.OrderItemStatus.DELIVERED")
    Long countDeliveredUnitsSoldByProductId(@org.springframework.data.repository.query.Param("productId") UUID productId);

    @org.springframework.data.jpa.repository.Query("SELECT oi.productVariant.product.id, COALESCE(SUM(oi.quantity), 0L) FROM OrderItem oi WHERE oi.productVariant.product.id IN :productIds AND oi.itemStatus = com.amazon.enums.OrderItemStatus.DELIVERED GROUP BY oi.productVariant.product.id")
    List<Object[]> countDeliveredUnitsSoldByProductIds(@org.springframework.data.repository.query.Param("productIds") java.util.Collection<UUID> productIds);
}
