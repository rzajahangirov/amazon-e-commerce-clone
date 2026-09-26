package com.amazon.dtos.admin.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUserStatusRequestDto {
    @NotNull
    private Boolean active;
}
