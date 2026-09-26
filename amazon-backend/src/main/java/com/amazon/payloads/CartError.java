package com.amazon.payloads;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Standardized error messages for shopping cart operations.
 * Adheres to Senior Backend Developer Guidelines Section 8.2.
 */
@Getter
@RequiredArgsConstructor
public enum CartError {

    CART_NOT_FOUND("Shopping cart not found for user"),
    CART_ITEM_NOT_FOUND("Cart item not found with given identifier"),
    ITEM_NOT_IN_USER_CART("Item does not belong to the user's cart"),
    INVALID_QUANTITY("Item quantity must be at least 1"),
    LISTING_INACTIVE("Cannot add inactive listing to cart"),
    EXCEEDS_AVAILABLE_STOCK("Requested quantity exceeds available stock");

    private final String message;
}
