package com.amazon.payloads;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic pagination wrapper for list-returning endpoints.
 * Encapsulates page content along with pagination metadata.
 *
 * @param <T> the type of elements in the page content
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaginationPayload<T> {

    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
