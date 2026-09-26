package com.amazon.service;

import com.amazon.dtos.order.request.CheckoutRequestDto;
import com.amazon.dtos.order.request.UpdateOrderStatusRequestDto;
import com.amazon.dtos.order.response.OrderResponseDto;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;

import java.util.UUID;

/**
 * Service interface for Order lifecycle and checkout operations.
 */
public interface OrderService {

    ResponseDto<OrderResponseDto> checkoutFromCart(String userEmail, CheckoutRequestDto request);

    ResponseDto<OrderResponseDto> getOrderById(UUID orderId, String userEmail);

    ResponseDto<OrderResponseDto> getOrderByOrderNumber(String orderNumber, String userEmail);

    ResponseDto<PaginationPayload<OrderResponseDto>> getMyOrders(String userEmail, int page, int size);

    ResponseDto<OrderResponseDto> updateOrderStatus(UUID orderId, UpdateOrderStatusRequestDto request);

    ResponseDto<OrderResponseDto> cancelOrder(UUID orderId, String userEmail);
}
