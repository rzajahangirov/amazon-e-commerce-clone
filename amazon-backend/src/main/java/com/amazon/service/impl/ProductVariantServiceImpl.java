package com.amazon.service.impl;

import com.amazon.dtos.listing.response.ProductListingResponseDto;
import com.amazon.dtos.product.request.CreateVariantRequestDto;
import com.amazon.dtos.product.response.ProductVariantResponseDto;
import com.amazon.entity.Product;
import com.amazon.entity.ProductVariant;
import com.amazon.exception.DuplicateResourceException;
import com.amazon.exception.ResourceNotFoundException;
import com.amazon.payloads.ApiResponse;
import com.amazon.payloads.CatalogError;
import com.amazon.payloads.ResponseDto;
import com.amazon.repository.ProductRepository;
import com.amazon.repository.ProductVariantRepository;
import com.amazon.repository.BrandMemberRepository;
import com.amazon.enums.BrandRole;
import org.springframework.security.access.AccessDeniedException;
import com.amazon.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Service implementation for managing product variants.
 * Strictly adheres to Senior Backend Developer Guidelines.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final BrandMemberRepository brandMemberRepository;

    @Override
    @Transactional
    public ResponseDto<ProductVariantResponseDto> createVariant(UUID productId, CreateVariantRequestDto request, String callerEmail) {
        var member = brandMemberRepository.findFirstByUserEmail(callerEmail)
                .orElseThrow(() -> new AccessDeniedException("Brand catalog administration membership is required"));
        if (member.getBrandRole() != BrandRole.BRAND_OWNER && member.getBrandRole() != BrandRole.BRAND_SUPER_ADMIN
                && member.getBrandRole() != BrandRole.BRAND_ADMIN) {
            throw new AccessDeniedException("Only brand administrators can register ASINs");
        }
        String asin = request.getAsin().toUpperCase().trim();
        if (productVariantRepository.existsByAsin(asin)) {
            throw new DuplicateResourceException(CatalogError.VARIANT_ASIN_EXISTS.getMessage());
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.PRODUCT_NOT_FOUND.getMessage()));

        if (product.getBrand() == null || product.getBrand().getStatus() != com.amazon.enums.BrandStatus.ACTIVE
                || product.getStatus() != com.amazon.enums.ProductStatus.ACTIVE
                || !Boolean.TRUE.equals(product.getCategory().getIsApproved())) {
            throw new com.amazon.exception.BusinessRuleException("ASINs can only be registered under active brand catalog products in approved categories");
        }
        if (!product.getBrand().getId().equals(member.getBrand().getId())) {
            throw new AccessDeniedException("Product does not belong to the caller's brand");
        }

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .asin(asin)
                .variantName(request.getVariantName())
                .variantAttributes(request.getVariantAttributes() != null ? request.getVariantAttributes() : new HashMap<>())
                .build();

        ProductVariant saved = productVariantRepository.save(variant);
        log.info("Product variant created successfully with id: {} and ASIN: {}", saved.getId(), saved.getAsin());

        return ApiResponse.success(mapToResponseDto(saved), "Product variant created successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<ProductVariantResponseDto> getVariantById(UUID id) {
        ProductVariant variant = productVariantRepository.findWithListingsById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.VARIANT_NOT_FOUND.getMessage()));

        return ApiResponse.success(mapToResponseDto(variant), "Product variant retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<ProductVariantResponseDto> getVariantByAsin(String asin) {
        ProductVariant variant = productVariantRepository.findByAsin(asin.toUpperCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.VARIANT_NOT_FOUND.getMessage()));

        if (variant.getProduct().getBrand() == null
                || variant.getProduct().getBrand().getStatus() != com.amazon.enums.BrandStatus.ACTIVE
                || variant.getProduct().getStatus() != com.amazon.enums.ProductStatus.ACTIVE
                || !Boolean.TRUE.equals(variant.getProduct().getCategory().getIsApproved())) {
            throw new ResourceNotFoundException(CatalogError.VARIANT_NOT_FOUND.getMessage());
        }

        return ApiResponse.success(mapToResponseDto(variant), "Product variant retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<List<ProductVariantResponseDto>> getVariantsByProductId(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.PRODUCT_NOT_FOUND.getMessage()));
        if (product.getBrand() == null || product.getBrand().getStatus() != com.amazon.enums.BrandStatus.ACTIVE
                || product.getStatus() != com.amazon.enums.ProductStatus.ACTIVE
                || !Boolean.TRUE.equals(product.getCategory().getIsApproved())) {
            throw new ResourceNotFoundException(CatalogError.PRODUCT_NOT_FOUND.getMessage());
        }
        List<ProductVariant> variants = productVariantRepository.findByProductId(productId);
        List<ProductVariantResponseDto> responseDtos = variants.stream()
                .map(this::mapToResponseDto)
                .toList();

        return ApiResponse.success(responseDtos, "Product variants retrieved successfully");
    }

    private ProductVariantResponseDto mapToResponseDto(ProductVariant variant) {
        List<ProductListingResponseDto> listingDtos = variant.getListings() != null
                ? variant.getListings().stream().map(l -> ProductListingResponseDto.builder()
                        .id(l.getId())
                        .productVariantId(variant.getId())
                        .variantAsin(variant.getAsin())
                        .variantName(variant.getVariantName())
                        .sellerId(l.getSeller() != null ? l.getSeller().getId() : null)
                        .sellerName(l.getSeller() != null ? l.getSeller().getFullName() : null)
                        .sellerSku(l.getSellerSku())
                        .price(l.getPrice())
                        .minPriceFloor(l.getMinPriceFloor())
                        .stockQuantity(l.getStockQuantity())
                        .fulfillmentType(l.getFulfillmentType())
                        .isBuyboxWinner(l.getIsBuyboxWinner())
                        .status(l.getStatus())
                        .createdAt(l.getCreatedAt())
                        .build()).toList()
                : List.of();

        return ProductVariantResponseDto.builder()
                .id(variant.getId())
                .productId(variant.getProduct() != null ? variant.getProduct().getId() : null)
                .asin(variant.getAsin())
                .variantName(variant.getVariantName())
                .variantAttributes(variant.getVariantAttributes())
                .listings(listingDtos)
                .createdAt(variant.getCreatedAt())
                .build();
    }
}
