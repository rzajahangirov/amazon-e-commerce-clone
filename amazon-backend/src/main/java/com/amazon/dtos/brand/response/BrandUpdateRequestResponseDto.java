package com.amazon.dtos.brand.response;

import com.amazon.enums.BrandUpdateRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO representing a proposed brand profile modification request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandUpdateRequestResponseDto {

    private UUID id;
    private UUID brandId;
    private String brandName;
    private UUID requestedByUserId;
    private String requestedByUserEmail;
    private String proposedBrandName;
    private String proposedLogoUrl;
    private String proposedAboutText;
    private String proposedTrademarkNo;
    private String officialLegalJustification;
    private BrandUpdateRequestStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
