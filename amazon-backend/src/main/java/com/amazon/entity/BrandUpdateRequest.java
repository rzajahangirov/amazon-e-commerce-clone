package com.amazon.entity;

import com.amazon.enums.BrandUpdateRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

/**
 * BrandUpdateRequest entity representing brand metadata update proposals submitted by brand owners.
 * Adheres to Senior Backend Developer Guidelines Section 5.
 */
@Entity
@Table(name = "brand_update_requests", indexes = {
    @Index(name = "idx_brand_update_brand_id", columnList = "brand_id"),
    @Index(name = "idx_brand_update_user_id", columnList = "requested_by_user_id"),
    @Index(name = "idx_brand_update_status", columnList = "status")
})
@SQLDelete(sql = "UPDATE brand_update_requests SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"brand", "requestedByUser"})
public class BrandUpdateRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private User requestedByUser;

    @Column(name = "proposed_brand_name", length = 150)
    private String proposedBrandName;

    @Column(name = "proposed_logo_url", length = 500)
    private String proposedLogoUrl;

    @Column(name = "proposed_about_text", columnDefinition = "TEXT")
    private String proposedAboutText;

    @Column(name = "proposed_trademark_no", length = 100)
    private String proposedTrademarkNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BrandUpdateRequestStatus status = BrandUpdateRequestStatus.PENDING;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BrandUpdateRequest other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
