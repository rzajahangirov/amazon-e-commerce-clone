package com.amazon.dtos.seller.response;

import com.amazon.enums.OrderItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for an order item from the seller's perspective.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerOrderItemResponseDto {

    private UUID orderItemId;
    private UUID orderId;
    private String productTitle;
    private String variantName;
    private String asin;
    private String sellerSku;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private OrderItemStatus itemStatus;
    private String buyerName;
    private LocalDateTime orderDate;
}
