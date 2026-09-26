package com.amazon.dtos.cart.request;

import com.amazon.payloads.ApiPayload;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for adding an item into the user's shopping cart.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddToCartRequestDto implements ApiPayload {

    @NotNull(message = "Listing ID is required")
    private UUID listingId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Builder.Default
    private Integer quantity = 1;
}
