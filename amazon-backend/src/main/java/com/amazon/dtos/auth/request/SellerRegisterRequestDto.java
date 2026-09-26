package com.amazon.dtos.auth.request;

import com.amazon.payloads.ApiPayload;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for standalone third-party (3P) seller registration.
 * Captures user credentials and essential business/store details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerRegisterRequestDto implements ApiPayload {

    // User account details
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Phone number is required")
    private String phone;

    // Store / Business details
    @NotBlank(message = "Store name is required")
    @Size(min = 2, max = 150, message = "Store name must be between 2 and 150 characters")
    private String storeName;

    @NotBlank(message = "Tax number is required")
    @Size(min = 2, max = 100, message = "Tax number must be between 2 and 100 characters")
    private String taxNumber;

    @NotBlank(message = "Business address is required")
    @Size(min = 5, max = 500, message = "Business address must be between 5 and 500 characters")
    private String businessAddress;

    private String bankAccountDetails;
}
