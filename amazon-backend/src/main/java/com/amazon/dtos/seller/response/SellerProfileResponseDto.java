package com.amazon.dtos.seller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for Seller Profile information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerProfileResponseDto {

    private UUID id;
    private UUID userId;
    private String fullName;
    private String email;
    private String storeName;
    private String taxNumber;
    private String businessAddress;
    private String bankAccountDetails;
    private Boolean isVerified;
    private UUID brandId;
    private String brandName;
    private String supportEmail;
    private String merchantPhone;
    private String returnPolicyUrl;
    private String legalName;
    private String stateTaxPermitNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
