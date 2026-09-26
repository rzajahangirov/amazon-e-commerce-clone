package com.amazon.entity;

import com.amazon.enums.FulfillmentType;
import com.amazon.enums.ListingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * ProductListing entity representing a seller's offer/inventory for a specific product variant.
 * Corresponds to DBML Table product_listings.
 * Adheres to Senior Backend Developer Guidelines Section 5 (including @Version for stock concurrency).
 */
@Entity
@Table(name = "product_listings", indexes = {
    @Index(name = "idx_listings_variant_id", columnList = "product_variant_id"),
    @Index(name = "idx_listings_seller_id", columnList = "seller_id"),
    @Index(name = "idx_listings_status", columnList = "status"),
    @Index(name = "idx_listings_buybox", columnList = "product_variant_id, is_buybox_winner")
})
@SQLDelete(sql = "UPDATE product_listings SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"productVariant", "seller"})
public class ProductListing extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(name = "seller_sku", nullable = false, length = 100)
    private String sellerSku;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Builder.Default
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_type", nullable = false, length = 10)
    @Builder.Default
    private FulfillmentType fulfillmentType = FulfillmentType.FBM;

    @Builder.Default
    @Column(name = "is_buybox_winner", nullable = false)
    private Boolean isBuyboxWinner = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ListingStatus status = ListingStatus.ACTIVE;

    @Version
    private Long version;

    public boolean isAvailableForPurchase(int requestedQuantity) {
        return status == ListingStatus.ACTIVE && stockQuantity != null && stockQuantity >= requestedQuantity;
    }

    public void deductStock(int quantity) {
        this.stockQuantity -= quantity;
    }

    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductListing other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
