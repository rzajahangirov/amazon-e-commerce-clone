package com.amazon.service.impl;

import com.amazon.dtos.product.request.ProductSearchRequestDto;
import com.amazon.dtos.product.response.ProductResponseDto;
import com.amazon.dtos.product.response.ProductVariantResponseDto;
import com.amazon.entity.Category;
import com.amazon.entity.Product;
import com.amazon.entity.User;
import com.amazon.enums.ProductSortBy;
import com.amazon.exception.ResourceNotFoundException;
import com.amazon.payloads.ApiResponse;
import com.amazon.payloads.CatalogError;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;
import com.amazon.repository.*;
import com.amazon.service.ProductService;
import com.amazon.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Service implementation for managing product catalog, discovery, performance metrics,
 * and Amazon-style multi-criteria search.
 * Strictly adheres to Senior Backend Developer Guidelines.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final CategoryRepository categoryRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductListingRepository productListingRepository;

    @Override
    @Transactional
    public ResponseDto<ProductResponseDto> getProductById(UUID id) {
        return getProductById(id, null);
    }

    @Override
    @Transactional
    public ResponseDto<ProductResponseDto> getProductById(UUID id, String userEmail) {
        // Atomically increment view count on product detail fetch
        productRepository.incrementViewCount(id);

        Product product = productRepository.findPublishedWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.PRODUCT_NOT_FOUND.getMessage()));

        boolean isFavorited = false;
        if (userEmail != null) {
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isPresent()) {
                isFavorited = wishlistItemRepository.existsByUserIdAndProductId(userOpt.get().getId(), id);
            }
        }

        // Aggregate units sold from delivered order items
        Long deliveredCount = orderItemRepository.countDeliveredUnitsSoldByProductId(id);
        long totalUnitsSold = Math.max(product.getTotalUnitsSold(), deliveredCount != null ? deliveredCount : 0L);

        // Fetch active buybox winner price, or fallback to product base price
        BigDecimal buyBoxPrice = productListingRepository.findBuyBoxPriceByProductId(id)
                .orElse(product.getBasePrice());

        return ApiResponse.success(
                mapToResponseDto(product, isFavorited, totalUnitsSold, buyBoxPrice),
                "Product retrieved successfully"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<PaginationPayload<ProductResponseDto>> getProducts(UUID categoryId, int page, int size) {
        return getProducts(categoryId, page, size, null);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<PaginationPayload<ProductResponseDto>> getProducts(UUID categoryId, int page, int size, String userEmail) {
        ProductSearchRequestDto searchRequest = ProductSearchRequestDto.builder()
                .categoryId(categoryId)
                .page(page)
                .size(size)
                .sortBy(ProductSortBy.FEATURED)
                .build();
        return searchProducts(searchRequest, userEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<PaginationPayload<ProductResponseDto>> searchProducts(ProductSearchRequestDto request, String userEmail) {
        // 1. Resolve category hierarchy if category filter requested
        Set<UUID> resolvedCategoryIds = null;
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndIsApprovedTrue(request.getCategoryId()).orElse(null);
            resolvedCategoryIds = collectCategoryHierarchyIds(category);
        } else if (request.getCategorySlug() != null && !request.getCategorySlug().trim().isEmpty()) {
            Category category = categoryRepository.findBySlugAndIsApprovedTrue(request.getCategorySlug().trim()).orElse(null);
            resolvedCategoryIds = collectCategoryHierarchyIds(category);
        }

        // 2. Resolve database sorting
        ProductSortBy sortBy = request.getSortBy() != null ? request.getSortBy() : ProductSortBy.FEATURED;
        Sort sort = switch (sortBy) {
            case BEST_SELLERS -> Sort.by(Sort.Direction.DESC, "totalUnitsSold").and(Sort.by(Sort.Direction.DESC, "createdAt"));
            case AVG_CUSTOMER_REVIEW -> Sort.by(Sort.Direction.DESC, "averageRating").and(Sort.by(Sort.Direction.DESC, "totalReviews"));
            case MOST_VIEWED -> Sort.by(Sort.Direction.DESC, "viewCount").and(Sort.by(Sort.Direction.DESC, "createdAt"));
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "basePrice");
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "basePrice");
            case NEWEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            case FEATURED -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        // 3. Build Specification and Pageable
        Specification<Product> spec = ProductSpecification.buildSearchSpecification(request, resolvedCategoryIds);
        int pageNumber = request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int pageSize = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 20;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> productPage = productRepository.findAll(spec, pageRequest);
        List<UUID> productIds = productPage.getContent().stream().map(Product::getId).toList();

        // 4. Batch query isFavorited status to eliminate N+1
        Set<UUID> favoritedProductIds = Collections.emptySet();
        if (userEmail != null && !productIds.isEmpty()) {
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isPresent()) {
                favoritedProductIds = wishlistItemRepository.findFavoritedProductIdsByUserIdAndProductIdIn(userOpt.get().getId(), productIds);
            }
        }

        // 5. Batch query performance metrics & BuyBox prices
        Map<UUID, Long> unitsSoldMap = new HashMap<>();
        Map<UUID, BigDecimal> buyBoxMap = new HashMap<>();

        if (!productIds.isEmpty()) {
            List<Object[]> unitsSoldRows = orderItemRepository.countDeliveredUnitsSoldByProductIds(productIds);
            for (Object[] row : unitsSoldRows) {
                if (row[0] != null && row[1] != null) {
                    unitsSoldMap.put((UUID) row[0], ((Number) row[1]).longValue());
                }
            }

            List<Object[]> buyBoxRows = productListingRepository.findBuyBoxPricesByProductIds(productIds);
            for (Object[] row : buyBoxRows) {
                if (row[0] != null && row[1] != null) {
                    buyBoxMap.put((UUID) row[0], (BigDecimal) row[1]);
                }
            }
        }

        final Set<UUID> finalFavoritedIds = favoritedProductIds;
        List<ProductResponseDto> content = productPage.getContent().stream()
                .map(p -> {
                    boolean isFav = finalFavoritedIds.contains(p.getId());
                    long units = Math.max(p.getTotalUnitsSold(), unitsSoldMap.getOrDefault(p.getId(), 0L));
                    BigDecimal bbPrice = buyBoxMap.getOrDefault(p.getId(), p.getBasePrice());
                    return mapToResponseDto(p, isFav, units, bbPrice);
                })
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

    private Set<UUID> collectCategoryHierarchyIds(Category category) {
        if (category == null || !Boolean.TRUE.equals(category.getIsApproved())) {
            return Collections.emptySet();
        }
        Set<UUID> ids = new HashSet<>();
        ids.add(category.getId());
        collectChildrenRecursively(category.getId(), ids);
        return ids;
    }

    private void collectChildrenRecursively(UUID parentId, Set<UUID> ids) {
        List<Category> children = categoryRepository.findByParentIdAndIsApprovedTrue(parentId);
        for (Category child : children) {
            if (ids.add(child.getId())) {
                collectChildrenRecursively(child.getId(), ids);
            }
        }
    }

    private ProductResponseDto mapToResponseDto(
            Product product,
            boolean isFavorited,
            Long totalUnitsSold,
            BigDecimal buyBoxPrice) {

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
                .totalUnitsSold(totalUnitsSold != null ? totalUnitsSold : 0L)
                .viewCount(product.getViewCount() != null ? product.getViewCount() : 0L)
                .buyBoxPrice(buyBoxPrice != null ? buyBoxPrice : product.getBasePrice())
                .mainImageUrl(product.getMainImageUrl())
                .isFavorited(isFavorited)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
