package com.amazon.payloads;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Standardized error messages for order operations.
 * Adheres to Senior Backend Developer Guidelines Section 8.2.
 */
@Getter
@RequiredArgsConstructor
public enum OrderError {

    ORDER_NOT_FOUND("Order not found with given identifier"),
    EMPTY_CART_CHECKOUT("Cannot create order from an empty cart"),
    ORDER_ACCESS_DENIED("You do not have permission to view or modify this order"),
    ORDER_ALREADY_CANCELLED("Order has already been cancelled"),
    ORDER_CANNOT_BE_CANCELLED("Order cannot be cancelled in its current status"),
    INVALID_STATUS_TRANSITION("Invalid order status transition requested");

    private final String message;
}
