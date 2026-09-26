package com.amazon.dtos.admin.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RejectCategoryRequestDto {
    @Size(max = 1000)
    private String reason;
}
