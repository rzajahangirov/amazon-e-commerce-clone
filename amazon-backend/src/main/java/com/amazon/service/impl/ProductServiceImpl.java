package com.amazon.service.impl;

import com.amazon.dtos.product.request.CreateProductRequestDto;
import com.amazon.dtos.product.request.UpdateProductRequestDto;
import com.amazon.dtos.product.response.ProductResponseDto;
import com.amazon.dtos.product.response.ProductVariantResponseDto;
import com.amazon.entity.Category;
import com.amazon.entity.Product;
import com.amazon.entity.User;
import com.amazon.enums.ProductStatus;
import com.amazon.exception.BusinessRuleException;
import com.amazon.exception.ResourceNotFoundException;
import com.amazon.payloads.ApiResponse;
import com.amazon.payloads.AuthError;
import com.amazon.payloads.CatalogError;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;
import com.amazon.repository.CategoryRepository;
import com.amazon.repository.ProductRepository;
import com.amazon.repository.UserRepository;
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
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ResponseDto<ProductResponseDto> createProduct(CreateProductRequestDto request, String sellerEmail) {
        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new ResourceNotFoundException(AuthError.USER_NOT_FOUND.getMessage()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.CATEGORY_NOT_FOUND.getMessage()));

        Product product = Product.builder()
                .seller(seller)
                .category(category)
                .brandId(request.getBrandId())
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .status(request.getStatus() != null ? request.getStatus() : ProductStatus.DRAFT)
                .build();

        Product saved = productRepository.save(product);
        log.info("Product created successfully with id: {} by seller: {}", saved.getId(), sellerEmail);

        return ApiResponse.success(mapToResponseDto(saved), "Product created successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<ProductResponseDto> getProductById(UUID id) {
        Product product = productRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.PRODUCT_NOT_FOUND.getMessage()));

        return ApiResponse.success(mapToResponseDto(product), "Product retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<PaginationPayload<ProductResponseDto>> getProducts(
            UUID categoryId, ProductStatus status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        ProductStatus searchStatus = status != null ? status : ProductStatus.ACTIVE;

        Page<Product> productPage;
        if (categoryId != null) {
            productPage = productRepository.findByCategoryIdAndStatus(categoryId, searchStatus, pageRequest);
        } else {
            productPage = productRepository.findByStatus(searchStatus, pageRequest);
        }

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

    @Override
    @Transactional
    public ResponseDto<ProductResponseDto> updateProduct(UUID id, UpdateProductRequestDto request, String sellerEmail) {
        Product product = productRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.PRODUCT_NOT_FOUND.getMessage()));

        User user = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new ResourceNotFoundException(AuthError.USER_NOT_FOUND.getMessage()));

        // Guard clause: Only product creator or admin can update product
        boolean isCreator = product.getSeller() != null && product.getSeller().getId().equals(user.getId());
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        if (!isCreator && !isAdmin) {
            throw new BusinessRuleException(CatalogError.SELLER_NOT_AUTHORIZED.getMessage());
        }

        if (request.getTitle() != null) {
            product.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getBasePrice() != null) {
            product.setBasePrice(request.getBasePrice());
        }
        if (request.getBrandId() != null) {
            product.setBrandId(request.getBrandId());
        }
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(CatalogError.CATEGORY_NOT_FOUND.getMessage()));
            product.setCategory(category);
        }

        Product updated = productRepository.save(product);
        log.info("Product updated successfully with id: {} by user: {}", updated.getId(), sellerEmail);

        return ApiResponse.success(mapToResponseDto(updated), "Product updated successfully");
    }

    @Override
    @Transactional
    public ResponseDto<ProductResponseDto> updateProductStatus(UUID id, ProductStatus status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.PRODUCT_NOT_FOUND.getMessage()));

        product.setStatus(status);
        Product saved = productRepository.save(product);
        log.info("Product status updated to: {} for product id: {}", status, id);

        return ApiResponse.success(mapToResponseDto(saved), "Product status updated successfully");
    }

    private ProductResponseDto mapToResponseDto(Product product) {
        List<ProductVariantResponseDto> variantDtos = product.getVariants() != null
                ? product.getVariants().stream().map(v -> ProductVariantResponseDto.builder()
                        .id(v.getId())
                        .productId(product.getId())
                        .asin(v.getAsin())
                        .variantName(v.getVariantName())
                        .variantAttributesJson(v.getVariantAttributesJson())
                        .createdAt(v.getCreatedAt())
                        .build()).toList()
                : List.of();

        return ProductResponseDto.builder()
                .id(product.getId())
                .sellerId(product.getSeller() != null ? product.getSeller().getId() : null)
                .sellerName(product.getSeller() != null ? product.getSeller().getFullName() : null)
                .brandId(product.getBrandId())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .title(product.getTitle())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .status(product.getStatus())
                .variants(variantDtos)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
