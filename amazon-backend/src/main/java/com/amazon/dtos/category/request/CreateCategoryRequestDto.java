package com.amazon.dtos.category.request;

import com.amazon.payloads.ApiPayload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for creating a new product category.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCategoryRequestDto implements ApiPayload {

    private UUID parentId;

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 150, message = "Category name must be between 2 and 150 characters")
    private String name;

    @NotBlank(message = "Category slug is required")
    @Size(min = 2, max = 150, message = "Category slug must be between 2 and 150 characters")
    private String slug;

    private Integer level;
}
