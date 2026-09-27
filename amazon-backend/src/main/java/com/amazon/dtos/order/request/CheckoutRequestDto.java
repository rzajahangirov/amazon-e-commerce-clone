package com.amazon.dtos.order.request;

import com.amazon.payloads.ApiPayload;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    private UUID shippingAddressId;

    private String idempotencyKey;

    @Valid
    private ShippingAddressRequestDto shippingAddress;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShippingAddressRequestDto {
        @NotBlank(message = "Full name is required")
        @Size(max = 120)
        private String fullName;

        @NotBlank(message = "Street address is required")
        @Size(max = 255)
        private String streetLine1;

        @Size(max = 255)
        private String streetLine2;

        @NotBlank(message = "City is required")
        @Size(max = 100)
        private String city;

        @NotBlank(message = "State is required")
        @Size(max = 100)
        private String state;

        @NotBlank(message = "Postal code is required")
        @Size(max = 20)
        private String postalCode;

        @NotBlank(message = "Country is required")
        @Size(max = 100)
        private String country;

        @Size(max = 40)
        private String phone;
    }
}
