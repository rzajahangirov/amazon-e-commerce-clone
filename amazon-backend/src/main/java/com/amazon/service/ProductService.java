package com.amazon.service;

import com.amazon.dtos.product.request.CreateProductRequestDto;
import com.amazon.dtos.product.request.UpdateProductRequestDto;
import com.amazon.dtos.product.response.ProductResponseDto;
import com.amazon.enums.ProductStatus;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;

import java.util.UUID;

/**
 * Service interface for Product aggregate operations.
 */
public interface ProductService {

    ResponseDto<ProductResponseDto> createProduct(CreateProductRequestDto request, String sellerEmail);

    ResponseDto<ProductResponseDto> getProductById(UUID id);

    ResponseDto<PaginationPayload<ProductResponseDto>> getProducts(UUID categoryId, ProductStatus status, int page, int size);

    ResponseDto<ProductResponseDto> updateProduct(UUID id, UpdateProductRequestDto request, String sellerEmail);

    ResponseDto<ProductResponseDto> updateProductStatus(UUID id, ProductStatus status);
}
