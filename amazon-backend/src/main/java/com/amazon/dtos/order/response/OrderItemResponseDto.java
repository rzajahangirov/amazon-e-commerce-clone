package com.amazon.dtos.order.response;

import com.amazon.enums.OrderItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for an order line item.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponseDto {

    private UUID id;
    private UUID listingId;
    private UUID productVariantId;
    private String productTitle;
    private String variantName;
    private String asin;
    private UUID sellerId;
    private String sellerName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private OrderItemStatus itemStatus;
}
