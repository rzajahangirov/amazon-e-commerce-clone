package com.amazon.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

/**
 * BrandPost entity representing social / marketing posts published by a brand.
 * Adheres to Senior Backend Developer Guidelines Section 5.
 */
@Entity
@Table(name = "brand_posts", indexes = {
    @Index(name = "idx_brand_posts_brand_id", columnList = "brand_id"),
    @Index(name = "idx_brand_posts_author_id", columnList = "author_user_id"),
    @Index(name = "idx_brand_posts_created", columnList = "brand_id, created_at")
})
@SQLDelete(sql = "UPDATE brand_posts SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"brand", "authorUser"})
public class BrandPost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_user_id", nullable = false)
    private User authorUser;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String caption;

    @Column(name = "linked_asin", length = 50)
    private String linkedAsin;

    @Column(name = "reach_count", nullable = false)
    @Builder.Default
    private Long reachCount = 0L;

    @Column(name = "likes_count", nullable = false)
    @Builder.Default
    private Long likesCount = 0L;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BrandPost other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
