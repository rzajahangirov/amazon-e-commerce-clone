package com.amazon.service;

import com.amazon.dtos.brand.request.RejectUpdateRequestDto;
import com.amazon.dtos.brand.response.BrandResponseDto;
import com.amazon.dtos.brand.response.BrandUpdateRequestResponseDto;
import com.amazon.enums.BrandUpdateRequestStatus;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;

import java.util.UUID;

/**
 * Service interface for Platform Admin brand governance, brand update request reviews, and brand lifecycle control.
 */
public interface AdminBrandManagementService {

    ResponseDto<PaginationPayload<BrandUpdateRequestResponseDto>> getUpdateRequests(BrandUpdateRequestStatus status, int page, int size);

    ResponseDto<BrandResponseDto> approveUpdateRequest(UUID requestId);

    ResponseDto<BrandUpdateRequestResponseDto> rejectUpdateRequest(UUID requestId, RejectUpdateRequestDto request);

    ResponseDto<PaginationPayload<BrandResponseDto>> getAllBrands(int page, int size);

    ResponseDto<Void> deleteBrand(UUID brandId);
}
