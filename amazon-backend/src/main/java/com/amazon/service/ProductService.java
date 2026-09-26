package com.amazon.service;

import com.amazon.dtos.product.response.ProductResponseDto;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;

import java.util.UUID;

/**
 * Service interface for Product aggregate operations.
 */
public interface ProductService {

    ResponseDto<ProductResponseDto> getProductById(UUID id);

    ResponseDto<PaginationPayload<ProductResponseDto>> getProducts(UUID categoryId, int page, int size);

}
