package com.amazon.service;

import com.amazon.dtos.auth.request.RegisterRequestDto;
import com.amazon.dtos.brand.request.CreateBrandApplicationRequestDto;
import com.amazon.dtos.brand.request.CreateBrandCatalogProductRequestDto;
import com.amazon.dtos.category.request.CreateCategoryRequestDto;
import com.amazon.dtos.category.response.CategoryResponseDto;
import com.amazon.dtos.product.request.ProductSearchRequestDto;
import com.amazon.dtos.product.response.ProductResponseDto;
import com.amazon.dtos.review.request.CreateReviewRequestDto;
import com.amazon.entity.*;
import com.amazon.enums.OrderItemStatus;
import com.amazon.enums.OrderStatus;
import com.amazon.enums.ProductSortBy;
import com.amazon.enums.ProductStatus;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;
import com.amazon.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Amazon-Style Unified Search & Discovery Engine,
 * View Count Atomic Increments, Delivered Units Sold Aggregations, and Multi-Criteria Filtering.
 * Strictly adheres to Senior Backend Developer Guidelines.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductSearchAndDiscoveryIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandApplicationService brandApplicationService;

    @Autowired
    private BrandDashboardService brandDashboardService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    private UUID parentCategoryId;
    private UUID childCategoryId;
    private UUID brandAppleId;
    private UUID brandSonyId;

    private UUID productApplePhoneId;
    private UUID productAppleLaptopId;
    private UUID productSonyHeadphonesId;

    private final String customerEmail = "customer.discovery@example.com";
    private final String appleOwnerEmail = "owner@apple.com";
    private final String sonyOwnerEmail = "owner@sony.com";

    @BeforeEach
    void setUp() {
        // Register customer user
        registerUser("Discovery Customer", customerEmail);

        // 1. Create Hierarchical Categories: "Electronics" (parent) -> "Smartphones & Audio" (child)
        ResponseDto<CategoryResponseDto> parentCatResp = categoryService.createCategory(
                CreateCategoryRequestDto.builder()
                        .name("Electronics Hierarchy")
                        .slug("electronics-hierarchy")
                        .level(0)
                        .build()
        );
        parentCategoryId = parentCatResp.getData().getId();

        ResponseDto<CategoryResponseDto> childCatResp = categoryService.createCategory(
                CreateCategoryRequestDto.builder()
                        .name("Smartphones and Audio")
                        .slug("smartphones-and-audio")
                        .parentId(parentCategoryId)
                        .level(1)
                        .build()
        );
        childCategoryId = childCatResp.getData().getId();

        // 2. Setup Brands: Apple and Sony
        brandAppleId = createApprovedBrand("Apple Inc", "apple-inc", appleOwnerEmail);
        brandSonyId = createApprovedBrand("Sony Corporation", "sony-corp", sonyOwnerEmail);

        // 3. Create Products
        // Product 1: Apple iPhone 15 Pro (in Child category)
        ResponseDto<ProductResponseDto> prod1 = brandDashboardService.createBrandProductTemplate(appleOwnerEmail,
                CreateBrandCatalogProductRequestDto.builder()
                        .categoryId(childCategoryId)
                        .title("Apple iPhone 15 Pro Max Titanium")
                        .description("Flagship smartphone with A17 Pro chip and super retina XDR display")
                        .build()
        );
        productApplePhoneId = prod1.getData().getId();

        // Product 2: Apple MacBook Pro 16 (in Parent category)
        ResponseDto<ProductResponseDto> prod2 = brandDashboardService.createBrandProductTemplate(appleOwnerEmail,
                CreateBrandCatalogProductRequestDto.builder()
                        .categoryId(parentCategoryId)
                        .title("Apple MacBook Pro 16 M3 Max")
                        .description("Professional workstation laptop with Liquid Retina XDR display")
                        .build()
        );
        productAppleLaptopId = prod2.getData().getId();

        // Product 3: Sony WH-1000XM5 (in Child category)
        ResponseDto<ProductResponseDto> prod3 = brandDashboardService.createBrandProductTemplate(sonyOwnerEmail,
                CreateBrandCatalogProductRequestDto.builder()
                        .categoryId(childCategoryId)
                        .title("Sony WH-1000XM5 Wireless Noise Cancelling Headphones")
                        .description("Premium over-ear wireless headphones with industry leading ANC")
                        .build()
        );
        productSonyHeadphonesId = prod3.getData().getId();

        // Activate all products with prices and images
        activateProduct(productApplePhoneId, new BigDecimal("1199.99"), "https://images.example.com/iphone15.jpg");
        activateProduct(productAppleLaptopId, new BigDecimal("2499.00"), "https://images.example.com/macbook.jpg");
        activateProduct(productSonyHeadphonesId, new BigDecimal("399.99"), "https://images.example.com/sony-xm5.jpg");
    }

    @Test
    @DisplayName("Requirement 1: View Count - Atomically increments by 1 on each detail fetch")
    void testAtomicViewCountIncrement() {
        // Fetch 1st time
        ResponseDto<ProductResponseDto> firstFetch = productService.getProductById(productApplePhoneId);
        assertNotNull(firstFetch.getData());
        assertEquals(1L, firstFetch.getData().getViewCount(), "First detail fetch should result in viewCount = 1");

        // Fetch 2nd time
        ResponseDto<ProductResponseDto> secondFetch = productService.getProductById(productApplePhoneId);
        assertEquals(2L, secondFetch.getData().getViewCount(), "Second detail fetch should result in viewCount = 2");

        // Verify in DB directly
        Product product = productRepository.findById(productApplePhoneId).orElseThrow();
        assertEquals(2L, product.getViewCount(), "Product entity viewCount must be 2");
    }

    @Test
    @DisplayName("Requirement 1 & 2: Performance Metrics - averageRating, totalReviews, totalUnitsSold, buyBoxPrice")
    void testPerformanceMetricsAggregation() {
        // 1. Submit 5-star review for Sony headphones
        reviewService.createReview(customerEmail, productSonyHeadphonesId,
                CreateReviewRequestDto.builder().rating(5).comment("Best headphones ever!").build());

        // 2. Create delivered order item for Sony headphones (quantity = 7)
        createDeliveredOrderItem(productSonyHeadphonesId, 7);

        // 3. Fetch product detail
        ResponseDto<ProductResponseDto> response = productService.getProductById(productSonyHeadphonesId);
        ProductResponseDto dto = response.getData();

        assertNotNull(dto);
        assertEquals(5.0, dto.getAverageRating(), "Average rating should be 5.0");
        assertEquals(1, dto.getTotalReviews(), "Total reviews should be 1");
        assertEquals(7L, dto.getTotalUnitsSold(), "Total units sold should be 7 from DELIVERED order item");
        assertEquals(new BigDecimal("399.99"), dto.getBuyBoxPrice(), "BuyBox price fallback to basePrice");
        assertEquals("https://images.example.com/sony-xm5.jpg", dto.getMainImageUrl());
        assertEquals("Sony Corporation", dto.getBrandName());
        assertEquals("Smartphones and Audio", dto.getCategoryName());
    }

    @Test
    @DisplayName("Requirement 2: Search by Keyword query across title, description, brand, and category")
    void testSearchByKeyword() {
        // Search by keyword in title: "Titanium"
        ProductSearchRequestDto reqTitle = ProductSearchRequestDto.builder().query("Titanium").build();
        ResponseDto<PaginationPayload<ProductResponseDto>> resTitle = productService.searchProducts(reqTitle, null);
        assertEquals(1, resTitle.getData().getTotalElements());
        assertEquals(productApplePhoneId, resTitle.getData().getContent().get(0).getId());

        // Search by brand name keyword: "Apple"
        ProductSearchRequestDto reqBrand = ProductSearchRequestDto.builder().query("Apple").build();
        ResponseDto<PaginationPayload<ProductResponseDto>> resBrand = productService.searchProducts(reqBrand, null);
        assertEquals(2, resBrand.getData().getTotalElements(), "Apple keyword should match iPhone and MacBook");

        // Search by description keyword: "ANC"
        ProductSearchRequestDto reqDesc = ProductSearchRequestDto.builder().query("ANC").build();
        ResponseDto<PaginationPayload<ProductResponseDto>> resDesc = productService.searchProducts(reqDesc, null);
        assertEquals(1, resDesc.getData().getTotalElements());
        assertEquals(productSonyHeadphonesId, resDesc.getData().getContent().get(0).getId());
    }

    @Test
    @DisplayName("Requirement 2: Category Filter includes Child Categories in Search")
    void testCategoryFilterWithHierarchy() {
        // Filter by parent category: should return products in parent ("Electronics Hierarchy") AND child ("Smartphones and Audio")
        ProductSearchRequestDto reqParent = ProductSearchRequestDto.builder()
                .categoryId(parentCategoryId)
                .build();
        ResponseDto<PaginationPayload<ProductResponseDto>> resParent = productService.searchProducts(reqParent, null);
        assertEquals(3, resParent.getData().getTotalElements(), "Filtering by parent category must include child category products");

        // Filter by child category: should only return products in child category (iPhone + Sony headphones)
        ProductSearchRequestDto reqChild = ProductSearchRequestDto.builder()
                .categoryId(childCategoryId)
                .build();
        ResponseDto<PaginationPayload<ProductResponseDto>> resChild = productService.searchProducts(reqChild, null);
        assertEquals(2, resChild.getData().getTotalElements());

        // Filter by category slug
        ProductSearchRequestDto reqSlug = ProductSearchRequestDto.builder()
                .categorySlug("electronics-hierarchy")
                .build();
        ResponseDto<PaginationPayload<ProductResponseDto>> resSlug = productService.searchProducts(reqSlug, null);
        assertEquals(3, resSlug.getData().getTotalElements());
    }

    @Test
    @DisplayName("Requirement 2: Multi-criteria price range and rating filters")
    void testPriceAndRatingFilters() {
        // Price filter: between $1000 and $3000 (iPhone $1199.99 and MacBook $2499.00)
        ProductSearchRequestDto reqPrice = ProductSearchRequestDto.builder()
                .minPrice(new BigDecimal("1000.00"))
                .maxPrice(new BigDecimal("3000.00"))
                .build();
        ResponseDto<PaginationPayload<ProductResponseDto>> resPrice = productService.searchProducts(reqPrice, null);
        assertEquals(2, resPrice.getData().getTotalElements());

        // Rating filter: Add 5-star review to iPhone
        reviewService.createReview(customerEmail, productApplePhoneId,
                CreateReviewRequestDto.builder().rating(5).comment("Superb!").build());

        ProductSearchRequestDto reqRating = ProductSearchRequestDto.builder()
                .minRating(4.0)
                .build();
        ResponseDto<PaginationPayload<ProductResponseDto>> resRating = productService.searchProducts(reqRating, null);
        assertEquals(1, resRating.getData().getTotalElements());
        assertEquals(productApplePhoneId, resRating.getData().getContent().get(0).getId());
    }

    @Test
    @DisplayName("Requirement 2: Sorting Options - BEST_SELLERS, MOST_VIEWED, PRICE_ASC, PRICE_DESC")
    void testSortingOptions() {
        // 1. Give iPhone 15 units sold, Sony 5 units sold, MacBook 0 units sold
        createDeliveredOrderItem(productApplePhoneId, 15);
        createDeliveredOrderItem(productSonyHeadphonesId, 5);

        ProductSearchRequestDto bestSellersReq = ProductSearchRequestDto.builder()
                .sortBy(ProductSortBy.BEST_SELLERS)
                .build();
        ResponseDto<PaginationPayload<ProductResponseDto>> bestSellersRes = productService.searchProducts(bestSellersReq, null);
        assertEquals(productApplePhoneId, bestSellersRes.getData().getContent().get(0).getId(), "Best seller #1 should be iPhone");
        assertEquals(productSonyHeadphonesId, bestSellersRes.getData().getContent().get(1).getId(), "Best seller #2 should be Sony");

        // 2. View Sony 5 times, iPhone 1 time
        for (int i = 0; i < 5; i++) {
            productService.getProductById(productSonyHeadphonesId);
        }
        productService.getProductById(productApplePhoneId);

        ProductSearchRequestDto mostViewedReq = ProductSearchRequestDto.builder()
                .sortBy(ProductSortBy.MOST_VIEWED)
                .build();
        ResponseDto<PaginationPayload<ProductResponseDto>> mostViewedRes = productService.searchProducts(mostViewedReq, null);
        assertEquals(productSonyHeadphonesId, mostViewedRes.getData().getContent().get(0).getId(), "Most viewed should be Sony");

        // 3. Price ascending
        ProductSearchRequestDto priceAscReq = ProductSearchRequestDto.builder()
                .sortBy(ProductSortBy.PRICE_ASC)
                .build();
        ResponseDto<PaginationPayload<ProductResponseDto>> priceAscRes = productService.searchProducts(priceAscReq, null);
        assertEquals(productSonyHeadphonesId, priceAscRes.getData().getContent().get(0).getId(), "Lowest price is Sony ($399.99)");
        assertEquals(productAppleLaptopId, priceAscRes.getData().getContent().get(2).getId(), "Highest price is MacBook ($2499.00)");
    }

    @Test
    @DisplayName("Requirement 3: Uniformity - GET /v1/api/products returns same enriched DTO as search")
    void testCatalogUniformity() {
        ResponseDto<PaginationPayload<ProductResponseDto>> catalogResp = productService.getProducts(null, 0, 10);
        assertNotNull(catalogResp.getData());
        assertFalse(catalogResp.getData().getContent().isEmpty());

        for (ProductResponseDto p : catalogResp.getData().getContent()) {
            assertNotNull(p.getAverageRating());
            assertNotNull(p.getTotalReviews());
            assertNotNull(p.getTotalUnitsSold());
            assertNotNull(p.getViewCount());
            assertNotNull(p.getBuyBoxPrice());
            assertNotNull(p.getBrandName());
            assertNotNull(p.getCategoryName());
        }
    }

    // --- Helper Methods ---

    private void registerUser(String name, String email) {
        authService.register(RegisterRequestDto.builder()
                .fullName(name)
                .email(email)
                .password("Password123!")
                .phone("+1999999999")
                .build());
    }

    private UUID createApprovedBrand(String name, String slug, String ownerEmail) {
        var application = brandApplicationService.submitApplication(CreateBrandApplicationRequestDto.builder()
                .applicantName(name + " Owner").applicantEmail(ownerEmail).password("OwnerPassword123!")
                .applicantPhone("+1234567890").brandName(name).brandSlug(slug)
                .trademarkRegistrationNumber("TM-" + slug).brandCountry("US").build());
        return brandApplicationService.approveApplication(application.getData().getId()).getData().getId();
    }

    private void activateProduct(UUID productId, BigDecimal basePrice, String mainImageUrl) {
        Product product = productRepository.findById(productId).orElseThrow();
        product.setStatus(ProductStatus.ACTIVE);
        product.setBasePrice(basePrice);
        product.setMainImageUrl(mainImageUrl);
        productRepository.save(product);
    }

    private void createDeliveredOrderItem(UUID productId, int quantity) {
        Product product = productRepository.findWithDetailsById(productId).orElseThrow();
        ProductVariant variant;
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            variant = product.getVariants().get(0);
        } else {
            variant = ProductVariant.builder()
                    .product(product)
                    .asin("ASIN-" + UUID.randomUUID().toString().substring(0, 10))
                    .variantName("Default Variant")
                    .build();
            variant = productVariantRepository.save(variant);
        }
        User customer = userRepository.findByEmail(customerEmail).orElseThrow();
        User seller = product.getSeller() != null ? product.getSeller() : customer;

        Order order = Order.builder()
                .user(customer)
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8))
                .status(OrderStatus.DELIVERED)
                .shippingAddressId(UUID.randomUUID())
                .totalAmount(product.getBasePrice().multiply(BigDecimal.valueOf(quantity)))
                .build();
        order = orderRepository.save(order);

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .productVariant(variant)
                .seller(seller)
                .unitPrice(product.getBasePrice())
                .quantity(quantity)
                .subtotal(product.getBasePrice().multiply(BigDecimal.valueOf(quantity)))
                .itemStatus(OrderItemStatus.DELIVERED)
                .build();
        orderItemRepository.save(orderItem);

        // Update product denormalized units sold for best seller ranking
        product.setTotalUnitsSold(product.getTotalUnitsSold() + quantity);
        productRepository.save(product);
    }
}
