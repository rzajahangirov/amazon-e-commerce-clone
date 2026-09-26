package com.amazon.dtos.product.request;

import com.amazon.enums.ProductStatus;
import com.amazon.payloads.ApiPayload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for creating a new product definition.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequestDto implements ApiPayload {

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    private UUID brandId;

    @NotBlank(message = "Product title is required")
    @Size(min = 3, max = 255, message = "Product title must be between 3 and 255 characters")
    private String title;

    private String description;

    @PositiveOrZero(message = "Base price must be greater than or equal to zero")
    private BigDecimal basePrice;

    private ProductStatus status;
}
