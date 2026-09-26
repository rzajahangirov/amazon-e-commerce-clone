package com.amazon.exception;

/**
 * Thrown when authentication fails due to invalid email or password.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
