package com.amazon.dtos.order.response;

import com.amazon.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for a customer order.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {

    private UUID id;
    private String orderNumber;
    private UUID userId;
    private String userEmail;
    private UUID shippingAddressId;
    private String shippingFullName;
    private String shippingStreetLine1;
    private String shippingStreetLine2;
    private String shippingCity;
    private String shippingState;
    private String shippingPostalCode;
    private String shippingCountry;
    private String shippingPhone;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime placedAt;
    private List<OrderItemResponseDto> items;
}
