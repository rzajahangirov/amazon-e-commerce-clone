package com.amazon.service;

import com.amazon.dtos.cart.request.AddToCartRequestDto;
import com.amazon.dtos.cart.request.UpdateCartItemRequestDto;
import com.amazon.dtos.cart.response.CartResponseDto;
import com.amazon.payloads.ResponseDto;

import java.util.UUID;

/**
 * Service interface for Cart operations.
 */
public interface CartService {

    ResponseDto<CartResponseDto> getCartByUserEmail(String userEmail);

    ResponseDto<CartResponseDto> addToCart(String userEmail, AddToCartRequestDto request);

    ResponseDto<CartResponseDto> updateCartItem(String userEmail, UUID itemId, UpdateCartItemRequestDto request);

    ResponseDto<CartResponseDto> removeCartItem(String userEmail, UUID itemId);

    ResponseDto<CartResponseDto> toggleSaveForLater(String userEmail, UUID itemId);

    ResponseDto<Void> clearCart(String userEmail);
}
