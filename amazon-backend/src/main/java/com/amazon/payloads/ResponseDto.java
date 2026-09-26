package com.amazon.payloads;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Universal HTTP response wrapper used throughout the application.
 * Both success and error responses are returned in this format.
 *
 * @param <T> the type of the response data payload
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseDto<T> {

    private T data;
    private String message;
}
