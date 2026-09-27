package com.amazon.service;

import com.amazon.dtos.admin.response.AdminAnalyticsResponseDto;
import com.amazon.dtos.admin.response.DailyGmvPointDto;
import com.amazon.dtos.auth.request.LoginRequestDto;
import com.amazon.dtos.category.request.CreateCategoryRequestDto;
import com.amazon.dtos.category.response.CategoryResponseDto;
import com.amazon.entity.BrandApplication;
import com.amazon.entity.Order;
import com.amazon.entity.User;
import com.amazon.enums.BrandApplicationStatus;
import com.amazon.enums.OrderStatus;
import com.amazon.enums.UserStatus;
import com.amazon.payloads.ResponseDto;
import com.amazon.repository.BrandApplicationRepository;
import com.amazon.repository.OrderRepository;
import com.amazon.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminGovernanceIntegrationTest {
    @Autowired private AdminGovernanceService adminService;
    @Autowired private CategoryService categoryService;
    @Autowired private UserRepository userRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private BrandApplicationRepository brandApplicationRepository;
    @Autowired private AuthService authService;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void suspendsUser() {
        User user = createUser("admin-governance-user@example.com");
        var result = adminService.updateUserStatus(user.getId(), false);
        assertEquals(UserStatus.SUSPENDED, result.getData().getStatus());
    }

    @Test
    void approvesPendingCategory() {
        var proposal = categoryService.createBrandCategory(CreateCategoryRequestDto.builder().name("Admin Queue Category").build());
        assertFalse(proposal.getData().getIsApproved());
        ResponseDto<CategoryResponseDto> approved = categoryService.approveCategory(proposal.getData().getId());
        assertTrue(approved.getData().getIsApproved());
        assertNull(approved.getData().getRejectionReason());
    }

    @Test
    void overridesOrderStatus() {
        User buyer = createUser("admin-governance-buyer@example.com");
        Order order = orderRepository.save(Order.builder().orderNumber("ADMIN-" + UUID.randomUUID())
                .user(buyer).shippingAddressId(UUID.randomUUID()).totalAmount(new BigDecimal("42.50"))
                .status(OrderStatus.PENDING).build());
        var result = adminService.overrideOrderStatus(order.getId(), OrderStatus.REFUNDED);
        assertEquals(OrderStatus.REFUNDED, result.getData().getStatus());
    }

    @Test
    void calculatesPlatformAnalytics() {
        User buyer = createUser("admin-governance-analytics@example.com");
        orderRepository.save(Order.builder().orderNumber("ANALYTICS-" + UUID.randomUUID()).user(buyer)
                .shippingAddressId(UUID.randomUUID()).totalAmount(new BigDecimal("125.75"))
                .status(OrderStatus.DELIVERED).build());

        brandApplicationRepository.save(BrandApplication.builder()
                .applicantName("Pending Founder")
                .applicantEmail("founder-" + UUID.randomUUID() + "@brand.com")
                .passwordHash("secret")
                .brandName("Brand " + UUID.randomUUID())
                .brandSlug("brand-" + UUID.randomUUID())
                .trademarkRegistrationNumber("TM-998811")
                .brandCountry("US")
                .status(BrandApplicationStatus.PENDING)
                .build());

        ResponseDto<AdminAnalyticsResponseDto> response = adminService.getAnalytics();
        assertTrue(response.getData().getTotalGMV().compareTo(new BigDecimal("125.75")) >= 0);
        assertTrue(response.getData().getTotalOrdersCount() >= 1);
        assertNotNull(response.getData().getTopPerformingBrands());
        assertNotNull(response.getData().getTopPerformingCategories());

        // Verify Daily GMV Trajectory
        List<DailyGmvPointDto> trajectory = response.getData().getDailyGmvTrajectory();
        assertNotNull(trajectory);
        assertEquals(30, trajectory.size(), "Trajectory must contain exactly 30 days");
        DailyGmvPointDto latestPoint = trajectory.get(trajectory.size() - 1);
        assertEquals(LocalDate.now(), latestPoint.getDate());
        assertTrue(latestPoint.getGmv().compareTo(BigDecimal.ZERO) >= 0);

        // Verify Total Pending Brand Applications count
        assertNotNull(response.getData().getTotalPendingBrandApplications());
        assertTrue(response.getData().getTotalPendingBrandApplications() >= 1);
    }

    @Test
    void filtersOrdersByDateRange() {
        User buyer = createUser("admin-order-filter@example.com");
        LocalDate today = LocalDate.now();

        Order orderToday = orderRepository.save(Order.builder().orderNumber("FILTER-TODAY-" + UUID.randomUUID())
                .user(buyer).shippingAddressId(UUID.randomUUID()).totalAmount(new BigDecimal("99.00"))
                .status(OrderStatus.CONFIRMED).build());

        var allOrders = adminService.getOrders(null, null, buyer.getId(), null, today, today, 0, 10);
        assertTrue(allOrders.getData().getContent().stream()
                .anyMatch(o -> o.getOrderNumber().equals(orderToday.getOrderNumber())));

        var pastOrders = adminService.getOrders(null, null, buyer.getId(), null,
                today.minusDays(10), today.minusDays(5), 0, 10);
        assertTrue(pastOrders.getData().getContent().stream()
                .noneMatch(o -> o.getOrderNumber().equals(orderToday.getOrderNumber())));
    }

    @Test
    void userDtoContainsAvatarAndLastActiveAtAndUpdatesOnLogin() {
        String email = "active-user-" + UUID.randomUUID() + "@example.com";
        String password = "SecurePassword123!";
        User user = userRepository.save(User.builder()
                .email(email)
                .fullName("Active User")
                .passwordHash(passwordEncoder.encode(password))
                .avatarUrl("https://images.example.com/avatar1.png")
                .status(UserStatus.ACTIVE)
                .build());

        var usersResponse = adminService.getUsers(null, null, email, 0, 10);
        assertEquals(1, usersResponse.getData().getContent().size());
        var userDto = usersResponse.getData().getContent().get(0);
        assertEquals("https://images.example.com/avatar1.png", userDto.getAvatarUrl());

        // Perform login and verify lastActiveAt updates
        var loginResponse = authService.login(new LoginRequestDto(email, password));
        assertNotNull(loginResponse.getData().getUser().getLastActiveAt());

        // Verify via admin getUsers as well
        var refreshedUsers = adminService.getUsers(null, null, email, 0, 10);
        assertNotNull(refreshedUsers.getData().getContent().get(0).getLastActiveAt());
    }

    @Test
    void categoryProposalSupportsCommercialJustification() {
        String justification = "High-growth market category expanding our enterprise catalogue.";
        var proposal = categoryService.createBrandCategory(CreateCategoryRequestDto.builder()
                .name("Cat With Justification " + UUID.randomUUID())
                .commercialJustification(justification)
                .build());

        assertEquals(justification, proposal.getData().getCommercialJustification());

        // Check it appears in pending categories list
        var pending = categoryService.getPendingCategories();
        assertTrue(pending.getData().stream()
                .anyMatch(c -> justification.equals(c.getCommercialJustification())));
    }

    private User createUser(String email) {
        return userRepository.save(User.builder().email(email).fullName("Integration User")
                .passwordHash("test-hash").status(UserStatus.ACTIVE).build());
    }
}
