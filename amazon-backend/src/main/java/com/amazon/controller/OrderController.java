package com.amazon.controller;

import com.amazon.dtos.order.request.CheckoutRequestDto;
import com.amazon.dtos.order.request.UpdateOrderStatusRequestDto;
import com.amazon.dtos.order.response.OrderResponseDto;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;
import com.amazon.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

/**
 * REST controller for Order checkout and lifecycle management.
 * Strictly adheres to Senior Backend Developer Guidelines (Principal pattern, zero entity leakage).
 */
@RestController
@RequestMapping("v1/api/orders")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Customer checkout and order lifecycle management")
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    @Operation(summary = "Checkout cart", description = "Converts active cart items into a confirmed customer order with stock deduction")
    public ResponseEntity<ResponseDto<OrderResponseDto>> checkout(
            @Valid @RequestBody CheckoutRequestDto request,
            Principal principal) {
        String email = principal.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.checkoutFromCart(email, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieves order details for the authenticated customer or administrator")
    public ResponseEntity<ResponseDto<OrderResponseDto>> getOrderById(
            @PathVariable UUID id,
            Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(orderService.getOrderById(id, email));
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by order number", description = "Retrieves order details by public order number (e.g., AMZ-12345)")
    public ResponseEntity<ResponseDto<OrderResponseDto>> getOrderByOrderNumber(
            @PathVariable String orderNumber,
            Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(orderService.getOrderByOrderNumber(orderNumber, email));
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Get my orders", description = "Retrieves paginated orders placed by the authenticated customer")
    public ResponseEntity<ResponseDto<PaginationPayload<OrderResponseDto>>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(orderService.getMyOrders(email, page, size));
    }

    /**
     * Top-level Order encompasses multi-seller order items; only Platform Admins can override root
     * order status for global dispute resolution/fraud cancellation.
     * Normal sellers MUST use PUT /v1/api/seller-dashboard/orders/{orderItemId}/status to manage item-level fulfillment.
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update order status", description = "Updates order fulfillment status (e.g. PROCESSING, SHIPPED, DELIVERED)")
    public ResponseEntity<ResponseDto<OrderResponseDto>> updateOrderStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequestDto request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel order", description = "Cancels an order and automatically restores stock inventory back to listings")
    public ResponseEntity<ResponseDto<OrderResponseDto>> cancelOrder(
            @PathVariable UUID id,
            Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(orderService.cancelOrder(id, email));
    }
}
