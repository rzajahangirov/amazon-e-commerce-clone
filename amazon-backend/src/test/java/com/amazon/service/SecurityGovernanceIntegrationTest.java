package com.amazon.service;

import com.amazon.controller.CartController;
import com.amazon.controller.OrderController;
import com.amazon.controller.ReviewController;
import com.amazon.controller.SellerDashboardController;
import com.amazon.dtos.order.request.UpdateOrderStatusRequestDto;
import com.amazon.dtos.review.request.CreateReviewRequestDto;
import com.amazon.enums.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SecurityGovernanceIntegrationTest {

    @Autowired
    private OrderController orderController;

    @Autowired
    private SellerDashboardController sellerDashboardController;

    @Autowired
    private CartController cartController;

    @Autowired
    private ReviewController reviewController;

    // =========================================================================
    // 1. STRICT ADMIN-ONLY ORDER OVERRIDE (PUT /v1/api/orders/{id}/status)
    // =========================================================================

    @Test
    @DisplayName("Order Status Override: Reject seller from updating root order status (strictly ADMIN only)")
    @WithMockUser(username = "seller@store.com", roles = {"SELLER"})
    void testOrderStatusUpdateDeniedForSeller() {
        UpdateOrderStatusRequestDto request = new UpdateOrderStatusRequestDto(OrderStatus.SHIPPED);
        UUID dummyOrderId = UUID.randomUUID();

        assertThrows(AccessDeniedException.class, () ->
                orderController.updateOrderStatus(dummyOrderId, request));
    }

    @Test
    @DisplayName("Order Status Override: Reject customer from updating root order status")
    @WithMockUser(username = "customer@buyer.com", roles = {"CUSTOMER"})
    void testOrderStatusUpdateDeniedForCustomer() {
        UpdateOrderStatusRequestDto request = new UpdateOrderStatusRequestDto(OrderStatus.CANCELLED);
        UUID dummyOrderId = UUID.randomUUID();

        assertThrows(AccessDeniedException.class, () ->
                orderController.updateOrderStatus(dummyOrderId, request));
    }

    @Test
    @DisplayName("Order Status Override: Reject unauthenticated user from updating root order status")
    void testOrderStatusUpdateDeniedForUnauthenticated() {
        UpdateOrderStatusRequestDto request = new UpdateOrderStatusRequestDto(OrderStatus.DELIVERED);
        UUID dummyOrderId = UUID.randomUUID();

        assertThrows(AuthenticationCredentialsNotFoundException.class, () ->
                orderController.updateOrderStatus(dummyOrderId, request));
    }

    // =========================================================================
    // 2. STRICT SELLER-ONLY DASHBOARD ACCESSIBILITY (/v1/api/seller-dashboard/**)
    // =========================================================================

    @Test
    @DisplayName("Seller Dashboard: Pure ADMIN without ROLE_SELLER is rejected with 403 AccessDenied")
    @WithMockUser(username = "admin@platform.com", roles = {"ADMIN"})
    void testSellerDashboardDeniedForPureAdmin() {
        Principal principal = () -> "admin@platform.com";

        assertThrows(AccessDeniedException.class, () ->
                sellerDashboardController.getSellerProfile(principal));
    }

    @Test
    @DisplayName("Seller Dashboard: Customer without ROLE_SELLER is rejected with 403 AccessDenied")
    @WithMockUser(username = "customer@buyer.com", roles = {"CUSTOMER"})
    void testSellerDashboardDeniedForCustomer() {
        Principal principal = () -> "customer@buyer.com";

        assertThrows(AccessDeniedException.class, () ->
                sellerDashboardController.getSellerProfile(principal));
    }

    @Test
    @DisplayName("Seller Dashboard: Unauthenticated user is rejected")
    void testSellerDashboardDeniedForUnauthenticated() {
        Principal principal = () -> "guest@platform.com";

        assertThrows(AuthenticationCredentialsNotFoundException.class, () ->
                sellerDashboardController.getSellerProfile(principal));
    }

    // =========================================================================
    // 3. CART SECURITY (CartController requires isAuthenticated)
    // =========================================================================

    @Test
    @DisplayName("Cart Controller: Reject unauthenticated requests")
    void testCartDeniedForUnauthenticated() {
        Principal principal = () -> "unauthenticated@buyer.com";

        assertThrows(AuthenticationCredentialsNotFoundException.class, () ->
                cartController.getCart(principal));
    }

    // =========================================================================
    // 4. REVIEW ENDPOINTS SECURITY
    // =========================================================================

    @Test
    @DisplayName("Review Controller: Reject review creation for unauthenticated user")
    void testReviewCreateDeniedForUnauthenticated() {
        CreateReviewRequestDto request = CreateReviewRequestDto.builder().rating(5).build();
        UUID productId = UUID.randomUUID();
        Principal principal = () -> "guest@buyer.com";

        assertThrows(AuthenticationCredentialsNotFoundException.class, () ->
                reviewController.createReview(productId, request, principal));
    }

    @Test
    @DisplayName("Review Controller: Public access permitted for reading reviews")
    void testReviewListPubliclyPermitted() {
        UUID productId = UUID.randomUUID();

        // Reading reviews is permitAll and should not throw security authentication/authorization exceptions
        // (It may throw ResourceNotFoundException if dummy product doesn't exist in DB, but NOT security exception)
        try {
            reviewController.getProductReviews(productId, 0, 10);
        } catch (Exception ex) {
            assertFalse(ex instanceof AccessDeniedException);
            assertFalse(ex instanceof AuthenticationCredentialsNotFoundException);
        }
    }
}
