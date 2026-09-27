package com.amazon.dtos.brand.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a brand marketing post.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBrandPostRequestDto {

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    private String caption;

    private String linkedAsin;

    @Builder.Default
    private Long reachCount = 0L;

    @Builder.Default
    private Long likesCount = 0L;
}
