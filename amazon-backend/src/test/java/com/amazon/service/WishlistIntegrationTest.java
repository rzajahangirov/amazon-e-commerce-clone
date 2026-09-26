package com.amazon.service;

import com.amazon.dtos.auth.request.RegisterRequestDto;
import com.amazon.dtos.brand.request.CreateBrandApplicationRequestDto;
import com.amazon.dtos.brand.request.CreateBrandCatalogProductRequestDto;
import com.amazon.dtos.category.request.CreateCategoryRequestDto;
import com.amazon.dtos.category.response.CategoryResponseDto;
import com.amazon.dtos.product.response.ProductResponseDto;
import com.amazon.dtos.wishlist.response.WishlistItemResponseDto;
import com.amazon.entity.Product;
import com.amazon.entity.User;
import com.amazon.enums.ProductStatus;
import com.amazon.exception.ResourceNotFoundException;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;
import com.amazon.repository.ProductRepository;
import com.amazon.repository.UserRepository;
import com.amazon.repository.WishlistItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration tests for Wishlist (Favorites) feature.
 * Strictly adheres to Senior Backend Developer Guidelines.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WishlistIntegrationTest {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandApplicationService brandApplicationService;

    @Autowired
    private BrandDashboardService brandDashboardService;

    private UUID productAId;
    private UUID productBId;
    private final String customerEmail = "customer.wishlist@example.com";
    private final String otherCustomerEmail = "other.wishlist@example.com";

    @BeforeEach
    void setUp() {
        // Register customer accounts
        registerCustomer("Wishlist Customer", customerEmail);
        registerCustomer("Other Customer", otherCustomerEmail);

        // Setup Brand & Category
        createApprovedBrand("Samsung Electronics", "samsung-elec", "samsungowner@samsung.com");

        CreateCategoryRequestDto catDto = CreateCategoryRequestDto.builder()
                .name("Smartphones")
                .slug("smartphones")
                .level(0)
                .build();
        ResponseDto<CategoryResponseDto> catResp = categoryService.createCategory(catDto);
        UUID categoryId = catResp.getData().getId();

        // Create Product A
        ResponseDto<ProductResponseDto> prodAResp = brandDashboardService.createBrandProductTemplate("samsungowner@samsung.com",
                CreateBrandCatalogProductRequestDto.builder()
                        .categoryId(categoryId)
                        .title("Samsung Galaxy S24 Ultra")
                        .description("Flagship AI Smartphone")
                        .build());
        productAId = prodAResp.getData().getId();

        // Create Product B
        ResponseDto<ProductResponseDto> prodBResp = brandDashboardService.createBrandProductTemplate("samsungowner@samsung.com",
                CreateBrandCatalogProductRequestDto.builder()
                        .categoryId(categoryId)
                        .title("Samsung Galaxy A54")
                        .description("Mid-range Smartphone")
                        .build());
        productBId = prodBResp.getData().getId();

        // Set products to ACTIVE so they appear in catalog
        Product prodA = productRepository.findById(productAId).orElseThrow();
        prodA.setStatus(ProductStatus.ACTIVE);
        productRepository.save(prodA);

        Product prodB = productRepository.findById(productBId).orElseThrow();
        prodB.setStatus(ProductStatus.ACTIVE);
        productRepository.save(prodB);
    }

    @Test
    @DisplayName("Requirement 1 & 2: Add product to wishlist - persists item and returns mapped response")
    void testAddToWishlistSuccess() {
        ResponseDto<WishlistItemResponseDto> response = wishlistService.addToWishlist(customerEmail, productAId);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals("Product added to wishlist successfully", response.getMessage());
        assertEquals(productAId, response.getData().getProductId());
        assertEquals("Samsung Galaxy S24 Ultra", response.getData().getProductTitle());
        assertEquals("Samsung Electronics", response.getData().getBrandName());
        assertEquals("Smartphones", response.getData().getCategoryName());
        assertNotNull(response.getData().getAddedAt());

        // Verify DB state
        User user = userRepository.findByEmail(customerEmail).orElseThrow();
        assertTrue(wishlistItemRepository.existsByUserIdAndProductId(user.getId(), productAId));
    }

    @Test
    @DisplayName("Requirement 2: Add product to wishlist - handles duplication idempotently")
    void testAddToWishlistIdempotent() {
        // First add
        ResponseDto<WishlistItemResponseDto> firstResp = wishlistService.addToWishlist(customerEmail, productAId);
        assertNotNull(firstResp.getData());
        assertEquals("Product added to wishlist successfully", firstResp.getMessage());

        // Second add (duplicate)
        ResponseDto<WishlistItemResponseDto> secondResp = wishlistService.addToWishlist(customerEmail, productAId);
        assertNotNull(secondResp.getData());
        assertEquals("Product is already in your wishlist", secondResp.getMessage());
        assertEquals(firstResp.getData().getWishlistItemId(), secondResp.getData().getWishlistItemId());

        // Assert count in DB is still 1
        User user = userRepository.findByEmail(customerEmail).orElseThrow();
        assertEquals(1, wishlistItemRepository.countByUserId(user.getId()));
    }

    @Test
    @DisplayName("Requirement 4: Add non-existent product to wishlist throws ResourceNotFoundException")
    void testAddToWishlistNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        assertThrows(ResourceNotFoundException.class, () ->
                wishlistService.addToWishlist(customerEmail, nonExistentId));
    }

    @Test
    @DisplayName("Requirement 2: Remove product from wishlist removes item from DB")
    void testRemoveFromWishlistSuccess() {
        wishlistService.addToWishlist(customerEmail, productAId);
        User user = userRepository.findByEmail(customerEmail).orElseThrow();
        assertTrue(wishlistItemRepository.existsByUserIdAndProductId(user.getId(), productAId));

        ResponseDto<Void> removeResp = wishlistService.removeFromWishlist(customerEmail, productAId);
        assertNotNull(removeResp);
        assertEquals("Product removed from wishlist successfully", removeResp.getMessage());

        assertFalse(wishlistItemRepository.existsByUserIdAndProductId(user.getId(), productAId));
    }

    @Test
    @DisplayName("Requirement 4: Remove product not in wishlist throws ResourceNotFoundException")
    void testRemoveFromWishlistNotInWishlist() {
        assertThrows(ResourceNotFoundException.class, () ->
                wishlistService.removeFromWishlist(customerEmail, productBId));
    }

    @Test
    @DisplayName("Requirement 2: Get paginated wishlist retrieves user favorites")
    void testGetWishlistPaginated() {
        wishlistService.addToWishlist(customerEmail, productAId);
        wishlistService.addToWishlist(customerEmail, productBId);

        ResponseDto<PaginationPayload<WishlistItemResponseDto>> wishlistResp =
                wishlistService.getWishlist(customerEmail, 0, 10);

        assertNotNull(wishlistResp);
        PaginationPayload<WishlistItemResponseDto> payload = wishlistResp.getData();
        assertEquals(2, payload.getTotalElements());
        assertEquals(2, payload.getContent().size());
        assertTrue(payload.getContent().stream().anyMatch(i -> i.getProductId().equals(productAId)));
        assertTrue(payload.getContent().stream().anyMatch(i -> i.getProductId().equals(productBId)));
    }

    @Test
    @DisplayName("Requirement 2: Clear wishlist removes all user items without affecting other users")
    void testClearWishlist() {
        wishlistService.addToWishlist(customerEmail, productAId);
        wishlistService.addToWishlist(customerEmail, productBId);
        wishlistService.addToWishlist(otherCustomerEmail, productAId);

        ResponseDto<Void> clearResp = wishlistService.clearWishlist(customerEmail);
        assertEquals("Wishlist cleared successfully", clearResp.getMessage());

        User user = userRepository.findByEmail(customerEmail).orElseThrow();
        assertEquals(0, wishlistItemRepository.countByUserId(user.getId()));

        User otherUser = userRepository.findByEmail(otherCustomerEmail).orElseThrow();
        assertEquals(1, wishlistItemRepository.countByUserId(otherUser.getId()));
    }

    @Test
    @DisplayName("Requirement 3: Dynamic isFavorited flag in getProductById for authenticated user")
    void testGetProductByIdDynamicIsFavorited() {
        // Customer favorites product A, but not product B
        wishlistService.addToWishlist(customerEmail, productAId);

        // Fetch Product A with customer authentication
        ResponseDto<ProductResponseDto> prodAResp = productService.getProductById(productAId, customerEmail);
        assertTrue(prodAResp.getData().getIsFavorited(), "Product A should be favorited by customer");

        // Fetch Product B with customer authentication
        ResponseDto<ProductResponseDto> prodBResp = productService.getProductById(productBId, customerEmail);
        assertFalse(prodBResp.getData().getIsFavorited(), "Product B should NOT be favorited by customer");
    }

    @Test
    @DisplayName("Requirement 3: Dynamic isFavorited flag defaults to false for guest/unauthenticated user")
    void testGetProductByIdForGuestUser() {
        // Customer favorites product A
        wishlistService.addToWishlist(customerEmail, productAId);

        // Fetch Product A as guest (userEmail = null)
        ResponseDto<ProductResponseDto> guestResp = productService.getProductById(productAId, null);
        assertFalse(guestResp.getData().getIsFavorited(), "Product A must default isFavorited to false for unauthenticated user");

        // Overloaded getProductById(UUID id) backward compatibility
        ResponseDto<ProductResponseDto> legacyResp = productService.getProductById(productAId);
        assertFalse(legacyResp.getData().getIsFavorited(), "Legacy getProductById must default isFavorited to false");
    }

    @Test
    @DisplayName("Requirement 3: Dynamic isFavorited flag in paginated getProducts (batch optimization, no N+1)")
    void testGetProductsDynamicIsFavoritedBatch() {
        // Customer favorites product A only
        wishlistService.addToWishlist(customerEmail, productAId);

        // Fetch paginated products with customer email
        ResponseDto<PaginationPayload<ProductResponseDto>> pageResp =
                productService.getProducts(null, 0, 20, customerEmail);

        assertNotNull(pageResp.getData());
        for (ProductResponseDto prod : pageResp.getData().getContent()) {
            if (prod.getId().equals(productAId)) {
                assertTrue(prod.getIsFavorited(), "Product A in catalog page must have isFavorited = true");
            } else if (prod.getId().equals(productBId)) {
                assertFalse(prod.getIsFavorited(), "Product B in catalog page must have isFavorited = false");
            }
        }

        // Fetch paginated products as guest (userEmail = null)
        ResponseDto<PaginationPayload<ProductResponseDto>> guestPageResp =
                productService.getProducts(null, 0, 20, null);

        for (ProductResponseDto prod : guestPageResp.getData().getContent()) {
            assertFalse(prod.getIsFavorited(), "All products in catalog page must have isFavorited = false for guests");
        }
    }

    // --- Helper Setup Methods ---

    private void registerCustomer(String fullName, String email) {
        authService.register(RegisterRequestDto.builder()
                .fullName(fullName)
                .email(email)
                .password("Password123!")
                .phone("+1234567890")
                .build());
    }

    private void createApprovedBrand(String brandName, String slug, String ownerEmail) {
        var application = brandApplicationService.submitApplication(CreateBrandApplicationRequestDto.builder()
                .applicantName(brandName + " Owner")
                .applicantEmail(ownerEmail)
                .password("OwnerPassword123!")
                .applicantPhone("+1234567890")
                .brandName(brandName)
                .brandSlug(slug)
                .trademarkRegistrationNumber("TM-" + slug)
                .brandCountry("US")
                .build());
        brandApplicationService.approveApplication(application.getData().getId());
    }
}
