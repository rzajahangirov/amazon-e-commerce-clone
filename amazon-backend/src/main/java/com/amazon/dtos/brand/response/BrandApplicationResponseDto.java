package com.amazon.dtos.brand.response;

import com.amazon.enums.BrandApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for Brand Registration Application details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandApplicationResponseDto {

    private UUID id;
    private String applicantName;
    private String applicantEmail;
    private String applicantPhone;
    private String brandName;
    private String brandSlug;
    private String trademarkRegistrationNumber;
    private String brandCountry;
    private String logoUrl;
    private String aboutText;
    private BrandApplicationStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
