package com.amazon.payloads;

/**
 * Static factory helper for creating {@link ResponseDto} instances.
 * Controllers and exception handlers should always use these methods
 * instead of constructing {@code ResponseDto} directly.
 */
public class ApiResponse {

    private ApiResponse() {
        // Utility class â€” prevent instantiation
    }

    public static <T> ResponseDto<T> success(T data, String message) {
        return ResponseDto.<T>builder().data(data).message(message).build();
    }

    public static <T> ResponseDto<T> success(T data) {
        return success(data, "Successful operation");
    }

    public static <T> ResponseDto<T> error(String message) {
        return ResponseDto.<T>builder().data(null).message(message).build();
    }
}
