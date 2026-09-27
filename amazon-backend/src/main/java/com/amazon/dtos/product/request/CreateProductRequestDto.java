package com.amazon.dtos.product.request;

import com.amazon.enums.GovernanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for creating a brand catalog product template.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequestDto {

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @NotBlank(message = "Product title is required")
    @Size(min = 3, max = 255, message = "Product title must be between 3 and 255 characters")
    private String title;

    private String description;

    @Size(max = 100, message = "Master SKU must not exceed 100 characters")
    private String masterSku;

    private GovernanceStatus governanceStatus;
}
