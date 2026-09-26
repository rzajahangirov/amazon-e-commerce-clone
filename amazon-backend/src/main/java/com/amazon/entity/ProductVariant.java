package com.amazon.entity;

import com.amazon.converter.JsonToMapConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ProductVariant entity representing specific sellable variations (size, color, ASIN) of a product.
 * Corresponds to DBML Table product_variants.
 * Adheres to Senior Backend Developer Guidelines Section 5.
 */
@Entity
@Table(name = "product_variants", indexes = {
    @Index(name = "idx_variants_asin", columnList = "asin", unique = true),
    @Index(name = "idx_variants_product_id", columnList = "product_id")
})
@SQLDelete(sql = "UPDATE product_variants SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"product", "listings"})
public class ProductVariant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(unique = true, nullable = false, length = 20)
    private String asin;

    @Column(name = "variant_name")
    private String variantName;

    @Column(name = "variant_attributes_json", columnDefinition = "TEXT")
    @Convert(converter = JsonToMapConverter.class)
    @Builder.Default
    private Map<String, Object> variantAttributes = new HashMap<>();

    public Map<String, Object> getVariantAttributes() {
        if (variantAttributes == null) {
            variantAttributes = new HashMap<>();
        }
        return variantAttributes;
    }

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductListing> listings = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductVariant other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
