package com.amazon.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

/**
 * SellerProfile entity representing seller store information associated with a user and brand.
 * Adheres to Senior Backend Developer Guidelines Section 5.
 */
@Entity
@Table(name = "seller_profiles", indexes = {
    @Index(name = "idx_seller_profiles_user_id", columnList = "user_id", unique = true),
    @Index(name = "idx_seller_profiles_brand_id", columnList = "brand_id")
})
@SQLDelete(sql = "UPDATE seller_profiles SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "brand"})
public class SellerProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Column(name = "store_name", length = 150)
    private String storeName;

    @Column(name = "tax_number", length = 100)
    private String taxNumber;

    @Column(name = "business_address", length = 500)
    private String businessAddress;

    @Column(name = "bank_account_details", length = 500)
    private String bankAccountDetails;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SellerProfile other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
