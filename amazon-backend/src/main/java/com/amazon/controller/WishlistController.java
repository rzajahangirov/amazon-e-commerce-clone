package com.amazon.controller;

import com.amazon.dtos.wishlist.response.WishlistItemResponseDto;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;
import com.amazon.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

/**
 * REST controller for authenticated User Wishlist (Favorites) operations.
 * Strictly adheres to Senior Backend Developer Guidelines (Principal pattern, zero entity leakage).
 */
@RestController
@RequestMapping("/v1/api/wishlist")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Customer wishlist / favorites management endpoints")
@Slf4j
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/{productId}")
    @Operation(summary = "Add product to wishlist", description = "Adds a product to the authenticated user's favorites list (idempotent)")
    public ResponseEntity<ResponseDto<WishlistItemResponseDto>> addToWishlist(
            @PathVariable UUID productId,
            Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(wishlistService.addToWishlist(email, productId));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear wishlist", description = "Removes all items from the authenticated user's wishlist")
    public ResponseEntity<ResponseDto<Void>> clearWishlist(Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(wishlistService.clearWishlist(email));
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Remove product from wishlist", description = "Removes a specific product from the user's wishlist")
    public ResponseEntity<ResponseDto<Void>> removeFromWishlist(
            @PathVariable UUID productId,
            Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(wishlistService.removeFromWishlist(email, productId));
    }

    @GetMapping
    @Operation(summary = "Get user wishlist", description = "Retrieves a paginated list of products saved in the user's wishlist")
    public ResponseEntity<ResponseDto<PaginationPayload<WishlistItemResponseDto>>> getWishlist(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(wishlistService.getWishlist(email, page, size));
    }
}
