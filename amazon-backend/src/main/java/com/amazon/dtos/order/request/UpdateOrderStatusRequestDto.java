package com.amazon.dtos.order.request;

import com.amazon.enums.OrderStatus;
import com.amazon.payloads.ApiPayload;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for administrative or seller updates to order status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderStatusRequestDto implements ApiPayload {

    @NotNull(message = "Order status is required")
    private OrderStatus status;
}
