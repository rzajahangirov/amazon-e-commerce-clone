package com.amazon.dtos.product.request;

import com.amazon.enums.ProductSortBy;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Multi-criteria request DTO for Amazon-style product search, filtering, and discovery.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Criteria for searching and filtering the public product catalog")
public class ProductSearchRequestDto {

    @Schema(description = "Free-text keyword matched against title, description, brand, and category")
    private String query;

    @Schema(description = "Filter by specific category ID (including child categories)")
    private UUID categoryId;

    @Schema(description = "Filter by specific category slug (including child categories)")
    private String categorySlug;

    @Schema(description = "Filter by specific brand ID")
    private UUID brandId;

    @Schema(description = "Minimum BuyBox / base price")
    private BigDecimal minPrice;

    @Schema(description = "Maximum BuyBox / base price")
    private BigDecimal maxPrice;

    @Schema(description = "Minimum average customer review rating (e.g. 4.0)")
    private Double minRating;

    @Schema(description = "Sorting criteria")
    @Builder.Default
    private ProductSortBy sortBy = ProductSortBy.FEATURED;

    @Schema(description = "Page number (0-indexed)")
    @Builder.Default
    private Integer page = 0;

    @Schema(description = "Page size")
    @Builder.Default
    private Integer size = 20;
}
