package com.amazon.enums;

/**
 * Status of a customer order.
 * Corresponds to DBML enum order_status_enum { pending, confirmed, processing, shipped, delivered, cancelled, refunded }.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}
