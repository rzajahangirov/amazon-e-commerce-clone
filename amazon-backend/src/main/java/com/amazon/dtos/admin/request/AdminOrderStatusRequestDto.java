package com.amazon.dtos.admin.request;

import com.amazon.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminOrderStatusRequestDto {
    @NotNull
    private OrderStatus status;
}
