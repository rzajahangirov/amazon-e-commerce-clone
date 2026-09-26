package com.amazon.dtos.product.response;

import com.amazon.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Summary Response DTO for product listings / search results.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSummaryResponseDto {

    private UUID id;
    private String title;
    private BigDecimal basePrice;
    private String categoryName;
    private ProductStatus status;
    @Builder.Default
    private Double averageRating = 0.0;
    @Builder.Default
    private Integer totalReviews = 0;
    @Builder.Default
    private Boolean isFavorited = false;
}
