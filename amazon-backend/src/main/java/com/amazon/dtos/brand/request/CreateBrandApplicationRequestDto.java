package com.amazon.dtos.brand.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for submitting a new Brand Registration Application.
 * Adheres to Senior Backend Developer Guidelines Section 4.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBrandApplicationRequestDto {

    @NotBlank(message = "Applicant name is required")
    @Size(min = 2, max = 150, message = "Applicant name must be between 2 and 150 characters")
    private String applicantName;

    @NotBlank(message = "Applicant email is required")
    @Email(message = "Invalid applicant email address")
    private String applicantEmail;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    private String applicantPhone;

    @NotBlank(message = "Brand name is required")
    @Size(min = 2, max = 150, message = "Brand name must be between 2 and 150 characters")
    private String brandName;

    @NotBlank(message = "Brand slug is required")
    @Size(min = 2, max = 150, message = "Brand slug must be between 2 and 150 characters")
    private String brandSlug;

    @NotBlank(message = "Trademark registration number is required")
    @Size(min = 2, max = 100, message = "Trademark registration number must be between 2 and 100 characters")
    private String trademarkRegistrationNumber;

    @NotBlank(message = "Brand country is required")
    @Size(min = 2, max = 100, message = "Brand country must be between 2 and 100 characters")
    private String brandCountry;

    private String logoUrl;

    private String aboutText;
}
