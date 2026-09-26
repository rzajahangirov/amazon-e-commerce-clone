package com.amazon.service;

import com.amazon.dtos.admin.response.AdminAnalyticsResponseDto;
import com.amazon.dtos.category.request.CreateCategoryRequestDto;
import com.amazon.dtos.category.response.CategoryResponseDto;
import com.amazon.entity.Order;
import com.amazon.entity.User;
import com.amazon.enums.OrderStatus;
import com.amazon.enums.UserStatus;
import com.amazon.payloads.ResponseDto;
import com.amazon.repository.OrderRepository;
import com.amazon.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
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
        ResponseDto<AdminAnalyticsResponseDto> response = adminService.getAnalytics();
        assertTrue(response.getData().getTotalGMV().compareTo(new BigDecimal("125.75")) >= 0);
        assertTrue(response.getData().getTotalOrdersCount() >= 1);
        assertNotNull(response.getData().getTopPerformingBrands());
        assertNotNull(response.getData().getTopPerformingCategories());
    }

    private User createUser(String email) {
        return userRepository.save(User.builder().email(email).fullName("Integration User")
                .passwordHash("test-hash").status(UserStatus.ACTIVE).build());
    }
}
