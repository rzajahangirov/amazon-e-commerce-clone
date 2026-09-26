package com.amazon.entity;

import com.amazon.enums.OrderItemStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * OrderItem entity representing an individual product variant line item within a customer order.
 * Corresponds to DBML Table order_items.
 * Adheres to Senior Backend Developer Guidelines Section 5.
 */
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_items_order_id", columnList = "order_id"),
    @Index(name = "idx_order_items_variant_id", columnList = "product_variant_id"),
    @Index(name = "idx_order_items_seller_id", columnList = "seller_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"order", "listing", "productVariant", "seller"})
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id")
    private ProductListing listing;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_status", nullable = false, length = 20)
    @Builder.Default
    private OrderItemStatus itemStatus = OrderItemStatus.PENDING;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
