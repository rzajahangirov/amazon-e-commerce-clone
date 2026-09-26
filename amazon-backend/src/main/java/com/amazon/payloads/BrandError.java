package com.amazon.payloads;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Standardized domain error messages for Brand operations.
 * Adheres to Senior Backend Developer Guidelines Section 8.
 */
@Getter
@RequiredArgsConstructor
public enum BrandError {
    APPLICATION_NOT_FOUND("Brand application not found with id: "),
    APPLICATION_ALREADY_PROCESSED("Brand application has already been processed"),
    UPDATE_REQUEST_NOT_FOUND("Brand update request not found with id: "),
    UPDATE_REQUEST_ALREADY_PROCESSED("Brand update request has already been processed"),
    BRAND_NOT_FOUND("Brand not found with id: "),
    BRAND_NOT_FOUND_FOR_USER("No brand associated with current user"),
    BRAND_SLUG_ALREADY_EXISTS("A brand with this slug already exists: "),
    BRAND_NAME_ALREADY_EXISTS("A brand with this name already exists: "),
    BRAND_SUSPENDED("Brand is currently suspended"),
    MEMBER_NOT_FOUND("Brand member not found with id: "),
    MEMBER_ALREADY_EXISTS("User is already a member of this brand"),
    CANNOT_REMOVE_OWNER("Cannot remove the brand owner from the brand"),
    CANNOT_CHANGE_OWNER_ROLE("Cannot change role of brand owner"),
    POST_NOT_FOUND("Brand post not found with id: "),
    UNAUTHORIZED_BRAND_ACCESS("Unauthorized access to another brand's resources"),
    NOT_A_BRAND_MEMBER("Caller is not associated with any active brand"),
    INSUFFICIENT_BRAND_PERMISSIONS("Insufficient permissions to perform this brand operation");

    private final String message;
}
