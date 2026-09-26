package com.amazon.exception;

/**
 * Thrown when a business rule is violated (e.g., invalid state transition,
 * unauthorized operation, constraint violation, etc.)
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
