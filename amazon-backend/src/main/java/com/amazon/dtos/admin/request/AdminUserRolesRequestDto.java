package com.amazon.dtos.admin.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Set;

@Data
public class AdminUserRolesRequestDto {
    @NotEmpty
    private Set<@NotBlank String> roles;
}
