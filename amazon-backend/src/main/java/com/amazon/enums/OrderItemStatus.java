package com.amazon.enums;

/**
 * Fulfillment and lifecycle status of an individual item within an order.
 * Corresponds to DBML enum order_item_status_enum { pending, shipped, delivered, cancelled, returned }.
 */
public enum OrderItemStatus {
    PENDING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    RETURNED
}
