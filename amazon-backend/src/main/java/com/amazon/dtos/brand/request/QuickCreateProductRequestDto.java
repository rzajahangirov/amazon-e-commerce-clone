package com.amazon.dtos.brand.request;

import com.amazon.enums.FulfillmentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for atomic creation of Product, Variant, and Buy-Box Listing in a single transaction.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickCreateProductRequestDto {

    @NotBlank(message = "Product title is required")
    @Size(min = 3, max = 255, message = "Product title must be between 3 and 255 characters")
    private String title;

    private String description;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @NotNull(message = "Base price is required")
    @Positive(message = "Base price must be greater than zero")
    private BigDecimal basePrice;

    @NotBlank(message = "ASIN is required")
    @Size(min = 5, max = 20, message = "ASIN must be between 5 and 20 characters")
    private String asin;

    @NotBlank(message = "Variant name is required")
    @Size(min = 1, max = 150, message = "Variant name must be between 1 and 150 characters")
    private String variantName;

    private Map<String, Object> variantAttributes;

    @NotBlank(message = "Seller SKU is required")
    @Size(min = 1, max = 100, message = "Seller SKU must be between 1 and 100 characters")
    private String sellerSku;

    @NotNull(message = "Listing price is required")
    @Positive(message = "Price must be greater than zero")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @Builder.Default
    private FulfillmentType fulfillmentType = FulfillmentType.FBM;
}
