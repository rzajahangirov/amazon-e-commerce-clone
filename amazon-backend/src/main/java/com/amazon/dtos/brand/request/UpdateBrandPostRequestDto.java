package com.amazon.dtos.brand.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing brand marketing post.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBrandPostRequestDto {

    private String imageUrl;

    private String caption;

    private String linkedAsin;

    private Long reachCount;

    private Long likesCount;
}
