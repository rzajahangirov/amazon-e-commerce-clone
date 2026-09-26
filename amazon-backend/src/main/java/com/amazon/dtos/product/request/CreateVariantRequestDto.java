package com.amazon.dtos.product.request;

import com.amazon.payloads.ApiPayload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new product variant.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVariantRequestDto implements ApiPayload {

    @NotBlank(message = "ASIN is required")
    @Size(min = 5, max = 20, message = "ASIN must be between 5 and 20 characters")
    private String asin;

    private String variantName;

    private String variantAttributesJson;
}
