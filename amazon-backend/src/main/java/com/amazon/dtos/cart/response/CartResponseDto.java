package com.amazon.dtos.cart.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for the complete shopping cart.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponseDto {

    private UUID id;
    private UUID userId;
    private List<CartItemResponseDto> items;
    private Integer totalActiveItems;
    private Integer totalSavedForLaterItems;
    private BigDecimal activeSubtotal;
}
