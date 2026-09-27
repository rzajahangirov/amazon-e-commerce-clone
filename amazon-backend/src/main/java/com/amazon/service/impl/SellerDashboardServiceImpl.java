package com.amazon.service.impl;

import com.amazon.dtos.listing.request.UpdateListingStockRequestDto;
import com.amazon.dtos.listing.response.ProductListingResponseDto;
import com.amazon.dtos.seller.request.UpdateOrderItemStatusRequestDto;
import com.amazon.dtos.seller.request.UpdateSellerProfileRequestDto;
import com.amazon.dtos.seller.response.HourlyOrderInfluxDto;
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
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        if (request.getSupportEmail() != null) {
            profile.setSupportEmail(request.getSupportEmail().trim());
        }
        if (request.getMerchantPhone() != null) {
            profile.setMerchantPhone(request.getMerchantPhone().trim());
        }
        if (request.getReturnPolicyUrl() != null) {
            profile.setReturnPolicyUrl(request.getReturnPolicyUrl().trim());
        }
        if (request.getLegalName() != null) {
            profile.setLegalName(request.getLegalName().trim());
        }
        if (request.getStateTaxPermitNumber() != null) {
            profile.setStateTaxPermitNumber(request.getStateTaxPermitNumber().trim());
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
        if (request.getMinPriceFloor() != null) {
            listing.setMinPriceFloor(request.getMinPriceFloor());
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
    public ResponseDto<List<SellerOrderItemResponseDto>> getSellerOrders(
            String callerEmail,
            OrderItemStatus status,
            String searchKey,
            LocalDate startDate,
            LocalDate endDate) {
        User seller = resolveUser(callerEmail);

        Specification<OrderItem> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Mandatory seller tenant isolation
            predicates.add(cb.equal(root.get("seller").get("id"), seller.getId()));

            // 2. Status filter
            if (status != null) {
                predicates.add(cb.equal(root.get("itemStatus"), status));
            }

            // Join order
            Join<OrderItem, Order> orderJoin = root.join("order", JoinType.INNER);

            // 3. Date range filters
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        cb.coalesce(orderJoin.get("placedAt"), orderJoin.get("createdAt")),
                        startDate.atStartOfDay()
                ));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        cb.coalesce(orderJoin.get("placedAt"), orderJoin.get("createdAt")),
                        endDate.atTime(LocalTime.MAX)
                ));
            }

            // 4. Search key
            if (searchKey != null && !searchKey.isBlank()) {
                String pattern = "%" + searchKey.trim().toLowerCase(Locale.ROOT) + "%";
                List<Predicate> searchPredicates = new ArrayList<>();

                // Match order ID (UUID match if valid UUID, or orderNumber match)
                try {
                    UUID orderUuid = UUID.fromString(searchKey.trim());
                    searchPredicates.add(cb.equal(orderJoin.get("id"), orderUuid));
                } catch (IllegalArgumentException ignored) {
                }
                searchPredicates.add(cb.like(cb.lower(orderJoin.get("orderNumber")), pattern));

                // Match Buyer Name
                Join<Order, User> buyerJoin = orderJoin.join("user", JoinType.INNER);
                searchPredicates.add(cb.like(cb.lower(buyerJoin.get("fullName")), pattern));

                // Match ASIN
                Join<OrderItem, ProductVariant> variantJoin = root.join("productVariant", JoinType.INNER);
                searchPredicates.add(cb.like(cb.lower(variantJoin.get("asin")), pattern));

                // Match SKU
                Join<OrderItem, ProductListing> listingJoin = root.join("listing", JoinType.LEFT);
                searchPredicates.add(cb.like(cb.lower(listingJoin.get("sellerSku")), pattern));

                predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
            }

            query.orderBy(cb.desc(cb.coalesce(orderJoin.get("placedAt"), orderJoin.get("createdAt"))));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<OrderItem> orderItems = orderItemRepository.findAll(spec);
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

        // Buy Box Dominance Percentage: active listings winning the buy box
        double buyBoxDominance = 0.0;
        if (totalActiveListings > 0) {
            long buyBoxWinnerCount = listings.stream()
                    .filter(l -> l.getStatus() == ListingStatus.ACTIVE && Boolean.TRUE.equals(l.getIsBuyboxWinner()))
                    .count();
            buyBoxDominance = BigDecimal.valueOf((double) buyBoxWinnerCount * 100.0 / totalActiveListings)
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        // Stock alerts count: SKUs requiring stock action (stockQuantity <= 10 or inactive/suspended)
        long stockAlertsCount = listings.stream()
                .filter(l -> l.getStockQuantity() == null || l.getStockQuantity() <= 10 || l.getStatus() != ListingStatus.ACTIVE)
                .count();

        // Hourly order influx: distribution of orders across 24 hourly buckets
        Map<Integer, Long> hourlyCounts = new HashMap<>();
        LocalDateTime last24h = LocalDateTime.now().minusHours(24);
        for (OrderItem oi : orderItems) {
            Order o = oi.getOrder();
            LocalDateTime itemTime = (o != null && o.getPlacedAt() != null)
                    ? o.getPlacedAt()
                    : (o != null ? o.getCreatedAt() : null);
            if (itemTime != null && itemTime.isAfter(last24h)) {
                hourlyCounts.merge(itemTime.getHour(), 1L, Long::sum);
            }
        }
        List<HourlyOrderInfluxDto> hourlyOrderInflux = new ArrayList<>(24);
        for (int h = 0; h < 24; h++) {
            hourlyOrderInflux.add(HourlyOrderInfluxDto.builder()
                    .hour(String.format("%02d:00", h))
                    .orderCount(hourlyCounts.getOrDefault(h, 0L))
                    .build());
        }

        SellerAnalyticsResponseDto analytics = SellerAnalyticsResponseDto.builder()
                .totalActiveListings(totalActiveListings)
                .totalProducts(totalProducts)
                .totalRevenue(totalRevenue)
                .totalUnitsSold(totalUnitsSold)
                .totalPendingOrders(totalPendingOrders)
                .totalShippedOrders(totalShippedOrders)
                .totalDeliveredOrders(totalDeliveredOrders)
                .buyBoxDominancePercentage(buyBoxDominance)
                .stockAlertsCount(stockAlertsCount)
                .hourlyOrderInflux(hourlyOrderInflux)
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
                .supportEmail(profile.getSupportEmail())
                .merchantPhone(profile.getMerchantPhone())
                .returnPolicyUrl(profile.getReturnPolicyUrl())
                .legalName(profile.getLegalName())
                .stateTaxPermitNumber(profile.getStateTaxPermitNumber())
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
                .minPriceFloor(listing.getMinPriceFloor())
                .stockQuantity(listing.getStockQuantity())
                .fulfillmentType(listing.getFulfillmentType())
                .isBuyboxWinner(listing.getIsBuyboxWinner())
                .status(listing.getStatus())
                .createdAt(listing.getCreatedAt())
                .build();
    }

    private SellerOrderItemResponseDto mapToOrderItemResponse(OrderItem orderItem) {
        LocalDateTime orderDate = orderItem.getOrder() != null
                ? (orderItem.getOrder().getPlacedAt() != null ? orderItem.getOrder().getPlacedAt() : orderItem.getOrder().getCreatedAt())
                : null;
        LocalDateTime shipByDeadline = orderDate != null ? orderDate.plusDays(2) : LocalDateTime.now().plusDays(2);
        String buyerDestination = "Seattle, WA 98101";

        return SellerOrderItemResponseDto.builder()
                .orderItemId(orderItem.getId())
                .orderId(orderItem.getOrder() != null ? orderItem.getOrder().getId() : null)
                .productTitle(orderItem.getProductVariant() != null && orderItem.getProductVariant().getProduct() != null
                        ? orderItem.getProductVariant().getProduct().getTitle() : null)
                .variantName(orderItem.getProductVariant() != null ? orderItem.getProductVariant().getVariantName() : null)
                .asin(orderItem.getProductVariant() != null ? orderItem.getProductVariant().getAsin() : null)
                .sellerSku(orderItem.getListing() != null ? orderItem.getListing().getSellerSku() : null)
                .unitPrice(orderItem.getUnitPrice())
                .quantity(orderItem.getQuantity())
                .subtotal(orderItem.getSubtotal())
                .itemStatus(orderItem.getItemStatus())
                .buyerName(orderItem.getOrder() != null && orderItem.getOrder().getUser() != null
                        ? orderItem.getOrder().getUser().getFullName() : null)
                .orderDate(orderDate)
                .buyerDestination(buyerDestination)
                .shipByDeadline(shipByDeadline)
                .build();
    }
}
