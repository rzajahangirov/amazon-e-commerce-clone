package com.amazon.service;

import com.amazon.dtos.product.request.ProductSearchRequestDto;
import com.amazon.dtos.product.response.ProductResponseDto;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;

import java.util.UUID;

/**
 * Service interface for Product aggregate, search, and discovery operations.
 */
public interface ProductService {

    ResponseDto<ProductResponseDto> getProductById(UUID id);

    ResponseDto<ProductResponseDto> getProductById(UUID id, String userEmail);

    ResponseDto<PaginationPayload<ProductResponseDto>> getProducts(UUID categoryId, int page, int size);

    ResponseDto<PaginationPayload<ProductResponseDto>> getProducts(UUID categoryId, int page, int size, String userEmail);

    ResponseDto<PaginationPayload<ProductResponseDto>> searchProducts(ProductSearchRequestDto request, String userEmail);

    /**
     * Returns up to 3 recommended complementary products for the "Frequently Bought Together" section.
     */
    ResponseDto<java.util.List<ProductResponseDto>> getFrequentlyBoughtTogether(UUID productId, String userEmail);
}
