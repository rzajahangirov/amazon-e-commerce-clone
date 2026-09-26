package com.amazon.dtos.product.response;

import com.amazon.dtos.listing.response.ProductListingResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for product variants.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantResponseDto {

    private UUID id;
    private UUID productId;
    private String asin;
    private String variantName;
    private Map<String, Object> variantAttributes;
    private List<ProductListingResponseDto> listings;
    private LocalDateTime createdAt;
}
