package com.amazon.entity;

import com.amazon.enums.BrandStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Brand entity representing an officially registered and verified brand on the platform.
 * Adheres to Senior Backend Developer Guidelines Section 5 (Optimistic locking, lazy relations, soft-delete).
 */
@Entity
@Table(name = "brands", indexes = {
    @Index(name = "idx_brands_slug", columnList = "slug", unique = true),
    @Index(name = "idx_brands_name", columnList = "name"),
    @Index(name = "idx_brands_status", columnList = "status"),
    @Index(name = "idx_brands_owner_id", columnList = "owner_user_id")
})
@SQLDelete(sql = "UPDATE brands SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"ownerUser", "products", "members", "posts", "updateRequests", "sellerProfiles"})
public class Brand extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String slug;

    @Column(name = "trademark_reg_number", length = 100)
    private String trademarkRegistrationNumber;

    @Column(name = "brand_country", length = 100)
    private String brandCountry;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "about_text", columnDefinition = "TEXT")
    private String aboutText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BrandStatus status = BrandStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User ownerUser;

    @Version
    private Long version;

    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BrandMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BrandPost> posts = new ArrayList<>();

    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BrandUpdateRequest> updateRequests = new ArrayList<>();

    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SellerProfile> sellerProfiles = new ArrayList<>();

    public boolean isActive() {
        return status == BrandStatus.ACTIVE && getDeletedAt() == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Brand other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
