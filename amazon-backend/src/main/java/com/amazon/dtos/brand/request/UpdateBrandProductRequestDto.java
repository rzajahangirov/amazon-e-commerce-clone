package com.amazon.dtos.brand.request;

import com.amazon.enums.GovernanceStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for updating brand-owned product details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBrandProductRequestDto {

    @Size(min = 3, max = 255, message = "Product title must be between 3 and 255 characters")
    private String title;

    private String description;

    private UUID categoryId;

    @Size(max = 100, message = "Master SKU must not exceed 100 characters")
    private String masterSku;

    private GovernanceStatus governanceStatus;
}
