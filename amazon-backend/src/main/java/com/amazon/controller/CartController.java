package com.amazon.controller;

import com.amazon.dtos.cart.request.AddToCartRequestDto;
import com.amazon.dtos.cart.request.UpdateCartItemRequestDto;
import com.amazon.dtos.cart.response.CartResponseDto;
import com.amazon.payloads.ResponseDto;
import com.amazon.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

/**
 * REST controller for authenticated Shopping Cart operations.
 * Strictly adheres to Senior Backend Developer Guidelines (Principal pattern, zero entity leakage).
 */
@RestController
@RequestMapping("v1/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart management endpoints")
@Slf4j
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get user cart", description = "Retrieves the current authenticated user's shopping cart")
    public ResponseEntity<ResponseDto<CartResponseDto>> getCart(Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(cartService.getCartByUserEmail(email));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Adds a product listing offer with requested quantity to the cart")
    public ResponseEntity<ResponseDto<CartResponseDto>> addToCart(
            @Valid @RequestBody AddToCartRequestDto request,
            Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(cartService.addToCart(email, request));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item quantity", description = "Updates the quantity of an item in the user's cart")
    public ResponseEntity<ResponseDto<CartResponseDto>> updateCartItem(
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequestDto request,
            Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(cartService.updateCartItem(email, itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart", description = "Removes an item from the user's cart")
    public ResponseEntity<ResponseDto<CartResponseDto>> removeCartItem(
            @PathVariable UUID itemId,
            Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(cartService.removeCartItem(email, itemId));
    }

    @PostMapping("/items/{itemId}/save-for-later")
    @Operation(summary = "Toggle save for later", description = "Moves item between active cart and saved for later list")
    public ResponseEntity<ResponseDto<CartResponseDto>> toggleSaveForLater(
            @PathVariable UUID itemId,
            Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(cartService.toggleSaveForLater(email, itemId));
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Empties all items from the current user's cart")
    public ResponseEntity<ResponseDto<Void>> clearCart(Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(cartService.clearCart(email));
    }
}
