package com.amazon.dtos.listing.response;

import com.amazon.enums.FulfillmentType;
import com.amazon.enums.ListingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for a seller's product listing offer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductListingResponseDto {

    private UUID id;
    private UUID productVariantId;
    private String variantAsin;
    private String variantName;
    private UUID sellerId;
    private String sellerName;
    private String sellerSku;
    private BigDecimal price;
    private Integer stockQuantity;
    private FulfillmentType fulfillmentType;
    private Boolean isBuyboxWinner;
    private ListingStatus status;
    private LocalDateTime createdAt;
}
