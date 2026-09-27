package com.amazon.entity;

import com.amazon.converter.JsonToStringMapConverter;
import com.amazon.enums.GovernanceStatus;
import com.amazon.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Product entity representing master product definitions in the catalog.
 * Corresponds to DBML Table products.
 * Adheres to Senior Backend Developer Guidelines Section 5.
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_products_category_id", columnList = "category_id"),
    @Index(name = "idx_products_seller_id", columnList = "created_by_seller_id"),
    @Index(name = "idx_products_status", columnList = "status"),
    @Index(name = "idx_products_brand_id", columnList = "brand_id")
})
@SQLDelete(sql = "UPDATE products SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"seller", "category", "variants", "brand"})
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_seller_id")
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String title;

    @Column(name = "master_sku", length = 100)
    private String masterSku;

    @Enumerated(EnumType.STRING)
    @Column(name = "governance_status", length = 30)
    @Builder.Default
    private GovernanceStatus governanceStatus = GovernanceStatus.DRAFT;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "list_price", precision = 10, scale = 2)
    private BigDecimal listPrice;

    @Column(name = "discount_percentage")
    private Integer discountPercentage;

    @Column(name = "badge_tag", length = 50)
    private String badgeTag;

    @Column(name = "model_number", length = 100)
    private String modelNumber;

    @Column(name = "delivery_estimate")
    private String deliveryEstimate;

    @Column(name = "sales_volume_text")
    private String salesVolumeText;

    @Column(name = "specifications_json", columnDefinition = "TEXT")
    @Convert(converter = JsonToStringMapConverter.class)
    @Builder.Default
    private Map<String, String> specifications = new HashMap<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProductStatus status = ProductStatus.DRAFT;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    @Column(name = "main_image_url")
    private String mainImageUrl;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "total_units_sold", nullable = false)
    @Builder.Default
    private Long totalUnitsSold = 0L;

    @Column(name = "average_rating")
    @Builder.Default
    private Double averageRating = 0.0;

    @Column(name = "total_reviews")
    @Builder.Default
    private Integer totalReviews = 0;

    public Long getViewCount() {
        return viewCount != null ? viewCount : 0L;
    }

    public Long getTotalUnitsSold() {
        return totalUnitsSold != null ? totalUnitsSold : 0L;
    }

    public Double getAverageRating() {
        return averageRating != null ? averageRating : 0.0;
    }

    public Integer getTotalReviews() {
        return totalReviews != null ? totalReviews : 0;
    }

    public UUID getBrandId() {
        return brand != null ? brand.getId() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
