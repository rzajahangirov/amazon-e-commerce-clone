package com.amazon.dtos.admin.response;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class AdminSellerResponseDto {
    private UUID id;
    private UUID userId;
    private String email;
    private String fullName;
    private String storeName;
    private UUID brandId;
    private String brandName;
    private Boolean verified;
    private boolean active;
}
