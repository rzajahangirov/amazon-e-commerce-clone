package com.amazon.service;

import com.amazon.dtos.auth.request.RegisterRequestDto;
import com.amazon.dtos.brand.request.CreateBrandApplicationRequestDto;
import com.amazon.dtos.brand.request.CreateBrandCatalogProductRequestDto;
import com.amazon.dtos.brand.response.BrandResponseDto;
import com.amazon.dtos.category.request.CreateCategoryRequestDto;
import com.amazon.dtos.category.response.CategoryResponseDto;
import com.amazon.dtos.product.response.ProductResponseDto;
import com.amazon.dtos.review.request.CreateReviewRequestDto;
import com.amazon.dtos.review.request.UpdateReviewRequestDto;
import com.amazon.dtos.review.response.ReviewResponseDto;
import com.amazon.entity.Product;
import com.amazon.entity.Role;
import com.amazon.entity.User;
import com.amazon.enums.ProductStatus;
import com.amazon.exception.DuplicateResourceException;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;
import com.amazon.repository.ProductRepository;
import com.amazon.repository.RoleRepository;
import com.amazon.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductReviewIntegrationTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandApplicationService brandApplicationService;

    @Autowired
    private BrandDashboardService brandDashboardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private UUID activeProductId;
    private final String reviewer1Email = "reviewer1@example.com";
    private final String reviewer2Email = "reviewer2@example.com";
    private final String reviewer3Email = "reviewer3@example.com";
    private final String adminEmail = "platformadmin@example.com";

    @BeforeEach
    void setup() {
        // Register customer users
        registerCustomer("Reviewer One", reviewer1Email);
        registerCustomer("Reviewer Two", reviewer2Email);
        registerCustomer("Reviewer Three", reviewer3Email);
        registerCustomer("Admin User", adminEmail);

        // Assign ROLE_ADMIN to admin user
        User adminUser = userRepository.findByEmail(adminEmail).orElseThrow();
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").description("Administrator").build()));
        adminUser.addRole(adminRole);
        userRepository.save(adminUser);

        // Setup Brand & Category
        createApprovedBrand("Sony Tech", "sony-tech", "sonyowner@sony.com");
        CreateCategoryRequestDto catDto = CreateCategoryRequestDto.builder()
                .name("Audio & Headphones")
                .slug("audio-headphones")
                .level(0)
                .build();
        ResponseDto<CategoryResponseDto> catResp = categoryService.createCategory(catDto);
        UUID categoryId = catResp.getData().getId();

        // Create Product
        ResponseDto<ProductResponseDto> prodResp = brandDashboardService.createBrandProductTemplate("sonyowner@sony.com",
                CreateBrandCatalogProductRequestDto.builder()
                        .categoryId(categoryId)
                        .title("Sony WH-1000XM5 Wireless Headphones")
                        .description("Industry-leading noise cancelling headphones")
                        .build());
        activeProductId = prodResp.getData().getId();

        // Ensure product is ACTIVE so it can be reviewed and retrieved by public catalog
        Product product = productRepository.findById(activeProductId).orElseThrow();
        product.setStatus(ProductStatus.ACTIVE);
        productRepository.save(product);
    }

    @Test
    @DisplayName("Requirement 3: New/Unrated Product Handling & Null Safety - returns 0.0 rating and 0 total reviews")
    void testNewUnratedProductNullSafety() {
        // 1. Fetch unrated product details via ProductService
        ResponseDto<ProductResponseDto> response = productService.getProductById(activeProductId);
        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals("Product retrieved successfully", response.getMessage());
        assertEquals(0.0, response.getData().getAverageRating());
        assertEquals(0, response.getData().getTotalReviews());

        // 2. Fetch reviews for unrated product
        ResponseDto<PaginationPayload<ReviewResponseDto>> reviewsResp =
                reviewService.getProductReviews(activeProductId, 0, 10);
        assertNotNull(reviewsResp);
        assertNotNull(reviewsResp.getData());
        assertEquals("Reviews retrieved successfully", reviewsResp.getMessage());
        assertEquals(0, reviewsResp.getData().getTotalElements());
        assertTrue(reviewsResp.getData().getContent().isEmpty());
    }

    @Test
    @DisplayName("Requirements 1 & 4: Submit reviews and dynamically recalculate average rating & total reviews")
    void testCreateReviewsAndDynamicRecalculation() {
        // 1. First review: 5 stars
        CreateReviewRequestDto review1 = CreateReviewRequestDto.builder()
                .rating(5)
                .comment("Incredible sound quality and ANC!")
                .build();
        ResponseDto<ReviewResponseDto> resp1 = reviewService.createReview(reviewer1Email, activeProductId, review1);
        assertNotNull(resp1.getData());
        assertEquals(5, resp1.getData().getRating());
        assertEquals("Incredible sound quality and ANC!", resp1.getData().getComment());

        // Verify product aggregation updated
        ResponseDto<ProductResponseDto> prodAfterRev1 = productService.getProductById(activeProductId);
        assertEquals(5.0, prodAfterRev1.getData().getAverageRating());
        assertEquals(1, prodAfterRev1.getData().getTotalReviews());

        // 2. Second review: 4 stars
        CreateReviewRequestDto review2 = CreateReviewRequestDto.builder()
                .rating(4)
                .comment("Great headphones, but earcups get a bit warm.")
                .build();
        reviewService.createReview(reviewer2Email, activeProductId, review2);

        // Verify product aggregation updated: (5 + 4) / 2 = 4.5
        ResponseDto<ProductResponseDto> prodAfterRev2 = productService.getProductById(activeProductId);
        assertEquals(4.5, prodAfterRev2.getData().getAverageRating());
        assertEquals(2, prodAfterRev2.getData().getTotalReviews());

        // 3. Third review: 3 stars
        CreateReviewRequestDto review3 = CreateReviewRequestDto.builder()
                .rating(3)
                .comment("Good sound, but price is quite steep.")
                .build();
        reviewService.createReview(reviewer3Email, activeProductId, review3);

        // Verify product aggregation updated: (5 + 4 + 3) / 3 = 4.0
        ResponseDto<ProductResponseDto> prodAfterRev3 = productService.getProductById(activeProductId);
        assertEquals(4.0, prodAfterRev3.getData().getAverageRating());
        assertEquals(3, prodAfterRev3.getData().getTotalReviews());

        // 4. Verify paginated reviews query
        ResponseDto<PaginationPayload<ReviewResponseDto>> reviewsPage =
                reviewService.getProductReviews(activeProductId, 0, 10);
        assertEquals(3, reviewsPage.getData().getTotalElements());
        assertEquals(3, reviewsPage.getData().getContent().size());
    }

    @Test
    @DisplayName("Requirement 4: Prevent duplicate reviews per user per product")
    void testDuplicateReviewPrevention() {
        CreateReviewRequestDto review = CreateReviewRequestDto.builder()
                .rating(5)
                .comment("Top tier audio!")
                .build();
        reviewService.createReview(reviewer1Email, activeProductId, review);

        // Attempt second review from same user on same product
        CreateReviewRequestDto duplicate = CreateReviewRequestDto.builder()
                .rating(4)
                .comment("Trying to review again")
                .build();

        assertThrows(DuplicateResourceException.class, () ->
                reviewService.createReview(reviewer1Email, activeProductId, duplicate));
    }

    @Test
    @DisplayName("Requirements 1 & 4: Update review by author and dynamically recalculate ratings")
    void testUpdateReviewAndAuthorization() {
        // Create review (5 stars)
        CreateReviewRequestDto initial = CreateReviewRequestDto.builder()
                .rating(5)
                .comment("Initial review")
                .build();
        ResponseDto<ReviewResponseDto> created = reviewService.createReview(reviewer1Email, activeProductId, initial);
        UUID reviewId = created.getData().getId();

        // Non-author tries to update -> AccessDeniedException
        UpdateReviewRequestDto unauthorizedUpdate = UpdateReviewRequestDto.builder()
                .rating(1)
                .comment("Hacked review")
                .build();
        assertThrows(AccessDeniedException.class, () ->
                reviewService.updateReview(reviewer2Email, activeProductId, reviewId, unauthorizedUpdate));

        // Author updates rating from 5 to 2
        UpdateReviewRequestDto authorUpdate = UpdateReviewRequestDto.builder()
                .rating(2)
                .comment("Broke after two weeks, lowering rating.")
                .build();
        ResponseDto<ReviewResponseDto> updated = reviewService.updateReview(reviewer1Email, activeProductId, reviewId, authorUpdate);
        assertEquals(2, updated.getData().getRating());
        assertEquals("Broke after two weeks, lowering rating.", updated.getData().getComment());

        // Verify product aggregation updated dynamically to 2.0
        ResponseDto<ProductResponseDto> prod = productService.getProductById(activeProductId);
        assertEquals(2.0, prod.getData().getAverageRating());
        assertEquals(1, prod.getData().getTotalReviews());
    }

    @Test
    @DisplayName("Requirements 1 & 4: Delete review by author or ADMIN with dynamic recalculation back to zero")
    void testDeleteReviewAndRecalculation() {
        // Reviewer 1 creates review
        ResponseDto<ReviewResponseDto> rev1 = reviewService.createReview(reviewer1Email, activeProductId,
                CreateReviewRequestDto.builder().rating(5).comment("Best ever").build());
        // Reviewer 2 creates review
        ResponseDto<ReviewResponseDto> rev2 = reviewService.createReview(reviewer2Email, activeProductId,
                CreateReviewRequestDto.builder().rating(3).comment("Average").build());

        // Average: (5 + 3) / 2 = 4.0
        assertEquals(4.0, productService.getProductById(activeProductId).getData().getAverageRating());
        assertEquals(2, productService.getProductById(activeProductId).getData().getTotalReviews());

        // Unauthorized user attempts deletion
        assertThrows(AccessDeniedException.class, () ->
                reviewService.deleteReview(reviewer3Email, activeProductId, rev1.getData().getId()));

        // Author deletes their own review (rev1)
        reviewService.deleteReview(reviewer1Email, activeProductId, rev1.getData().getId());

        // Verify remaining review (rev2: 3 stars)
        ProductResponseDto prodAfterFirstDelete = productService.getProductById(activeProductId).getData();
        assertEquals(3.0, prodAfterFirstDelete.getAverageRating());
        assertEquals(1, prodAfterFirstDelete.getTotalReviews());

        // ADMIN deletes rev2
        reviewService.deleteReview(adminEmail, activeProductId, rev2.getData().getId());

        // Verify product returns safely to 0.0 and 0 without any NPE
        ProductResponseDto prodAfterAllDeleted = productService.getProductById(activeProductId).getData();
        assertEquals(0.0, prodAfterAllDeleted.getAverageRating());
        assertEquals(0, prodAfterAllDeleted.getTotalReviews());
    }

    private void registerCustomer(String name, String email) {
        authService.register(RegisterRequestDto.builder()
                .fullName(name)
                .email(email)
                .password("Password123!")
                .phone("+1999999999")
                .build());
    }

    private BrandResponseDto createApprovedBrand(String name, String slug, String ownerEmail) {
        var application = brandApplicationService.submitApplication(CreateBrandApplicationRequestDto.builder()
                .applicantName(name + " Owner").applicantEmail(ownerEmail).password("OwnerPassword123!")
                .applicantPhone("+1234567890").brandName(name).brandSlug(slug)
                .trademarkRegistrationNumber("TM-" + slug).brandCountry("US").build());
        return brandApplicationService.approveApplication(application.getData().getId()).getData();
    }
}
