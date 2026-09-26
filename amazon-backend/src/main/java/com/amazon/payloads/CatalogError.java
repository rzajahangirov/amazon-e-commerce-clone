package com.amazon.payloads;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Standardized error messages for catalog operations (categories, products, variants, listings).
 * Adheres to Senior Backend Developer Guidelines Section 8.2.
 */
@Getter
@RequiredArgsConstructor
public enum CatalogError {

    CATEGORY_NOT_FOUND("Category not found with given identifier"),
    CATEGORY_NAME_EXISTS("Category with this name already exists"),
    CATEGORY_SLUG_EXISTS("Category with this slug already exists"),
    CATEGORY_NOT_APPROVED("Category is pending approval and cannot be used"),
    PARENT_CATEGORY_NOT_FOUND("Parent category not found"),
    PRODUCT_NOT_FOUND("Product not found with given identifier"),
    VARIANT_NOT_FOUND("Product variant not found with given identifier"),
    VARIANT_ASIN_EXISTS("Product variant with this ASIN already exists"),
    LISTING_NOT_FOUND("Product listing not found with given identifier"),
    INSUFFICIENT_STOCK("Insufficient stock available for listing"),
    SELLER_NOT_AUTHORIZED("Seller is not authorized to modify this catalog entry"),
    INVALID_PRODUCT_STATUS("Invalid product status transition requested");

    private final String message;
}
