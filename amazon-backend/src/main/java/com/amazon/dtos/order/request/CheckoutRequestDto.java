package com.amazon.dtos.order.request;

import com.amazon.payloads.ApiPayload;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for checking out the user's active shopping cart into an order.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequestDto implements ApiPayload {

    @NotNull(message = "Shipping address ID is required")
    private UUID shippingAddressId;

    private String idempotencyKey;
}
