package com.amazon.service;

import com.amazon.dtos.product.request.CreateVariantRequestDto;
import com.amazon.dtos.product.response.ProductVariantResponseDto;
import com.amazon.payloads.ResponseDto;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for ProductVariant aggregate operations.
 */
public interface ProductVariantService {

    ResponseDto<ProductVariantResponseDto> createVariant(UUID productId, CreateVariantRequestDto request);

    ResponseDto<ProductVariantResponseDto> getVariantById(UUID id);

    ResponseDto<ProductVariantResponseDto> getVariantByAsin(String asin);

    ResponseDto<List<ProductVariantResponseDto>> getVariantsByProductId(UUID productId);
}
