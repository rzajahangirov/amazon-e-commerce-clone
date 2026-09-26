package com.amazon.dtos.brand.request;

import com.amazon.enums.BrandRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating an employee account and adding them to the brand organization.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddBrandMemberRequestDto {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 150, message = "Full name must be between 2 and 150 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    private String phone;

    @NotNull(message = "Brand role is required")
    private BrandRole brandRole;
}
