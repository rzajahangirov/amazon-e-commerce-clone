package com.amazon.payloads;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Standardized error messages for wishlist/favorites operations.
 * Adheres to Senior Backend Developer Guidelines Section 8.2.
 */
@Getter
@RequiredArgsConstructor
public enum WishlistError {

    PRODUCT_NOT_FOUND("Product not found with given identifier"),
    ITEM_NOT_IN_WISHLIST("Product is not in your wishlist"),
    WISHLIST_EMPTY("Your wishlist is already empty");

    private final String message;
}
