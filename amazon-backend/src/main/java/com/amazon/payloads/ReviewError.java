package com.amazon.payloads;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Standardized error messages for review and rating operations.
 * Adheres to Senior Backend Developer Guidelines Section 8.2.
 */
@Getter
@RequiredArgsConstructor
public enum ReviewError {

    REVIEW_NOT_FOUND("Review not found with given identifier"),
    DUPLICATE_REVIEW("You have already submitted a review for this product"),
    REVIEW_ACCESS_DENIED("You are not authorized to modify or delete this review"),
    INVALID_RATING("Rating must be between 1 and 5 stars"),
    PRODUCT_NOT_ACTIVE("Reviews can only be submitted for active products"),
    REVIEW_PRODUCT_MISMATCH("Review does not belong to the specified product");

    private final String message;
}
