package com.amazon.service.impl;

import com.amazon.dtos.product.response.ProductResponseDto;
import com.amazon.dtos.product.response.ProductVariantResponseDto;
import com.amazon.entity.Product;
import com.amazon.exception.ResourceNotFoundException;
import com.amazon.payloads.ApiResponse;
import com.amazon.payloads.CatalogError;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;
import com.amazon.repository.ProductRepository;
import com.amazon.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service implementation for managing product definitions.
 * Strictly adheres to Senior Backend Developer Guidelines.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    @Override
    @Transactional(readOnly = true)
    public ResponseDto<ProductResponseDto> getProductById(UUID id) {
        Product product = productRepository.findPublishedWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.PRODUCT_NOT_FOUND.getMessage()));

        return ApiResponse.success(mapToResponseDto(product), "Product retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<PaginationPayload<ProductResponseDto>> getProducts(UUID categoryId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> productPage = productRepository.findPublished(categoryId, pageRequest);

        List<ProductResponseDto> content = productPage.getContent().stream()
                .map(this::mapToResponseDto)
                .toList();

        PaginationPayload<ProductResponseDto> paginationPayload = PaginationPayload.<ProductResponseDto>builder()
                .content(content)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .last(productPage.isLast())
                .build();

        return ApiResponse.success(paginationPayload, "Products retrieved successfully");
    }

    private ProductResponseDto mapToResponseDto(Product product) {
        List<ProductVariantResponseDto> variantDtos = product.getVariants() != null
                ? product.getVariants().stream().map(v -> ProductVariantResponseDto.builder()
                        .id(v.getId())
                        .productId(product.getId())
                        .asin(v.getAsin())
                        .variantName(v.getVariantName())
                        .variantAttributes(v.getVariantAttributes())
                        .createdAt(v.getCreatedAt())
                        .build()).toList()
                : List.of();

        return ProductResponseDto.builder()
                .id(product.getId())
                .sellerId(product.getSeller() != null ? product.getSeller().getId() : null)
                .sellerName(product.getSeller() != null ? product.getSeller().getFullName() : null)
                .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .title(product.getTitle())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .status(product.getStatus())
                .variants(variantDtos)
                .averageRating(product.getAverageRating() != null ? product.getAverageRating() : 0.0)
                .totalReviews(product.getTotalReviews() != null ? product.getTotalReviews() : 0)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
