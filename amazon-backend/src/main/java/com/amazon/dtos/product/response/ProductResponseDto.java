package com.amazon.dtos.product.response;

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
    private UUID categoryId;
    private String categoryName;
    private String title;
    private String description;
    private BigDecimal basePrice;
    private ProductStatus status;
    private List<ProductVariantResponseDto> variants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
