package com.amazon.dtos.seller.request;

import com.amazon.enums.OrderItemStatus;
import com.amazon.payloads.ApiPayload;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for a seller to update the fulfillment status of an individual order item.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderItemStatusRequestDto implements ApiPayload {

    @NotNull(message = "New status is required")
    private OrderItemStatus newStatus;
}
