package com.amazon.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

/**
 * Review entity representing customer ratings (1 to 5 stars) and reviews for products.
 * Adheres to Senior Backend Developer Guidelines Section 5.
 */
@Entity
@Table(name = "reviews", indexes = {
    @Index(name = "idx_reviews_product_id", columnList = "product_id"),
    @Index(name = "idx_reviews_user_id", columnList = "user_id"),
    @Index(name = "idx_reviews_product_user_unique", columnList = "product_id, user_id", unique = true),
    @Index(name = "idx_reviews_rating", columnList = "rating")
})
@SQLDelete(sql = "UPDATE reviews SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"product", "user"})
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "user_title_role", length = 200)
    private String userTitleRole;

    @Column(name = "is_verified_enterprise", nullable = false)
    @Builder.Default
    private Boolean isVerifiedEnterprise = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Review other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
