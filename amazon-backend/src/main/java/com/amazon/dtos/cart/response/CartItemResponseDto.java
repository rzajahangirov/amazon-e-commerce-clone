package com.amazon.dtos.cart.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for an item within a shopping cart.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponseDto {

    private UUID id;
    private UUID listingId;
    private UUID productVariantId;
    private String productTitle;
    private String variantName;
    private String asin;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private Boolean isSavedForLater;
    private LocalDateTime addedAt;
}
