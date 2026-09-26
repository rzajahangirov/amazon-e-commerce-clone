package com.amazon.payloads;

/**
 * Centralized enum for authentication-related error messages.
 * Eliminates scattered string literals and ensures consistent error messaging.
 */
public enum AuthError {

    EMAIL_ALREADY_EXISTS("This email is already registered"),
    INVALID_CREDENTIALS("Email or password is incorrect"),
    USER_NOT_FOUND("User not found"),
    ACCOUNT_INACTIVE("User account is inactive, suspended, or deleted"),
    ROLE_NOT_FOUND("Role not found");

    private final String message;

    AuthError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
