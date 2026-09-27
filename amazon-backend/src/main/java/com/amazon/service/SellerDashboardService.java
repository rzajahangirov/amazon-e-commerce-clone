package com.amazon.service;

import com.amazon.dtos.listing.request.UpdateListingStockRequestDto;
import com.amazon.dtos.listing.response.ProductListingResponseDto;
import com.amazon.dtos.seller.request.UpdateOrderItemStatusRequestDto;
import com.amazon.dtos.seller.request.UpdateSellerProfileRequestDto;
import com.amazon.dtos.seller.response.SellerAnalyticsResponseDto;
import com.amazon.dtos.seller.response.SellerOrderItemResponseDto;
import com.amazon.dtos.seller.response.SellerProfileResponseDto;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Seller Dashboard operations, encompassing profile management,
 * listing/inventory management, order fulfillment, and seller analytics.
 * All operations enforce strict seller-scoped data isolation.
 */
public interface SellerDashboardService {

    // Profile Management
    ResponseDto<SellerProfileResponseDto> getSellerProfile(String callerEmail);

    ResponseDto<SellerProfileResponseDto> updateSellerProfile(String callerEmail, UpdateSellerProfileRequestDto request);

    // Listing Management
    ResponseDto<PaginationPayload<ProductListingResponseDto>> getSellerListings(String callerEmail, int page, int size);

    ResponseDto<ProductListingResponseDto> updateListingStock(String callerEmail, UUID listingId, UpdateListingStockRequestDto request);

    // Order Fulfillment
    ResponseDto<List<SellerOrderItemResponseDto>> getSellerOrders(
            String callerEmail,
            com.amazon.enums.OrderItemStatus status,
            String searchKey,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate);

    default ResponseDto<List<SellerOrderItemResponseDto>> getSellerOrders(String callerEmail) {
        return getSellerOrders(callerEmail, null, null, null, null);
    }

    ResponseDto<SellerOrderItemResponseDto> updateOrderItemStatus(String callerEmail, UUID orderItemId, UpdateOrderItemStatusRequestDto request);

    // Seller Analytics
    ResponseDto<SellerAnalyticsResponseDto> getSellerAnalytics(String callerEmail);
}
