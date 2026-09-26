package com.amazon.dtos.brand.response;

import com.amazon.enums.BrandStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO representing an approved Brand.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandResponseDto {

    private UUID id;
    private String name;
    private String slug;
    private String trademarkRegistrationNumber;
    private String brandCountry;
    private String logoUrl;
    private String aboutText;
    private BrandStatus status;
    private UUID ownerUserId;
    private String ownerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
