package com.amazon.dtos.brand.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO returning keys and identifiers created during the atomic quick-create operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickCreateProductResponseDto {

    private UUID productId;
    private String productTitle;
    private UUID variantId;
    private String asin;
    private String variantName;
    private UUID listingId;
    private String sellerSku;
    private BigDecimal price;
    private Integer stockQuantity;
    private UUID brandId;
}
