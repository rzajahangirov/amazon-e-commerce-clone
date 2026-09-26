package com.amazon.dtos.brand.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for rejecting a Brand Update Request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectUpdateRequestDto {

    @NotBlank(message = "Rejection reason is required")
    private String rejectionReason;
}
