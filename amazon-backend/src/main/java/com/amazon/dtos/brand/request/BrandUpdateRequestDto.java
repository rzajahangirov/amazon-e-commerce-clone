package com.amazon.dtos.brand.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for submitting proposed brand metadata modifications.
 * Alias / Enterprise standard DTO for brand update requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandUpdateRequestDto {

    @Size(min = 2, max = 150, message = "Proposed brand name must be between 2 and 150 characters")
    private String proposedBrandName;

    private String proposedLogoUrl;

    private String proposedAboutText;

    @Size(min = 2, max = 100, message = "Proposed trademark number must be between 2 and 100 characters")
    private String proposedTrademarkNo;

    @Size(max = 500, message = "Official legal justification must not exceed 500 characters")
    private String officialLegalJustification;
}
