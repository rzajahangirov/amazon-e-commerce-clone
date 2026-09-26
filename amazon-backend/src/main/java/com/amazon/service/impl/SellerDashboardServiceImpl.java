package com.amazon.service.impl;

import com.amazon.dtos.listing.request.UpdateListingStockRequestDto;
import com.amazon.dtos.listing.response.ProductListingResponseDto;
import com.amazon.dtos.seller.request.UpdateOrderItemStatusRequestDto;
import com.amazon.dtos.seller.request.UpdateSellerProfileRequestDto;
import com.amazon.dtos.seller.response.SellerAnalyticsResponseDto;
import com.amazon.dtos.seller.response.SellerOrderItemResponseDto;
import com.amazon.dtos.seller.response.SellerProfileResponseDto;
import com.amazon.entity.*;
import com.amazon.enums.ListingStatus;
import com.amazon.enums.OrderItemStatus;
import com.amazon.exception.BusinessRuleException;
import com.amazon.exception.DuplicateResourceException;
import com.amazon.exception.ResourceNotFoundException;
import com.amazon.payloads.ApiResponse;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;
import com.amazon.payloads.SellerError;
import com.amazon.repository.*;
import com.amazon.service.SellerDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of {@link SellerDashboardService} handling standalone 3P seller dashboard operations.
 * Enforces strict seller-scoped data isolation — a seller can ONLY access their own listings, orders, and analytics.
 * Adheres to Senior Backend Developer Guidelines Section 1, 5, 6, 8, 9, 10.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SellerDashboardServiceImpl implements SellerDashboardService {

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final ProductListingRepository productListingRepository;
    private final OrderItemRepository orderItemRepository;

    private static final Set<OrderItemStatus> VALID_SELLER_TRANSITIONS_FROM_PENDING =
            EnumSet.of(OrderItemStatus.SHIPPED, OrderItemStatus.CANCELLED);
    private static final Set<OrderItemStatus> VALID_SELLER_TRANSITIONS_FROM_SHIPPED =
            EnumSet.of(OrderItemStatus.DELIVERED);

    // ==========================================
    // Profile Management
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<SellerProfileResponseDto> getSellerProfile(String callerEmail) {
        SellerProfile profile = resolveSellerProfile(callerEmail);
        return ApiResponse.success(mapToProfileResponse(profile), "Seller profile retrieved successfully");
    }

    @Override
    @Transactional
    public ResponseDto<SellerProfileResponseDto> updateSellerProfile(String callerEmail, UpdateSellerProfileRequestDto request) {
        SellerProfile profile = resolveSellerProfile(callerEmail);

        if (request.getStoreName() != null && !request.getStoreName().isBlank()) {
            String newStoreName = request.getStoreName().trim();
            if (!newStoreName.equals(profile.getStoreName()) && sellerProfileRepository.existsByStoreName(newStoreName)) {
                throw new DuplicateResourceException(SellerError.STORE_NAME_ALREADY_EXISTS.getMessage());
            }
            profile.setStoreName(newStoreName);
        }
        if (request.getBusinessAddress() != null) {
            profile.setBusinessAddress(request.getBusinessAddress().trim());
        }
        if (request.getBankAccountDetails() != null) {
            profile.setBankAccountDetails(request.getBankAccountDetails().trim());
        }

        SellerProfile updated = sellerProfileRepository.save(profile);
        log.info("Seller profile updated: sellerId={}, email={}", updated.getId(), callerEmail);

        return ApiResponse.success(mapToProfileResponse(updated), "Seller profile updated successfully");
    }

    // ==========================================
    // Listing Management
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<PaginationPayload<ProductListingResponseDto>> getSellerListings(String callerEmail, int page, int size) {
        User seller = resolveUser(callerEmail);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ProductListing> listingsPage = productListingRepository.findBySellerId(seller.getId(), pageable);

        PaginationPayload<ProductListingResponseDto> payload = PaginationPayload.<ProductListingResponseDto>builder()
                .content(listingsPage.getContent().stream().map(this::mapToListingResponse).toList())
                .pageNumber(listingsPage.getNumber())
                .pageSize(listingsPage.getSize())
                .totalElements(listingsPage.getTotalElements())
                .totalPages(listingsPage.getTotalPages())
                .last(listingsPage.isLast())
                .build();

        return ApiResponse.success(payload, "Seller listings retrieved successfully");
    }

    @Override
    @Transactional
    public ResponseDto<ProductListingResponseDto> updateListingStock(
            String callerEmail, UUID listingId, UpdateListingStockRequestDto request) {
        User seller = resolveUser(callerEmail);

        ProductListing listing = productListingRepository.findWithDetailsById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found: " + listingId));

        enforceListingOwnership(listing, seller.getId());

        if (request.getStockQuantity() != null) {
            listing.setStockQuantity(request.getStockQuantity());
        }
        if (request.getPrice() != null) {
            listing.setPrice(request.getPrice());
        }

        ProductListing updated = productListingRepository.save(listing);
        log.info("Seller listing stock/price updated: listingId={}, sellerId={}", listingId, seller.getId());

        return ApiResponse.success(mapToListingResponse(updated), "Listing stock updated successfully");
    }

    // ==========================================
    // Order Fulfillment
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<List<SellerOrderItemResponseDto>> getSellerOrders(String callerEmail) {
        User seller = resolveUser(callerEmail);

        List<OrderItem> orderItems = orderItemRepository.findBySellerUserId(seller.getId());
        List<SellerOrderItemResponseDto> dtos = orderItems.stream()
                .map(this::mapToOrderItemResponse)
                .toList();

        return ApiResponse.success(dtos, "Seller order items retrieved successfully");
    }

    @Override
    @Transactional
    public ResponseDto<SellerOrderItemResponseDto> updateOrderItemStatus(
            String callerEmail, UUID orderItemId, UpdateOrderItemStatusRequestDto request) {
        User seller = resolveUser(callerEmail);

        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException(SellerError.ORDER_ITEM_NOT_FOUND.getMessage()));

        if (!orderItem.getSeller().getId().equals(seller.getId())) {
            throw new AccessDeniedException(SellerError.ORDER_ITEM_ACCESS_DENIED.getMessage());
        }

        validateStatusTransition(orderItem.getItemStatus(), request.getNewStatus());

        orderItem.setItemStatus(request.getNewStatus());
        OrderItem updated = orderItemRepository.save(orderItem);

        log.info("Order item status updated: orderItemId={}, newStatus={}, sellerId={}",
                orderItemId, request.getNewStatus(), seller.getId());

        return ApiResponse.success(mapToOrderItemResponse(updated), "Order item status updated successfully");
    }

    // ==========================================
    // Seller Analytics
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<SellerAnalyticsResponseDto> getSellerAnalytics(String callerEmail) {
        User seller = resolveUser(callerEmail);
        UUID sellerId = seller.getId();

        List<ProductListing> listings = productListingRepository.findBySellerId(sellerId);
        List<OrderItem> orderItems = orderItemRepository.findBySellerUserId(sellerId);

        long totalActiveListings = listings.stream()
                .filter(l -> l.getStatus() == ListingStatus.ACTIVE)
                .count();

        long totalProducts = listings.stream()
                .map(l -> l.getProductVariant().getProduct().getId())
                .distinct()
                .count();

        // Revenue & units from DELIVERED order items only
        List<OrderItem> deliveredItems = orderItems.stream()
                .filter(oi -> oi.getItemStatus() == OrderItemStatus.DELIVERED)
                .toList();

        BigDecimal totalRevenue = deliveredItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalUnitsSold = deliveredItems.stream()
                .mapToLong(OrderItem::getQuantity)
                .sum();

        long totalPendingOrders = orderItems.stream()
                .filter(oi -> oi.getItemStatus() == OrderItemStatus.PENDING)
                .count();
        long totalShippedOrders = orderItems.stream()
                .filter(oi -> oi.getItemStatus() == OrderItemStatus.SHIPPED)
                .count();
        long totalDeliveredOrders = deliveredItems.size();

        // Top selling listings (by units sold)
        Map<String, List<OrderItem>> groupedBySku = deliveredItems.stream()
                .filter(oi -> oi.getListing() != null)
                .collect(Collectors.groupingBy(oi -> oi.getListing().getSellerSku()));

        List<SellerAnalyticsResponseDto.TopSellingListingDto> topSellingListings = groupedBySku.entrySet().stream()
                .map(entry -> {
                    List<OrderItem> items = entry.getValue();
                    long units = items.stream().mapToLong(OrderItem::getQuantity).sum();
                    BigDecimal revenue = items.stream()
                            .map(OrderItem::getSubtotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    String productTitle = items.get(0).getProductVariant().getProduct().getTitle();
                    return SellerAnalyticsResponseDto.TopSellingListingDto.builder()
                            .productTitle(productTitle)
                            .sellerSku(entry.getKey())
                            .totalUnitsSold(units)
                            .totalRevenue(revenue)
                            .build();
                })
                .sorted(Comparator.comparing(SellerAnalyticsResponseDto.TopSellingListingDto::getTotalUnitsSold).reversed())
                .limit(10)
                .toList();

        SellerAnalyticsResponseDto analytics = SellerAnalyticsResponseDto.builder()
                .totalActiveListings(totalActiveListings)
                .totalProducts(totalProducts)
                .totalRevenue(totalRevenue)
                .totalUnitsSold(totalUnitsSold)
                .totalPendingOrders(totalPendingOrders)
                .totalShippedOrders(totalShippedOrders)
                .totalDeliveredOrders(totalDeliveredOrders)
                .topSellingListings(topSellingListings)
                .build();

        return ApiResponse.success(analytics, "Seller analytics retrieved successfully");
    }

    // ==========================================
    // Security & Isolation Helpers
    // ==========================================

    private User resolveUser(String callerEmail) {
        return userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + callerEmail));
    }

    private SellerProfile resolveSellerProfile(String callerEmail) {
        SellerProfile profile = sellerProfileRepository.findByUserEmail(callerEmail)
                .orElseThrow(() -> new ResourceNotFoundException(SellerError.SELLER_NOT_FOUND.getMessage()));
        return profile;
    }

    private void enforceListingOwnership(ProductListing listing, UUID sellerId) {
        if (!listing.getSeller().getId().equals(sellerId)) {
            throw new AccessDeniedException(SellerError.LISTING_ACCESS_DENIED.getMessage());
        }
    }

    private void validateStatusTransition(OrderItemStatus current, OrderItemStatus target) {
        boolean valid = switch (current) {
            case PENDING -> VALID_SELLER_TRANSITIONS_FROM_PENDING.contains(target);
            case SHIPPED -> VALID_SELLER_TRANSITIONS_FROM_SHIPPED.contains(target);
            default -> false;
        };
        if (!valid) {
            throw new BusinessRuleException(
                    SellerError.INVALID_STATUS_TRANSITION.getMessage() + ": " + current + " -> " + target);
        }
    }

    // ==========================================
    // Mappers
    // ==========================================

    private SellerProfileResponseDto mapToProfileResponse(SellerProfile profile) {
        return SellerProfileResponseDto.builder()
                .id(profile.getId())
                .userId(profile.getUser().getId())
                .fullName(profile.getUser().getFullName())
                .email(profile.getUser().getEmail())
                .storeName(profile.getStoreName())
                .taxNumber(profile.getTaxNumber())
                .businessAddress(profile.getBusinessAddress())
                .bankAccountDetails(profile.getBankAccountDetails())
                .isVerified(profile.getIsVerified())
                .brandId(profile.getBrand() != null ? profile.getBrand().getId() : null)
                .brandName(profile.getBrand() != null ? profile.getBrand().getName() : null)
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    private ProductListingResponseDto mapToListingResponse(ProductListing listing) {
        return ProductListingResponseDto.builder()
                .id(listing.getId())
                .productVariantId(listing.getProductVariant().getId())
                .variantAsin(listing.getProductVariant().getAsin())
                .variantName(listing.getProductVariant().getVariantName())
                .sellerId(listing.getSeller().getId())
                .sellerName(listing.getSeller().getFullName())
                .sellerSku(listing.getSellerSku())
                .price(listing.getPrice())
                .stockQuantity(listing.getStockQuantity())
                .fulfillmentType(listing.getFulfillmentType())
                .isBuyboxWinner(listing.getIsBuyboxWinner())
                .status(listing.getStatus())
                .createdAt(listing.getCreatedAt())
                .build();
    }

    private SellerOrderItemResponseDto mapToOrderItemResponse(OrderItem orderItem) {
        return SellerOrderItemResponseDto.builder()
                .orderItemId(orderItem.getId())
                .orderId(orderItem.getOrder().getId())
                .productTitle(orderItem.getProductVariant().getProduct().getTitle())
                .variantName(orderItem.getProductVariant().getVariantName())
                .asin(orderItem.getProductVariant().getAsin())
                .sellerSku(orderItem.getListing() != null ? orderItem.getListing().getSellerSku() : null)
                .unitPrice(orderItem.getUnitPrice())
                .quantity(orderItem.getQuantity())
                .subtotal(orderItem.getSubtotal())
                .itemStatus(orderItem.getItemStatus())
                .buyerName(orderItem.getOrder().getUser().getFullName())
                .orderDate(orderItem.getOrder().getCreatedAt())
                .build();
    }
}
