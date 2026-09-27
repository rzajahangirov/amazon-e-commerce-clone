package com.amazon.dtos.brand.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO representing a published brand marketing post.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandPostResponseDto {

    private UUID id;
    private UUID brandId;
    private String brandName;
    private UUID authorUserId;
    private String authorName;
    private String imageUrl;
    private String caption;
    private String linkedAsin;
    @Builder.Default
    private Long reachCount = 0L;
    @Builder.Default
    private Long likesCount = 0L;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
