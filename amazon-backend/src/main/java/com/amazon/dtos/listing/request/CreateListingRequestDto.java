package com.amazon.dtos.listing.request;

import com.amazon.enums.FulfillmentType;
import com.amazon.enums.ListingStatus;
import com.amazon.payloads.ApiPayload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for creating a new seller product listing.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateListingRequestDto implements ApiPayload {

    @NotNull(message = "Product variant ID is required")
    private UUID productVariantId;

    @NotBlank(message = "Seller SKU is required")
    private String sellerSku;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    private BigDecimal price;

    @Positive(message = "Minimum price floor must be greater than zero")
    private BigDecimal minPriceFloor;

    @NotNull(message = "Stock quantity is required")
    @PositiveOrZero(message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    private FulfillmentType fulfillmentType;

    private ListingStatus status;
}
