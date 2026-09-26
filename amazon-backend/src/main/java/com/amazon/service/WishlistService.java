package com.amazon.service;

import com.amazon.dtos.wishlist.response.WishlistItemResponseDto;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * Service interface for Wishlist (Favorites) operations.
 */
public interface WishlistService {

    ResponseDto<WishlistItemResponseDto> addToWishlist(String email, UUID productId);

    ResponseDto<Void> removeFromWishlist(String email, UUID productId);

    ResponseDto<PaginationPayload<WishlistItemResponseDto>> getWishlist(String email, int page, int size);

    ResponseDto<Void> clearWishlist(String email);

    /**
     * Batch-check which product IDs are favorited by a given user.
     * Used by ProductService to populate isFavorited flag without N+1.
     *
     * @param userId     the user's UUID
     * @param productIds collection of product UUIDs to check
     * @return set of product UUIDs that are in the user's wishlist
     */
    Set<UUID> getFavoritedProductIds(UUID userId, Collection<UUID> productIds);
}
