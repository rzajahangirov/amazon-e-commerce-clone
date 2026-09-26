package com.amazon.dtos.admin.response;

import com.amazon.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AdminOrderResponseDto {
    private UUID id;
    private String orderNumber;
    private UUID buyerId;
    private String buyerEmail;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime placedAt;
}
