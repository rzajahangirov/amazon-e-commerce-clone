package com.amazon.dtos.brand.response;

import com.amazon.enums.BrandRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO representing a member of a brand organization.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandMemberResponseDto {

    private UUID id;
    private UUID brandId;
    private String brandName;
    private UUID userId;
    private String userFullName;
    private String userEmail;
    private BrandRole brandRole;
    private String department;
    private LocalDateTime assignedAt;
}
