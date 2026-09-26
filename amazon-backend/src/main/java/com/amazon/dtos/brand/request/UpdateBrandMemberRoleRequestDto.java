package com.amazon.dtos.brand.request;

import com.amazon.enums.BrandRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for changing the brand role of an existing brand member.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBrandMemberRoleRequestDto {

    @NotNull(message = "Brand role is required")
    private BrandRole brandRole;
}
