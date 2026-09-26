package com.amazon.dtos.product.request;

import com.amazon.enums.ProductStatus;
import com.amazon.payloads.ApiPayload;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for updating an existing product definition.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductRequestDto implements ApiPayload {

    private UUID categoryId;
    private UUID brandId;

    @Size(min = 3, max = 255, message = "Product title must be between 3 and 255 characters")
    private String title;

    private String description;
    private BigDecimal basePrice;
    private ProductStatus status;
}
