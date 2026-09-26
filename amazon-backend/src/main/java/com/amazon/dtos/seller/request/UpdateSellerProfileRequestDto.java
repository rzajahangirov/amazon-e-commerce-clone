package com.amazon.dtos.seller.request;

import com.amazon.payloads.ApiPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating seller profile fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSellerProfileRequestDto implements ApiPayload {

    private String storeName;
    private String businessAddress;
    private String bankAccountDetails;
}
