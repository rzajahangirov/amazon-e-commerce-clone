package com.amazon.service.impl;

import com.amazon.dtos.order.request.CheckoutRequestDto;
import com.amazon.dtos.order.request.UpdateOrderStatusRequestDto;
import com.amazon.dtos.order.response.OrderItemResponseDto;
import com.amazon.dtos.order.response.OrderResponseDto;
import com.amazon.entity.*;
import com.amazon.enums.OrderItemStatus;
import com.amazon.enums.OrderStatus;
import com.amazon.exception.BusinessRuleException;
import com.amazon.exception.ResourceNotFoundException;
import com.amazon.payloads.ApiResponse;
import com.amazon.payloads.AuthError;
import com.amazon.payloads.CatalogError;
import com.amazon.payloads.OrderError;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;
import com.amazon.repository.CartItemRepository;
import com.amazon.repository.CartRepository;
import com.amazon.repository.OrderRepository;
import com.amazon.repository.ProductListingRepository;
import com.amazon.repository.UserRepository;
import com.amazon.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service implementation for managing the order lifecycle and checkout workflow.
 * Strictly adheres to Senior Backend Developer Guidelines.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductListingRepository productListingRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ResponseDto<OrderResponseDto> checkoutFromCart(String userEmail, CheckoutRequestDto request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(AuthError.USER_NOT_FOUND.getMessage()));

        Cart cart = cartRepository.findWithItemsByUserEmail(userEmail)
                .orElseThrow(() -> new BusinessRuleException(OrderError.EMPTY_CART_CHECKOUT.getMessage()));

        List<CartItem> activeItems = cart.getItems().stream()
                .filter(item -> !Boolean.TRUE.equals(item.getIsSavedForLater()))
                .toList();

        if (activeItems.isEmpty()) {
            throw new BusinessRuleException(OrderError.EMPTY_CART_CHECKOUT.getMessage());
        }

        // Validate stock availability for all active items
        for (CartItem item : activeItems) {
            ProductListing listing = item.getListing();
            if (!listing.isAvailableForPurchase(item.getQuantity())) {
                throw new BusinessRuleException(CatalogError.INSUFFICIENT_STOCK.getMessage()
                        + " for item: " + listing.getSellerSku());
            }
        }

        String orderNumber = generateOrderNumber();
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .shippingAddressId(request.getShippingAddressId())
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        for (CartItem item : activeItems) {
            ProductListing listing = item.getListing();
            BigDecimal unitPrice = listing.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            // Concurrency-safe inventory decrement (guarded by @Version on ProductListing)
            listing.deductStock(item.getQuantity());
            productListingRepository.save(listing);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .listing(listing)
                    .productVariant(listing.getProductVariant())
                    .seller(listing.getSeller())
                    .unitPrice(unitPrice)
                    .quantity(item.getQuantity())
                    .subtotal(subtotal)
                    .itemStatus(OrderItemStatus.PENDING)
                    .build();

            order.addItem(orderItem);
            orderItems.add(orderItem);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        // Remove purchased items from the user's cart
        cart.getItems().removeAll(activeItems);
        cartItemRepository.deleteAll(activeItems);
        cartRepository.save(cart);

        log.info("Order successfully placed with orderNumber: {} for user: {}, totalAmount: {}",
                savedOrder.getOrderNumber(), userEmail, totalAmount);

        return ApiResponse.success(mapToOrderResponseDto(savedOrder), "Order placed successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<OrderResponseDto> getOrderById(UUID orderId, String userEmail) {
        Order order = orderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(OrderError.ORDER_NOT_FOUND.getMessage()));

        validateOrderOwnership(order, userEmail);

        return ApiResponse.success(mapToOrderResponseDto(order), "Order retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<OrderResponseDto> getOrderByOrderNumber(String orderNumber, String userEmail) {
        Order order = orderRepository.findWithDetailsByOrderNumber(orderNumber.trim())
                .orElseThrow(() -> new ResourceNotFoundException(OrderError.ORDER_NOT_FOUND.getMessage()));

        validateOrderOwnership(order, userEmail);

        return ApiResponse.success(mapToOrderResponseDto(order), "Order retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<PaginationPayload<OrderResponseDto>> getMyOrders(String userEmail, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "placedAt"));
        Page<Order> orderPage = orderRepository.findByUserEmail(userEmail, pageRequest);

        List<OrderResponseDto> content = orderPage.getContent().stream()
                .map(this::mapToOrderResponseDto)
                .toList();

        PaginationPayload<OrderResponseDto> paginationPayload = PaginationPayload.<OrderResponseDto>builder()
                .content(content)
                .pageNumber(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .last(orderPage.isLast())
                .build();

        return ApiResponse.success(paginationPayload, "Orders retrieved successfully");
    }

    @Override
    @Transactional
    public ResponseDto<OrderResponseDto> updateOrderStatus(UUID orderId, UpdateOrderStatusRequestDto request) {
        Order order = orderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(OrderError.ORDER_NOT_FOUND.getMessage()));

        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = request.getStatus();

        if (oldStatus == OrderStatus.CANCELLED || oldStatus == OrderStatus.DELIVERED) {
            throw new BusinessRuleException(OrderError.INVALID_STATUS_TRANSITION.getMessage()
                    + " from " + oldStatus + " to " + newStatus);
        }

        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);
        log.info("Order status updated for order: {} from {} to {}", order.getOrderNumber(), oldStatus, newStatus);

        return ApiResponse.success(mapToOrderResponseDto(updated), "Order status updated successfully");
    }

    @Override
    @Transactional
    public ResponseDto<OrderResponseDto> cancelOrder(UUID orderId, String userEmail) {
        Order order = orderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(OrderError.ORDER_NOT_FOUND.getMessage()));

        validateOrderOwnership(order, userEmail);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessRuleException(OrderError.ORDER_ALREADY_CANCELLED.getMessage());
        }

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessRuleException(OrderError.ORDER_CANNOT_BE_CANCELLED.getMessage());
        }

        // Restore inventory stock back to listings
        for (OrderItem item : order.getItems()) {
            if (item.getListing() != null) {
                ProductListing listing = item.getListing();
                listing.addStock(item.getQuantity());
                productListingRepository.save(listing);
            }
            item.setItemStatus(OrderItemStatus.CANCELLED);
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);
        log.info("Order cancelled successfully with orderNumber: {} by user: {}", order.getOrderNumber(), userEmail);

        return ApiResponse.success(mapToOrderResponseDto(cancelledOrder), "Order cancelled successfully");
    }

    private void validateOrderOwnership(Order order, String userEmail) {
        boolean isOwner = order.getUser() != null && order.getUser().getEmail().equalsIgnoreCase(userEmail);
        boolean isAdmin = false;
        if (!isOwner) {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null) {
                isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
            }
        }
        if (!isOwner && !isAdmin) {
            throw new BusinessRuleException(OrderError.ORDER_ACCESS_DENIED.getMessage());
        }
    }

    private String generateOrderNumber() {
        return "AMZ-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private OrderResponseDto mapToOrderResponseDto(Order order) {
        List<OrderItemResponseDto> itemDtos = new ArrayList<>();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                ProductVariant variant = item.getProductVariant();
                itemDtos.add(OrderItemResponseDto.builder()
                        .id(item.getId())
                        .listingId(item.getListing() != null ? item.getListing().getId() : null)
                        .productVariantId(variant != null ? variant.getId() : null)
                        .productTitle(variant != null && variant.getProduct() != null ? variant.getProduct().getTitle() : null)
                        .variantName(variant != null ? variant.getVariantName() : null)
                        .asin(variant != null ? variant.getAsin() : null)
                        .sellerId(item.getSeller() != null ? item.getSeller().getId() : null)
                        .sellerName(item.getSeller() != null ? item.getSeller().getFullName() : null)
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .itemStatus(item.getItemStatus())
                        .build());
            }
        }

        return OrderResponseDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .userEmail(order.getUser() != null ? order.getUser().getEmail() : null)
                .shippingAddressId(order.getShippingAddressId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .placedAt(order.getPlacedAt())
                .items(itemDtos)
                .build();
    }
}
