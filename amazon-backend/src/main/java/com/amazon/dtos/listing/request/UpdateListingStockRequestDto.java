package com.amazon.dtos.listing.request;

import com.amazon.payloads.ApiPayload;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for updating inventory stock or price of a listing.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateListingStockRequestDto implements ApiPayload {

    @NotNull(message = "Stock quantity is required")
    @PositiveOrZero(message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    private BigDecimal price;
}
