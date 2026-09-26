package com.amazon.entity;

import com.amazon.enums.BrandApplicationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

/**
 * BrandApplication entity representing brand registry applications submitted by sellers.
 * Adheres to Senior Backend Developer Guidelines Section 5.
 */
@Entity
@Table(name = "brand_applications", indexes = {
    @Index(name = "idx_brand_app_email", columnList = "applicant_email"),
    @Index(name = "idx_brand_app_status", columnList = "status"),
    @Index(name = "idx_brand_app_slug", columnList = "brand_slug")
})
@SQLDelete(sql = "UPDATE brand_applications SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class BrandApplication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "applicant_name", nullable = false, length = 150)
    private String applicantName;

    @Column(name = "applicant_email", nullable = false, length = 150)
    private String applicantEmail;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "applicant_phone", length = 50)
    private String applicantPhone;

    @Column(name = "brand_name", nullable = false, length = 150)
    private String brandName;

    @Column(name = "brand_slug", nullable = false, length = 150)
    private String brandSlug;

    @Column(name = "trademark_reg_number", nullable = false, length = 100)
    private String trademarkRegistrationNumber;

    @Column(name = "brand_country", nullable = false, length = 100)
    private String brandCountry;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "about_text", columnDefinition = "TEXT")
    private String aboutText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BrandApplicationStatus status = BrandApplicationStatus.PENDING;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BrandApplication other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
