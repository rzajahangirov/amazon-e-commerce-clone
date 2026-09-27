package com.amazon.controller;

import com.amazon.dtos.listing.request.UpdateListingStockRequestDto;
import com.amazon.dtos.listing.response.ProductListingResponseDto;
import com.amazon.dtos.seller.request.UpdateOrderItemStatusRequestDto;
import com.amazon.dtos.seller.request.UpdateSellerProfileRequestDto;
import com.amazon.dtos.seller.response.SellerAnalyticsResponseDto;
import com.amazon.dtos.seller.response.SellerOrderItemResponseDto;
import com.amazon.dtos.seller.response.SellerProfileResponseDto;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;
import com.amazon.service.SellerDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.amazon.enums.OrderItemStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for Standalone Seller Dashboard.
 * Injects Principal and passes ONLY the email string to the service layer.
 * All operations enforce strict seller-scoped data isolation.
 * Strictly restricted to users holding the ROLE_SELLER authority.
 * Pure administrators (ROLE_ADMIN) without an active, verified seller profile/role are forbidden
 * from accessing the seller dashboard to enforce strict tenant isolation.
 * Adheres to Senior Backend Developer Guidelines Section 1, 3, 4, 10.2.
 */
@RestController
@RequestMapping("/v1/api/seller-dashboard")
@PreAuthorize("hasRole('SELLER')")
@RequiredArgsConstructor
@Tag(name = "Seller Dashboard", description = "Seller listing management, order fulfillment, profile, and analytics")
public class SellerDashboardController {

    private final SellerDashboardService sellerDashboardService;

    // ==========================================
    // Profile Management
    // ==========================================

    @GetMapping("/profile")
    @Operation(summary = "Get seller profile", description = "Returns the authenticated seller's profile information")
    public ResponseEntity<ResponseDto<SellerProfileResponseDto>> getSellerProfile(Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(sellerDashboardService.getSellerProfile(email));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update seller profile", description = "Update store name, business address, or bank details")
    public ResponseEntity<ResponseDto<SellerProfileResponseDto>> updateSellerProfile(
            Principal principal,
            @Valid @RequestBody UpdateSellerProfileRequestDto request) {
        String email = principal.getName();
        return ResponseEntity.ok(sellerDashboardService.updateSellerProfile(email, request));
    }

    // ==========================================
    // Listing Management
    // ==========================================

    @GetMapping("/listings")
    @Operation(summary = "Get seller listings", description = "Paginated list of seller's own product listings")
    public ResponseEntity<ResponseDto<PaginationPayload<ProductListingResponseDto>>> getSellerListings(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String email = principal.getName();
        return ResponseEntity.ok(sellerDashboardService.getSellerListings(email, page, size));
    }

    @PutMapping("/listings/{listingId}/stock")
    @Operation(summary = "Update listing stock/price", description = "Update stock quantity or price for an owned listing")
    public ResponseEntity<ResponseDto<ProductListingResponseDto>> updateListingStock(
            Principal principal,
            @PathVariable UUID listingId,
            @Valid @RequestBody UpdateListingStockRequestDto request) {
        String email = principal.getName();
        return ResponseEntity.ok(sellerDashboardService.updateListingStock(email, listingId, request));
    }

    // ==========================================
    // Order Fulfillment
    // ==========================================

    @GetMapping("/orders")
    @Operation(summary = "Get seller orders", description = "List of order items assigned to this seller with optional status, search, and date filters")
    public ResponseEntity<ResponseDto<List<SellerOrderItemResponseDto>>> getSellerOrders(
            Principal principal,
            @RequestParam(required = false) OrderItemStatus status,
            @RequestParam(required = false) String searchKey,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String email = principal.getName();
        return ResponseEntity.ok(sellerDashboardService.getSellerOrders(email, status, searchKey, startDate, endDate));
    }

    @PutMapping("/orders/{orderItemId}/status")
    @Operation(summary = "Update order item status", description = "Transition fulfillment status (PENDING->SHIPPED->DELIVERED or PENDING->CANCELLED)")
    public ResponseEntity<ResponseDto<SellerOrderItemResponseDto>> updateOrderItemStatus(
            Principal principal,
            @PathVariable UUID orderItemId,
            @Valid @RequestBody UpdateOrderItemStatusRequestDto request) {
        String email = principal.getName();
        return ResponseEntity.ok(sellerDashboardService.updateOrderItemStatus(email, orderItemId, request));
    }

    // ==========================================
    // Analytics
    // ==========================================

    @GetMapping("/analytics")
    @Operation(summary = "Get seller analytics", description = "Revenue, units sold, order breakdowns, and top-selling listings")
    public ResponseEntity<ResponseDto<SellerAnalyticsResponseDto>> getSellerAnalytics(Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(sellerDashboardService.getSellerAnalytics(email));
    }
}
