package com.amazon.service;

import com.amazon.controller.SellerDashboardController;
import com.amazon.dtos.listing.request.CreateListingRequestDto;
import com.amazon.dtos.listing.request.UpdateListingStockRequestDto;
import com.amazon.dtos.listing.response.ListingResponseDto;
import com.amazon.dtos.listing.response.ProductListingResponseDto;
import com.amazon.dtos.seller.request.UpdateSellerProfileRequestDto;
import com.amazon.dtos.seller.response.HourlyOrderInfluxDto;
import com.amazon.dtos.seller.response.SellerAnalyticsResponseDto;
import com.amazon.dtos.seller.response.SellerOrderItemResponseDto;
import com.amazon.dtos.seller.response.SellerProfileResponseDto;
import com.amazon.entity.*;
import com.amazon.enums.*;
import com.amazon.payloads.ResponseDto;
import com.amazon.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SellerDashboardIntegrationTest {

    @Autowired
    private SellerDashboardService sellerDashboardService;

    @Autowired
    private SellerDashboardController sellerDashboardController;

    @Autowired
    private ProductListingService productListingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SellerProfileRepository sellerProfileRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private ProductListingRepository productListingRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testSeller;
    private User testBuyer;
    private Brand testBrand;
    private Category testCategory;
    private Product testProduct;
    private ProductVariant testVariant1;
    private ProductVariant testVariant2;

    @BeforeEach
    void setUp() {
        Role sellerRole = roleRepository.findByName("ROLE_SELLER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_SELLER").description("Seller").build()));
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").description("User").build()));

        String sellerEmail = "merchant-" + UUID.randomUUID() + "@test.com";
        testSeller = userRepository.save(User.builder()
                .email(sellerEmail)
                .fullName("Enterprise Merchant")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .status(UserStatus.ACTIVE)
                .roles(Set.of(sellerRole, userRole))
                .build());

        String brandId = UUID.randomUUID().toString().substring(0, 6);
        testBrand = brandRepository.save(Brand.builder()
                .name("Apex Retail " + brandId)
                .slug("apex-retail-" + brandId.toLowerCase())
                .ownerUser(testSeller)
                .status(BrandStatus.ACTIVE)
                .build());

        SellerProfile profile = SellerProfile.builder()
                .user(testSeller)
                .brand(testBrand)
                .storeName("Apex Store " + UUID.randomUUID().toString().substring(0, 6))
                .taxNumber("TX-123456")
                .businessAddress("789 Enterprise Blvd, Suite 400")
                .bankAccountDetails("IBAN: US99-APEX-0001")
                .supportEmail("support@apexstore.com")
                .merchantPhone("+1-800-555-0199")
                .returnPolicyUrl("https://apexstore.com/returns-policy")
                .legalName("Apex Global Merchants LLC")
                .stateTaxPermitNumber("STP-WA-987654")
                .isVerified(true)
                .build();
        sellerProfileRepository.save(profile);

        String buyerEmail = "buyer-" + UUID.randomUUID() + "@test.com";
        testBuyer = userRepository.save(User.builder()
                .email(buyerEmail)
                .fullName("Jane Doe")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .status(UserStatus.ACTIVE)
                .roles(Set.of(userRole))
                .build());

        testCategory = categoryRepository.save(Category.builder()
                .name("Electronics " + UUID.randomUUID().toString().substring(0, 6))
                .slug("electronics-" + UUID.randomUUID().toString().substring(0, 6))
                .level(0)
                .isApproved(true)
                .build());

        testProduct = productRepository.save(Product.builder()
                .seller(testSeller)
                .brand(testBrand)
                .category(testCategory)
                .title("Apex Pro Sound Headphones")
                .description("Noise cancelling wireless headphones")
                .basePrice(new BigDecimal("199.99"))
                .status(ProductStatus.ACTIVE)
                .build());

        testVariant1 = productVariantRepository.save(ProductVariant.builder()
                .product(testProduct)
                .asin("B09" + UUID.randomUUID().toString().substring(0, 7).toUpperCase())
                .variantName("Midnight Black")
                .build());

        testVariant2 = productVariantRepository.save(ProductVariant.builder()
                .product(testProduct)
                .asin("B09" + UUID.randomUUID().toString().substring(0, 7).toUpperCase())
                .variantName("Silver Frost")
                .build());
    }

    // =========================================================================
    // 1. SELLER PROFILE ENHANCEMENTS
    // =========================================================================

    @Test
    @DisplayName("Requirement 1: Seller Profile DTOs retrieve and update enterprise merchant fields")
    void testSellerProfileEnhancements() {
        // 1. Get profile
        ResponseDto<SellerProfileResponseDto> profileResponse =
                sellerDashboardService.getSellerProfile(testSeller.getEmail());

        assertNotNull(profileResponse.getData());
        SellerProfileResponseDto dto = profileResponse.getData();
        assertEquals("support@apexstore.com", dto.getSupportEmail());
        assertEquals("+1-800-555-0199", dto.getMerchantPhone());
        assertEquals("https://apexstore.com/returns-policy", dto.getReturnPolicyUrl());
        assertEquals("Apex Global Merchants LLC", dto.getLegalName());
        assertEquals("STP-WA-987654", dto.getStateTaxPermitNumber());

        // 2. Update profile with new values
        UpdateSellerProfileRequestDto updateRequest = UpdateSellerProfileRequestDto.builder()
                .supportEmail("helpdesk@apex-enterprise.io")
                .merchantPhone("+1-888-555-7777")
                .returnPolicyUrl("https://apex-enterprise.io/30-day-returns")
                .legalName("Apex Global Enterprise Inc.")
                .stateTaxPermitNumber("STP-WA-999999")
                .build();

        ResponseDto<SellerProfileResponseDto> updatedResponse =
                sellerDashboardService.updateSellerProfile(testSeller.getEmail(), updateRequest);

        assertNotNull(updatedResponse.getData());
        SellerProfileResponseDto updatedDto = updatedResponse.getData();
        assertEquals("helpdesk@apex-enterprise.io", updatedDto.getSupportEmail());
        assertEquals("+1-888-555-7777", updatedDto.getMerchantPhone());
        assertEquals("https://apex-enterprise.io/30-day-returns", updatedDto.getReturnPolicyUrl());
        assertEquals("Apex Global Enterprise Inc.", updatedDto.getLegalName());
        assertEquals("STP-WA-999999", updatedDto.getStateTaxPermitNumber());
    }

    // =========================================================================
    // 2. SELLER ORDERS & FILTERS ENHANCEMENTS
    // =========================================================================

    @Test
    @DisplayName("Requirement 2: Seller Orders supports status, searchKey, date range filtering, and enriched DTO fields")
    @org.springframework.security.test.context.support.WithMockUser(username = "seller@test.com", roles = {"SELLER"})
    void testSellerOrdersFilteringAndEnrichment() {
        // Create Product Listings
        ProductListing listing1 = productListingRepository.save(ProductListing.builder()
                .productVariant(testVariant1)
                .seller(testSeller)
                .sellerSku("APEX-AUDIO-BLK")
                .price(new BigDecimal("199.99"))
                .minPriceFloor(new BigDecimal("169.99"))
                .stockQuantity(50)
                .isBuyboxWinner(true)
                .status(ListingStatus.ACTIVE)
                .build());

        // Create Order 1 (PENDING)
        Order order1 = orderRepository.save(Order.builder()
                .orderNumber("ORD-1001-" + UUID.randomUUID().toString().substring(0, 4))
                .user(testBuyer)
                .shippingAddressId(UUID.randomUUID())
                .totalAmount(new BigDecimal("199.99"))
                .status(OrderStatus.PENDING)
                .placedAt(LocalDateTime.now().minusDays(5))
                .build());

        OrderItem item1 = orderItemRepository.save(OrderItem.builder()
                .order(order1)
                .listing(listing1)
                .productVariant(testVariant1)
                .seller(testSeller)
                .unitPrice(new BigDecimal("199.99"))
                .quantity(1)
                .subtotal(new BigDecimal("199.99"))
                .itemStatus(OrderItemStatus.PENDING)
                .build());

        // Create Order 2 (SHIPPED)
        Order order2 = orderRepository.save(Order.builder()
                .orderNumber("ORD-1002-" + UUID.randomUUID().toString().substring(0, 4))
                .user(testBuyer)
                .shippingAddressId(UUID.randomUUID())
                .totalAmount(new BigDecimal("399.98"))
                .status(OrderStatus.SHIPPED)
                .placedAt(LocalDateTime.now().minusDays(1))
                .build());

        OrderItem item2 = orderItemRepository.save(OrderItem.builder()
                .order(order2)
                .listing(listing1)
                .productVariant(testVariant1)
                .seller(testSeller)
                .unitPrice(new BigDecimal("199.99"))
                .quantity(2)
                .subtotal(new BigDecimal("399.98"))
                .itemStatus(OrderItemStatus.SHIPPED)
                .build());

        // 1. Test Filter by Status: SHIPPED
        ResponseDto<List<SellerOrderItemResponseDto>> shippedOnly =
                sellerDashboardService.getSellerOrders(testSeller.getEmail(), OrderItemStatus.SHIPPED, null, null, null);
        assertEquals(1, shippedOnly.getData().size());
        assertEquals(OrderItemStatus.SHIPPED, shippedOnly.getData().get(0).getItemStatus());

        // 2. Test Enriched Fields: buyerDestination & shipByDeadline
        SellerOrderItemResponseDto itemDto = shippedOnly.getData().get(0);
        assertEquals("Seattle, WA 98101", itemDto.getBuyerDestination());
        assertNotNull(itemDto.getShipByDeadline());
        assertTrue(itemDto.getShipByDeadline().isAfter(itemDto.getOrderDate()));

        // 3. Test Search Key by SKU
        ResponseDto<List<SellerOrderItemResponseDto>> bySku =
                sellerDashboardService.getSellerOrders(testSeller.getEmail(), null, "AUDIO-BLK", null, null);
        assertEquals(2, bySku.getData().size());

        // 4. Test Search Key by ASIN
        ResponseDto<List<SellerOrderItemResponseDto>> byAsin =
                sellerDashboardService.getSellerOrders(testSeller.getEmail(), null, testVariant1.getAsin(), null, null);
        assertEquals(2, byAsin.getData().size());

        // 5. Test Search Key by Buyer Name
        ResponseDto<List<SellerOrderItemResponseDto>> byBuyer =
                sellerDashboardService.getSellerOrders(testSeller.getEmail(), null, "Jane", null, null);
        assertEquals(2, byBuyer.getData().size());

        // 6. Test Date Range: only last 2 days
        LocalDate yesterday = LocalDate.now().minusDays(2);
        LocalDate today = LocalDate.now();
        ResponseDto<List<SellerOrderItemResponseDto>> recent =
                sellerDashboardService.getSellerOrders(testSeller.getEmail(), null, null, yesterday, today);
        assertEquals(1, recent.getData().size());
        assertEquals(item2.getId(), recent.getData().get(0).getOrderItemId());

        // 7. Verify via Controller endpoint
        Principal principal = () -> testSeller.getEmail();
        ResponseEntity<ResponseDto<List<SellerOrderItemResponseDto>>> controllerResponse =
                sellerDashboardController.getSellerOrders(principal, OrderItemStatus.PENDING, null, null, null);
        assertNotNull(controllerResponse.getBody());
        assertEquals(1, controllerResponse.getBody().getData().size());
        assertEquals(item1.getId(), controllerResponse.getBody().getData().get(0).getOrderItemId());
    }

    // =========================================================================
    // 3. SELLER ANALYTICS ENHANCEMENTS
    // =========================================================================

    @Test
    @DisplayName("Requirement 3: Seller Analytics includes hourlyOrderInflux, buyBoxDominancePercentage, and stockAlertsCount")
    void testSellerAnalyticsEnhancements() {
        // Create 2 listings: 1 winning with healthy stock, 1 not winning with low stock (<= 10)
        productListingRepository.save(ProductListing.builder()
                .productVariant(testVariant1)
                .seller(testSeller)
                .sellerSku("SKU-WIN-1")
                .price(new BigDecimal("149.99"))
                .minPriceFloor(new BigDecimal("129.99"))
                .stockQuantity(100)
                .isBuyboxWinner(true)
                .status(ListingStatus.ACTIVE)
                .build());

        productListingRepository.save(ProductListing.builder()
                .productVariant(testVariant2)
                .seller(testSeller)
                .sellerSku("SKU-LOW-2")
                .price(new BigDecimal("179.99"))
                .minPriceFloor(new BigDecimal("159.99"))
                .stockQuantity(4) // Stock alert: <= 10
                .isBuyboxWinner(false)
                .status(ListingStatus.ACTIVE)
                .build());

        ResponseDto<SellerAnalyticsResponseDto> analyticsResponse =
                sellerDashboardService.getSellerAnalytics(testSeller.getEmail());

        assertNotNull(analyticsResponse.getData());
        SellerAnalyticsResponseDto analytics = analyticsResponse.getData();

        // 1. BuyBox Dominance: 1 winner out of 2 active listings = 50.0%
        assertNotNull(analytics.getBuyBoxDominancePercentage());
        assertEquals(50.0, analytics.getBuyBoxDominancePercentage());

        // 2. Stock Alerts Count: 1 listing with stock <= 10
        assertNotNull(analytics.getStockAlertsCount());
        assertEquals(1L, analytics.getStockAlertsCount());

        // 3. Hourly Order Influx: 24 hourly buckets
        assertNotNull(analytics.getHourlyOrderInflux());
        assertEquals(24, analytics.getHourlyOrderInflux().size());
        assertEquals("00:00", analytics.getHourlyOrderInflux().get(0).getHour());
        assertEquals("23:00", analytics.getHourlyOrderInflux().get(23).getHour());
    }

    // =========================================================================
    // 4. PRODUCT LISTINGS MIN PRICE FLOOR ENHANCEMENTS
    // =========================================================================

    @Test
    @DisplayName("Requirement 4: CreateListingRequestDto and ProductListingResponseDto support minPriceFloor")
    void testProductListingMinPriceFloor() {
        CreateListingRequestDto request = CreateListingRequestDto.builder()
                .productVariantId(testVariant1.getId())
                .sellerSku("SKU-REPRICE-001")
                .price(new BigDecimal("189.99"))
                .minPriceFloor(new BigDecimal("149.99"))
                .stockQuantity(75)
                .fulfillmentType(FulfillmentType.FBA)
                .status(ListingStatus.ACTIVE)
                .build();

        ResponseDto<ProductListingResponseDto> response =
                productListingService.createListing(request, testSeller.getEmail());

        assertNotNull(response.getData());
        assertEquals(new BigDecimal("189.99"), response.getData().getPrice());
        assertEquals(new BigDecimal("149.99"), response.getData().getMinPriceFloor());

        // Verify retrieval by ID
        ResponseDto<ProductListingResponseDto> retrieved =
                productListingService.getListingById(response.getData().getId());
        assertEquals(new BigDecimal("149.99"), retrieved.getData().getMinPriceFloor());

        // Verify update of stock & minPriceFloor
        UpdateListingStockRequestDto updateRequest = UpdateListingStockRequestDto.builder()
                .stockQuantity(120)
                .price(new BigDecimal("179.99"))
                .minPriceFloor(new BigDecimal("139.99"))
                .build();

        ResponseDto<ProductListingResponseDto> updated =
                productListingService.updateStock(response.getData().getId(), updateRequest, testSeller.getEmail());
        assertEquals(new BigDecimal("139.99"), updated.getData().getMinPriceFloor());
        assertEquals(new BigDecimal("179.99"), updated.getData().getPrice());

        // Verify ListingResponseDto alias compatibility
        ListingResponseDto aliasDto = new ListingResponseDto();
        aliasDto.setMinPriceFloor(new BigDecimal("139.99"));
        assertEquals(new BigDecimal("139.99"), aliasDto.getMinPriceFloor());
    }
}
