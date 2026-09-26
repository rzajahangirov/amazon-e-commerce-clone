package com.amazon.payloads;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Standardized error messages for 3P Seller operations.
 */
@Getter
@RequiredArgsConstructor
public enum SellerError {

    SELLER_NOT_FOUND("Seller profile not found for user"),
    STORE_NAME_ALREADY_EXISTS("Store name is already taken"),
    LISTING_ACCESS_DENIED("You do not own this listing"),
    ORDER_ITEM_ACCESS_DENIED("You do not own this order line item"),
    ORDER_ITEM_NOT_FOUND("Order item not found"),
    INVALID_STATUS_TRANSITION("Invalid fulfillment status transition");

    private final String message;
}
