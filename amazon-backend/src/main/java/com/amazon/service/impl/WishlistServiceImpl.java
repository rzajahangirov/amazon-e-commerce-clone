package com.amazon.service.impl;

import com.amazon.dtos.wishlist.response.WishlistItemResponseDto;
import com.amazon.entity.Product;
import com.amazon.entity.User;
import com.amazon.entity.WishlistItem;
import com.amazon.exception.ResourceNotFoundException;
import com.amazon.payloads.ApiResponse;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;
import com.amazon.payloads.WishlistError;
import com.amazon.repository.ProductRepository;
import com.amazon.repository.UserRepository;
import com.amazon.repository.WishlistItemRepository;
import com.amazon.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service implementation for Wishlist (Favorites) management.
 * Strictly adheres to Senior Backend Developer Guidelines (zero entity leakage, transactional boundaries).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistServiceImpl implements WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ResponseDto<WishlistItemResponseDto> addToWishlist(String email, UUID productId) {
        User user = findUserByEmail(email);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(WishlistError.PRODUCT_NOT_FOUND.getMessage()));

        // Idempotency check: if already in wishlist, return existing item gracefully
        Optional<WishlistItem> existingItem = wishlistItemRepository.findByUserIdAndProductId(user.getId(), productId);
        if (existingItem.isPresent()) {
            log.info("Product {} is already in wishlist for user {}", productId, user.getId());
            return ApiResponse.success(mapToResponseDto(existingItem.get()), "Product is already in your wishlist");
        }

        WishlistItem newItem = WishlistItem.builder()
                .user(user)
                .product(product)
                .build();

        WishlistItem savedItem = wishlistItemRepository.save(newItem);
        log.info("Product {} added to wishlist for user {}", productId, user.getId());

        return ApiResponse.success(mapToResponseDto(savedItem), "Product added to wishlist successfully");
    }

    @Override
    @Transactional
    public ResponseDto<Void> removeFromWishlist(String email, UUID productId) {
        User user = findUserByEmail(email);

        WishlistItem item = wishlistItemRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException(WishlistError.ITEM_NOT_IN_WISHLIST.getMessage()));

        wishlistItemRepository.delete(item);
        log.info("Product {} removed from wishlist for user {}", productId, user.getId());

        return ApiResponse.success(null, "Product removed from wishlist successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<PaginationPayload<WishlistItemResponseDto>> getWishlist(String email, int page, int size) {
        User user = findUserByEmail(email);

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<WishlistItem> itemPage = wishlistItemRepository.findByUserId(user.getId(), pageRequest);

        List<WishlistItemResponseDto> content = itemPage.getContent().stream()
                .map(this::mapToResponseDto)
                .toList();

        PaginationPayload<WishlistItemResponseDto> paginationPayload = PaginationPayload.<WishlistItemResponseDto>builder()
                .content(content)
                .pageNumber(itemPage.getNumber())
                .pageSize(itemPage.getSize())
                .totalElements(itemPage.getTotalElements())
                .totalPages(itemPage.getTotalPages())
                .last(itemPage.isLast())
                .build();

        return ApiResponse.success(paginationPayload, "Wishlist retrieved successfully");
    }

    @Override
    @Transactional
    public ResponseDto<Void> clearWishlist(String email) {
        User user = findUserByEmail(email);

        wishlistItemRepository.deleteAllByUserId(user.getId());
        log.info("Wishlist cleared for user {}", user.getId());

        return ApiResponse.success(null, "Wishlist cleared successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public Set<UUID> getFavoritedProductIds(UUID userId, Collection<UUID> productIds) {
        if (userId == null || productIds == null || productIds.isEmpty()) {
            return Collections.emptySet();
        }
        return wishlistItemRepository.findFavoritedProductIdsByUserIdAndProductIdIn(userId, productIds);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private WishlistItemResponseDto mapToResponseDto(WishlistItem item) {
        Product product = item.getProduct();
        return WishlistItemResponseDto.builder()
                .wishlistItemId(item.getId())
                .productId(product.getId())
                .productTitle(product.getTitle())
                .productDescription(product.getDescription())
                .basePrice(product.getBasePrice())
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .averageRating(product.getAverageRating() != null ? product.getAverageRating() : 0.0)
                .totalReviews(product.getTotalReviews() != null ? product.getTotalReviews() : 0)
                .addedAt(item.getCreatedAt())
                .build();
    }
}
