package com.amazon.service.impl;

import com.amazon.dtos.admin.response.*;
import com.amazon.dtos.user.response.UserResponseDto;
import com.amazon.entity.*;
import com.amazon.enums.*;
import com.amazon.exception.ResourceNotFoundException;
import com.amazon.payloads.ApiResponse;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;
import com.amazon.repository.*;
import com.amazon.service.AdminGovernanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.JoinType;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminGovernanceServiceImpl implements AdminGovernanceService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final List<OrderStatus> EXCLUDED_GMV_STATUSES = List.of(OrderStatus.CANCELLED, OrderStatus.REFUNDED);
    private final UserRepository userRepository;
    private final SellerProfileRepository sellerRepository;
    private final OrderRepository orderRepository;
    private final RoleRepository roleRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    @Override @Transactional(readOnly = true)
    public ResponseDto<PaginationPayload<UserResponseDto>> getUsers(String role, Boolean active, String email, int page, int size) {
        Specification<User> spec = (root, query, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (active != null) predicates.add(cb.equal(root.get("status"), active ? UserStatus.ACTIVE : UserStatus.SUSPENDED));
            if (email != null && !email.isBlank()) predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.trim().toLowerCase(Locale.ROOT) + "%"));
            if (role != null && !role.isBlank()) {
                var roles = root.join("roles", JoinType.INNER);
                String normalized = role.trim().toUpperCase(Locale.ROOT);
                if (!normalized.startsWith("ROLE_")) normalized = "ROLE_" + normalized;
                predicates.add(cb.equal(roles.get("name"), normalized));
                query.distinct(true);
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        Page<User> results = userRepository.findAll(spec, pageRequest(page, size));
        return ApiResponse.success(pagination(results, results.map(this::mapUser).getContent()), "Users retrieved successfully");
    }

    @Override @Transactional
    public ResponseDto<UserResponseDto> updateUserStatus(UUID id, boolean active) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(active ? UserStatus.ACTIVE : UserStatus.SUSPENDED);
        return ApiResponse.success(mapUser(userRepository.save(user)), "User status updated successfully");
    }

    @Override @Transactional
    public ResponseDto<UserResponseDto> updateUserRoles(UUID id, Set<String> roleNames) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Set<Role> roles = roleNames.stream().map(String::trim).map(name -> name.toUpperCase(Locale.ROOT))
                .map(name -> name.startsWith("ROLE_") ? name : "ROLE_" + name)
                .map(name -> roleRepository.findByName(name).orElseThrow(() -> new ResourceNotFoundException("Role not found: " + name)))
                .collect(Collectors.toSet());
        user.setRoles(roles);
        return ApiResponse.success(mapUser(userRepository.save(user)), "User roles updated successfully");
    }

    @Override @Transactional(readOnly = true)
    public ResponseDto<PaginationPayload<AdminSellerResponseDto>> getSellers(Boolean verified, int page, int size) {
        Specification<SellerProfile> spec = (root, query, cb) -> verified == null ? cb.conjunction() : cb.equal(root.get("isVerified"), verified);
        Page<SellerProfile> results = sellerRepository.findAll(spec, pageRequest(page, size));
        return ApiResponse.success(pagination(results, results.getContent().stream().map(this::mapSeller).toList()), "Sellers retrieved successfully");
    }

    @Override @Transactional
    public ResponseDto<AdminSellerResponseDto> updateSellerVerification(UUID id, boolean verified) {
        SellerProfile seller = sellerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Seller profile not found"));
        seller.setIsVerified(verified);
        return ApiResponse.success(mapSeller(sellerRepository.save(seller)), "Seller verification status updated successfully");
    }

    @Override @Transactional(readOnly = true)
    public ResponseDto<PaginationPayload<AdminOrderResponseDto>> getOrders(OrderStatus status, UUID sellerId, UUID buyerId, String orderNumber, int page, int size) {
        Specification<Order> spec = (root, query, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (buyerId != null) predicates.add(cb.equal(root.get("user").get("id"), buyerId));
            if (orderNumber != null && !orderNumber.isBlank()) predicates.add(cb.like(cb.lower(root.get("orderNumber")), "%" + orderNumber.trim().toLowerCase(Locale.ROOT) + "%"));
            if (sellerId != null) {
                var items = root.join("items", JoinType.INNER);
                predicates.add(cb.equal(items.get("seller").get("id"), sellerId));
                query.distinct(true);
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        Page<Order> results = orderRepository.findAll(spec, pageRequest(page, size));
        return ApiResponse.success(pagination(results, results.getContent().stream().map(this::mapOrder).toList()), "Orders retrieved successfully");
    }

    @Override @Transactional
    public ResponseDto<AdminOrderResponseDto> overrideOrderStatus(UUID id, OrderStatus status) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setStatus(status);
        return ApiResponse.success(mapOrder(orderRepository.save(order)), "Order status overridden successfully");
    }

    @Override @Transactional(readOnly = true)
    public ResponseDto<AdminAnalyticsResponseDto> getAnalytics() {
        AdminAnalyticsResponseDto metrics = AdminAnalyticsResponseDto.builder()
                .totalGMV(Optional.ofNullable(orderRepository.calculateGmv()).orElse(BigDecimal.ZERO))
                .totalOrdersCount(orderRepository.count())
                .totalActiveUsers(userRepository.count((root, query, cb) -> cb.equal(root.get("status"), UserStatus.ACTIVE)))
                .totalActiveSellers(sellerRepository.countActiveVerifiedSellers())
                .totalActiveBrands(brandRepository.countByStatus(BrandStatus.ACTIVE))
                .topPerformingBrands(mapMetrics(brandRepository.findTopRevenueBrands(PageRequest.of(0, 5))))
                .topPerformingCategories(mapMetrics(categoryRepository.findTopRevenueCategories(PageRequest.of(0, 5))))
                .build();
        return ApiResponse.success(metrics, "Platform analytics retrieved successfully");
    }

    private PageRequest pageRequest(int page, int size) {
        return PageRequest.of(Math.max(0, page), Math.max(1, Math.min(size, MAX_PAGE_SIZE)), Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private <T> PaginationPayload<T> pagination(Page<?> page, List<T> content) {
        return PaginationPayload.<T>builder().content(content).pageNumber(page.getNumber()).pageSize(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages()).last(page.isLast()).build();
    }

    private UserResponseDto mapUser(User user) {
        return UserResponseDto.builder().id(user.getId()).fullName(user.getFullName()).email(user.getEmail()).phone(user.getPhone())
                .status(user.getStatus()).roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())).build();
    }

    private AdminSellerResponseDto mapSeller(SellerProfile seller) {
        return AdminSellerResponseDto.builder().id(seller.getId()).userId(seller.getUser().getId()).email(seller.getUser().getEmail())
                .fullName(seller.getUser().getFullName()).storeName(seller.getStoreName()).brandId(seller.getBrand() == null ? null : seller.getBrand().getId())
                .brandName(seller.getBrand() == null ? null : seller.getBrand().getName()).verified(seller.getIsVerified())
                .active(seller.getUser().getStatus() == UserStatus.ACTIVE).build();
    }

    private AdminOrderResponseDto mapOrder(Order order) {
        return AdminOrderResponseDto.builder().id(order.getId()).orderNumber(order.getOrderNumber()).buyerId(order.getUser().getId())
                .buyerEmail(order.getUser().getEmail()).totalAmount(order.getTotalAmount()).status(order.getStatus()).placedAt(order.getPlacedAt()).build();
    }

    private List<TopMetricResponseDto> mapMetrics(List<Object[]> rows) {
        return rows.stream().map(row -> TopMetricResponseDto.builder().id((UUID) row[0]).name((String) row[1])
                .revenue((BigDecimal) row[2]).build()).toList();
    }
}
