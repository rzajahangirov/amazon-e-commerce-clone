package com.amazon.service.impl;

import com.amazon.dtos.cart.request.AddToCartRequestDto;
import com.amazon.dtos.cart.request.UpdateCartItemRequestDto;
import com.amazon.dtos.cart.response.CartItemResponseDto;
import com.amazon.dtos.cart.response.CartResponseDto;
import com.amazon.entity.Cart;
import com.amazon.entity.CartItem;
import com.amazon.entity.ProductListing;
import com.amazon.entity.User;
import com.amazon.exception.BusinessRuleException;
import com.amazon.exception.ResourceNotFoundException;
import com.amazon.payloads.ApiResponse;
import com.amazon.payloads.AuthError;
import com.amazon.payloads.CartError;
import com.amazon.payloads.CatalogError;
import com.amazon.payloads.ResponseDto;
import com.amazon.repository.CartItemRepository;
import com.amazon.repository.CartRepository;
import com.amazon.repository.ProductListingRepository;
import com.amazon.repository.UserRepository;
import com.amazon.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service implementation for managing customer shopping carts.
 * Strictly adheres to Senior Backend Developer Guidelines.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductListingRepository productListingRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ResponseDto<CartResponseDto> getCartByUserEmail(String userEmail) {
        Cart cart = getOrCreateCart(userEmail);
        return ApiResponse.success(mapToCartResponseDto(cart), "Cart retrieved successfully");
    }

    @Override
    @Transactional
    public ResponseDto<CartResponseDto> addToCart(String userEmail, AddToCartRequestDto request) {
        if (request.getQuantity() == null || request.getQuantity() < 1) {
            throw new BusinessRuleException(CartError.INVALID_QUANTITY.getMessage());
        }

        Cart cart = getOrCreateCart(userEmail);

        UUID targetId = request.getListingId();
        ProductListing listing = productListingRepository.findWithDetailsById(targetId)
                .or(() -> productListingRepository.findActiveListingsByVariantIdWithDetails(targetId).stream().findFirst())
                .or(() -> productListingRepository.findActiveListingsByProductIdWithDetails(targetId).stream().findFirst())
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.LISTING_NOT_FOUND.getMessage()));

        if (!listing.isAvailableForPurchase(request.getQuantity())) {
            throw new BusinessRuleException(CatalogError.INSUFFICIENT_STOCK.getMessage());
        }

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getListing().getId().equals(listing.getId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (newQuantity > listing.getStockQuantity()) {
                throw new BusinessRuleException(CartError.EXCEEDS_AVAILABLE_STOCK.getMessage());
            }
            existingItem.setQuantity(newQuantity);
            existingItem.setIsSavedForLater(false);
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .listing(listing)
                    .quantity(request.getQuantity())
                    .isSavedForLater(false)
                    .build();
            cart.addItem(newItem);
            cartItemRepository.save(newItem);
        }

        log.info("Item added to cart for user: {}, listingId: {}, quantity: {}",
                userEmail, request.getListingId(), request.getQuantity());

        return ApiResponse.success(mapToCartResponseDto(cart), "Item added to cart successfully");
    }

    @Override
    @Transactional
    public ResponseDto<CartResponseDto> updateCartItem(
            String userEmail, UUID itemId, UpdateCartItemRequestDto request) {
        if (request.getQuantity() == null || request.getQuantity() < 1) {
            throw new BusinessRuleException(CartError.INVALID_QUANTITY.getMessage());
        }

        Cart cart = getOrCreateCart(userEmail);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(CartError.CART_ITEM_NOT_FOUND.getMessage()));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BusinessRuleException(CartError.ITEM_NOT_IN_USER_CART.getMessage());
        }

        if (request.getQuantity() > item.getListing().getStockQuantity()) {
            throw new BusinessRuleException(CartError.EXCEEDS_AVAILABLE_STOCK.getMessage());
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        log.info("Cart item: {} quantity updated to: {} for user: {}", itemId, request.getQuantity(), userEmail);

        return ApiResponse.success(mapToCartResponseDto(cart), "Cart item updated successfully");
    }

    @Override
    @Transactional
    public ResponseDto<CartResponseDto> removeCartItem(String userEmail, UUID itemId) {
        Cart cart = getOrCreateCart(userEmail);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(CartError.CART_ITEM_NOT_FOUND.getMessage()));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BusinessRuleException(CartError.ITEM_NOT_IN_USER_CART.getMessage());
        }

        cart.removeItem(item);
        cartItemRepository.delete(item);
        cartRepository.save(cart);

        log.info("Cart item: {} removed from cart for user: {}", itemId, userEmail);

        return ApiResponse.success(mapToCartResponseDto(cart), "Cart item removed successfully");
    }

    @Override
    @Transactional
    public ResponseDto<CartResponseDto> toggleSaveForLater(String userEmail, UUID itemId) {
        Cart cart = getOrCreateCart(userEmail);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(CartError.CART_ITEM_NOT_FOUND.getMessage()));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BusinessRuleException(CartError.ITEM_NOT_IN_USER_CART.getMessage());
        }

        item.setIsSavedForLater(!Boolean.TRUE.equals(item.getIsSavedForLater()));
        cartItemRepository.save(item);

        log.info("Cart item: {} save-for-later set to: {} for user: {}",
                itemId, item.getIsSavedForLater(), userEmail);

        return ApiResponse.success(mapToCartResponseDto(cart), "Cart item status toggled successfully");
    }

    @Override
    @Transactional
    public ResponseDto<Void> clearCart(String userEmail) {
        Cart cart = getOrCreateCart(userEmail);
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);

        log.info("Cart cleared successfully for user: {}", userEmail);

        return ApiResponse.success(null, "Cart cleared successfully");
    }

    private Cart getOrCreateCart(String userEmail) {
        return cartRepository.findWithItemsByUserEmail(userEmail)
                .orElseGet(() -> {
                    User user = userRepository.findByEmail(userEmail)
                            .orElseThrow(() -> new ResourceNotFoundException(AuthError.USER_NOT_FOUND.getMessage()));
                    Cart newCart = Cart.builder()
                            .user(user)
                            .items(new ArrayList<>())
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    private CartResponseDto mapToCartResponseDto(Cart cart) {
        List<CartItemResponseDto> itemDtos = new ArrayList<>();
        BigDecimal activeSubtotal = BigDecimal.ZERO;
        int activeCount = 0;
        int savedCount = 0;

        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                ProductListing listing = item.getListing();
                BigDecimal price = listing != null ? listing.getPrice() : BigDecimal.ZERO;
                BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));

                if (Boolean.TRUE.equals(item.getIsSavedForLater())) {
                    savedCount += item.getQuantity();
                } else {
                    activeCount += item.getQuantity();
                    activeSubtotal = activeSubtotal.add(subtotal);
                }

                itemDtos.add(CartItemResponseDto.builder()
                        .id(item.getId())
                        .listingId(listing != null ? listing.getId() : null)
                        .productVariantId(listing != null && listing.getProductVariant() != null ? listing.getProductVariant().getId() : null)
                        .productTitle(listing != null && listing.getProductVariant() != null && listing.getProductVariant().getProduct() != null ? listing.getProductVariant().getProduct().getTitle() : null)
                        .productMainImageUrl(listing != null && listing.getProductVariant() != null && listing.getProductVariant().getProduct() != null ? listing.getProductVariant().getProduct().getMainImageUrl() : null)
                        .variantName(listing != null && listing.getProductVariant() != null ? listing.getProductVariant().getVariantName() : null)
                        .asin(listing != null && listing.getProductVariant() != null ? listing.getProductVariant().getAsin() : null)
                        .unitPrice(price)
                        .quantity(item.getQuantity())
                        .subtotal(subtotal)
                        .isSavedForLater(item.getIsSavedForLater())
                        .addedAt(item.getAddedAt())
                        // Enterprise Storefront Fields
                        .sellerSku(listing != null ? listing.getSellerSku() : null)
                        .stockWarning(listing != null && listing.getStockQuantity() != null && listing.getStockQuantity() < 10
                                ? "Only " + listing.getStockQuantity() + " units left in stock"
                                : null)
                        .badgeTag(listing != null && listing.getProductVariant() != null && listing.getProductVariant().getProduct() != null
                                ? listing.getProductVariant().getProduct().getBadgeTag()
                                : null)
                        .build());
            }
        }

        return CartResponseDto.builder()
                .id(cart.getId())
                .userId(cart.getUser() != null ? cart.getUser().getId() : null)
                .items(itemDtos)
                .totalActiveItems(activeCount)
                .totalSavedForLaterItems(savedCount)
                .activeSubtotal(activeSubtotal)
                .build();
    }
}
