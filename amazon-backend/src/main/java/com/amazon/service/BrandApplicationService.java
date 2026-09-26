package com.amazon.service;

import com.amazon.dtos.brand.request.CreateBrandApplicationRequestDto;
import com.amazon.dtos.brand.request.RejectApplicationRequestDto;
import com.amazon.dtos.brand.response.BrandApplicationResponseDto;
import com.amazon.dtos.brand.response.BrandResponseDto;
import com.amazon.enums.BrandApplicationStatus;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;

import java.util.UUID;

/**
 * Service interface for Brand Application submissions and approval/rejection lifecycle.
 */
public interface BrandApplicationService {

    ResponseDto<BrandApplicationResponseDto> submitApplication(CreateBrandApplicationRequestDto request);

    ResponseDto<PaginationPayload<BrandApplicationResponseDto>> getApplications(BrandApplicationStatus status, int page, int size);

    ResponseDto<BrandResponseDto> approveApplication(UUID applicationId);

    ResponseDto<BrandApplicationResponseDto> rejectApplication(UUID applicationId, RejectApplicationRequestDto request);
}
