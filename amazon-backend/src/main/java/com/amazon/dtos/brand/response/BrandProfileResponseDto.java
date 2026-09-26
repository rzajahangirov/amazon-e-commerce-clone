package com.amazon.dtos.brand.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Composite Response DTO representing full brand profile information along with team members.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandProfileResponseDto {

    private BrandResponseDto brand;
    private List<BrandMemberResponseDto> teamMembers;
}
