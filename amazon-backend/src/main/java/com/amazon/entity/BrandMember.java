package com.amazon.entity;

import com.amazon.enums.BrandRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * BrandMember entity representing membership and specific brand permissions of users within a Brand organization.
 * Adheres to Senior Backend Developer Guidelines Section 5.
 */
@Entity
@Table(name = "brand_members", indexes = {
    @Index(name = "idx_brand_members_brand_id", columnList = "brand_id"),
    @Index(name = "idx_brand_members_user_id", columnList = "user_id"),
    @Index(name = "idx_brand_members_unique", columnList = "brand_id, user_id", unique = true)
})
@SQLDelete(sql = "UPDATE brand_members SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"brand", "user"})
public class BrandMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "brand_role", nullable = false, length = 30)
    private BrandRole brandRole;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "assigned_at")
    @Builder.Default
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BrandMember other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
