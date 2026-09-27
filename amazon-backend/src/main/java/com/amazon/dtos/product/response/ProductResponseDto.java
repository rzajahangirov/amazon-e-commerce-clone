package com.amazon.dtos.product.response;

import com.amazon.enums.GovernanceStatus;
import com.amazon.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Detailed Response DTO for a product, including its variants.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDto {

    private UUID id;
    private UUID sellerId;
    private String sellerName;
    private UUID brandId;
    private String brandName;
    private UUID categoryId;
    private String categoryName;
    private String title;
    private String masterSku;
    private GovernanceStatus governanceStatus;
    private String description;
    private BigDecimal basePrice;
    private ProductStatus status;
    private List<ProductVariantResponseDto> variants;
    @Builder.Default
    private Double averageRating = 0.0;
    @Builder.Default
    private Integer totalReviews = 0;
    @Builder.Default
    private Long totalUnitsSold = 0L;
    @Builder.Default
    private Long viewCount = 0L;
    private BigDecimal buyBoxPrice;
    private String mainImageUrl;
    @Builder.Default
    private Boolean isFavorited = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
