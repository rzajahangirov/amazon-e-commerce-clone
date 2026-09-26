package com.amazon.dtos.brand.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for rejecting a Brand Application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectApplicationRequestDto {

    @NotBlank(message = "Rejection reason is required")
    private String rejectionReason;
}
