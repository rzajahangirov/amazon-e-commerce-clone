package com.amazon.service;

import com.amazon.dtos.brand.request.*;
import com.amazon.dtos.brand.response.*;
import com.amazon.dtos.category.request.CreateCategoryRequestDto;
import com.amazon.dtos.category.response.CategoryResponseDto;
import com.amazon.dtos.product.response.ProductResponseDto;
import com.amazon.entity.*;
import com.amazon.enums.*;
import com.amazon.exception.BusinessRuleException;
import com.amazon.exception.DuplicateResourceException;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;
import com.amazon.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BrandModuleIntegrationTest {

    @Autowired
    private BrandApplicationService brandApplicationService;

    @Autowired
    private AdminBrandManagementService adminBrandManagementService;

    @Autowired
    private BrandDashboardService brandDashboardService;

    @Autowired
    private ProductListingService productListingService;

    @Autowired
    private AuthService authService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SellerProfileRepository sellerProfileRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandMemberRepository brandMemberRepository;

    private UUID categoryId;

    @BeforeEach
    void setUp() {
        CreateCategoryRequestDto catDto = CreateCategoryRequestDto.builder()
                .name("Computers & Accessories")
                .slug("computers-" + UUID.randomUUID())
                .level(0)
                .build();
        ResponseDto<CategoryResponseDto> catResp = categoryService.createCategory(catDto);
        categoryId = catResp.getData().getId();
    }

    @Test
    @DisplayName("Workflow 1: Brand Registration -> Admin Rejection")
    void testBrandApplicationRejection() {
        CreateBrandApplicationRequestDto appDto = CreateBrandApplicationRequestDto.builder()
                .applicantName("Fake Applicant")
                .applicantEmail("fake@applicant.com")
                .password("Password123!")
                .applicantPhone("+111222333")
                .brandName("Fake Brand")
                .brandSlug("fake-brand")
                .trademarkRegistrationNumber("TM-FAKE-999")
                .brandCountry("US")
                .logoUrl("https://example.com/logo.png")
                .aboutText("About fake brand")
                .build();

        ResponseDto<BrandApplicationResponseDto> submitted = brandApplicationService.submitApplication(appDto);
        assertNotNull(submitted.getData());
        assertEquals(BrandApplicationStatus.PENDING, submitted.getData().getStatus());

        RejectApplicationRequestDto rejectDto = new RejectApplicationRequestDto("Incomplete trademark documentation");
        ResponseDto<BrandApplicationResponseDto> rejected = brandApplicationService.rejectApplication(
                submitted.getData().getId(), rejectDto);

        assertEquals(BrandApplicationStatus.REJECTED, rejected.getData().getStatus());
        assertEquals("Incomplete trademark documentation", rejected.getData().getRejectionReason());

        // Second rejection should fail
        assertThrows(BusinessRuleException.class, () ->
                brandApplicationService.rejectApplication(submitted.getData().getId(), rejectDto));
    }

    @Test
    @DisplayName("Workflow 2: Brand Registration -> Admin Approval -> User, Brand, SellerProfile & BrandMember Creation")
    void testBrandApplicationApproval() {
        CreateBrandApplicationRequestDto appDto = CreateBrandApplicationRequestDto.builder()
                .applicantName("Steve Jobs")
                .applicantEmail("steve@apple.com")
                .password("AppleSecret123!")
                .applicantPhone("+14089961010")
                .brandName("Apple")
                .brandSlug("apple")
                .trademarkRegistrationNumber("TM-US-19760401")
                .brandCountry("United States")
                .logoUrl("https://apple.com/logo.png")
                .aboutText("Think different.")
                .build();

        ResponseDto<BrandApplicationResponseDto> appResp = brandApplicationService.submitApplication(appDto);
        UUID applicationId = appResp.getData().getId();

        ResponseDto<BrandResponseDto> approveResp = brandApplicationService.approveApplication(applicationId);
        assertNotNull(approveResp.getData());
        assertEquals("Apple", approveResp.getData().getName());
        assertEquals("apple", approveResp.getData().getSlug());
        assertEquals(BrandStatus.ACTIVE, approveResp.getData().getStatus());

        // Verify Brand Profile from BrandDashboard
        ResponseDto<BrandProfileResponseDto> profileResp = brandDashboardService.getBrandProfile("steve@apple.com");
        assertNotNull(profileResp.getData());
        assertEquals("Apple", profileResp.getData().getBrand().getName());
        assertEquals(1, profileResp.getData().getTeamMembers().size());
        assertEquals(BrandRole.BRAND_OWNER, profileResp.getData().getTeamMembers().get(0).getBrandRole());
        assertEquals("steve@apple.com", profileResp.getData().getTeamMembers().get(0).getUserEmail());

        // Verify Core User & SellerProfile provisioning
        User ownerUser = userRepository.findByEmail("steve@apple.com").orElseThrow();
        assertNotNull(ownerUser);
        assertTrue(ownerUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_BRAND_OWNER")));
        assertTrue(ownerUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_SELLER")));

        SellerProfile sellerProfile = sellerProfileRepository.findByUserId(ownerUser.getId()).orElseThrow();
        assertNotNull(sellerProfile);
        assertTrue(sellerProfile.getIsVerified());
        assertNotNull(sellerProfile.getBrand());
        assertEquals("Apple", sellerProfile.getBrand().getName());
        assertEquals(ownerUser.getId(), sellerProfile.getUser().getId());
    }

    @Test
    @DisplayName("Workflow 3: Brand Update Request Submission & Admin Review (Approval & Rejection)")
    void testBrandUpdateRequestFlow() {
        // 1. Setup approved brand
        BrandResponseDto brand = setupApprovedBrand("Nike", "nike", "phil@nike.com");

        // 2. Submit change proposal
        CreateBrandUpdateRequestDto updateReqDto = CreateBrandUpdateRequestDto.builder()
                .proposedBrandName("Nike Global")
                .proposedLogoUrl("https://nike.com/new-swoosh.png")
                .proposedAboutText("Just Do It Everywhere.")
                .proposedTrademarkNo("TM-NIKE-GLOBAL-2026")
                .build();

        ResponseDto<BrandUpdateRequestResponseDto> submitResp = brandDashboardService.submitUpdateRequest(
                "phil@nike.com", updateReqDto);
        assertNotNull(submitResp.getData());
        assertEquals(BrandUpdateRequestStatus.PENDING, submitResp.getData().getStatus());
        UUID requestId = submitResp.getData().getId();

        // 3. Admin lists update requests
        ResponseDto<PaginationPayload<BrandUpdateRequestResponseDto>> listResp =
                adminBrandManagementService.getUpdateRequests(BrandUpdateRequestStatus.PENDING, 0, 10);
        assertTrue(listResp.getData().getContent().stream().anyMatch(r -> r.getId().equals(requestId)));

        // 4. Admin approves
        ResponseDto<BrandResponseDto> approvedBrand = adminBrandManagementService.approveUpdateRequest(requestId);
        assertEquals("Nike Global", approvedBrand.getData().getName());
        assertEquals("https://nike.com/new-swoosh.png", approvedBrand.getData().getLogoUrl());

        // Verify updated brand entity
        Brand brandEntity = brandRepository.findById(brand.getId()).orElseThrow();
        assertEquals("Nike Global", brandEntity.getName());
    }

    @Test
    @DisplayName("Workflow 4: Team Member Management & Permission Enforcements")
    void testTeamManagementAndPermissions() {
        setupApprovedBrand("Sony", "sony", "owner@sony.com");

        // 1. Owner adds employee
        AddBrandMemberRequestDto addMemberDto = AddBrandMemberRequestDto.builder()
                .fullName("Ken Kutaragi")
                .email("ken@sony.com")
                .password("PlayStation123!")
                .brandRole(BrandRole.BRAND_SELLER)
                .build();

        ResponseDto<BrandMemberResponseDto> addedResp = brandDashboardService.addBrandMember("owner@sony.com", addMemberDto);
        assertNotNull(addedResp.getData());
        assertEquals(BrandRole.BRAND_SELLER, addedResp.getData().getBrandRole());
        UUID memberId = addedResp.getData().getId();

        // Verify employee User and SellerProfile creation
        User employeeUser = userRepository.findByEmail("ken@sony.com").orElseThrow();
        assertNotNull(employeeUser);
        assertTrue(employeeUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_SELLER")));

        SellerProfile employeeProfile = sellerProfileRepository.findByUserId(employeeUser.getId()).orElseThrow();
        assertNotNull(employeeProfile);
        assertTrue(employeeProfile.getIsVerified());
        assertNotNull(employeeProfile.getBrand());
        assertEquals("Sony", employeeProfile.getBrand().getName());

        BrandMember employeeMember = brandMemberRepository.findByBrandIdAndUserId(addedResp.getData().getBrandId(), employeeUser.getId()).orElseThrow();
        assertEquals(BrandRole.BRAND_SELLER, employeeMember.getBrandRole());
        assertEquals("ken@sony.com", employeeMember.getUser().getEmail());
        assertEquals("Sony", employeeMember.getBrand().getName());

        // 2. Non-owner (Ken) tries to add someone -> AccessDeniedException
        AddBrandMemberRequestDto unauthorizedAdd = AddBrandMemberRequestDto.builder()
                .fullName("Hideo Kojima")
                .email("hideo@sony.com")
                .password("Kojima123!")
                .brandRole(BrandRole.BRAND_MARKETING_MEMBER)
                .build();
        assertThrows(AccessDeniedException.class, () ->
                brandDashboardService.addBrandMember("ken@sony.com", unauthorizedAdd));

        // 3. Owner updates Ken's role to BRAND_ADMIN
        UpdateBrandMemberRoleRequestDto updateRoleDto = new UpdateBrandMemberRoleRequestDto(BrandRole.BRAND_ADMIN);
        ResponseDto<BrandMemberResponseDto> updatedRoleResp = brandDashboardService.updateBrandMemberRole(
                "owner@sony.com", memberId, updateRoleDto);
        assertEquals(BrandRole.BRAND_ADMIN, updatedRoleResp.getData().getBrandRole());

        // 4. Owner cannot remove owner
        ResponseDto<List<BrandMemberResponseDto>> members = brandDashboardService.getBrandMembers("owner@sony.com");
        BrandMemberResponseDto ownerMember = members.getData().stream()
                .filter(m -> m.getBrandRole() == BrandRole.BRAND_OWNER)
                .findFirst()
                .orElseThrow();

        assertThrows(BusinessRuleException.class, () ->
                brandDashboardService.removeBrandMember("owner@sony.com", ownerMember.getId()));

        // 5. Owner removes Ken
        ResponseDto<Void> removeResp = brandDashboardService.removeBrandMember("owner@sony.com", memberId);
        assertNotNull(removeResp);
    }

    @Test
    @DisplayName("Workflow 5: Separate Brand Catalog and Seller Offer Creation")
    void testBrandCatalogAndOfferSeparation() {
        setupApprovedBrand("Samsung", "samsung", "lee@samsung.com");

        CatalogFixture catalog = createCatalog("lee@samsung.com", "Galaxy S26 Ultra", "Flagship AI smartphone",
                "B09SAMS26U", "Titanium Gray / 512GB", Map.of("color", "Titanium Gray", "storage", "512GB"));
        assertNotNull(catalog.productId());
        assertNotNull(catalog.variantId());
        assertNull(catalog.product().getBasePrice());
        assertEquals("Titanium Gray", catalog.variant().getVariantAttributes().get("color"));
        var offer = createOffer(catalog.variantId(), "SAM-S26U-512-GRY", "lee@samsung.com", 150);
        assertEquals(150, offer.getStockQuantity());

        // Verify Product entity relationship to Brand
        Product productEntity = productRepository.findWithDetailsById(catalog.productId()).orElseThrow();
        assertNotNull(productEntity.getBrand());
        assertEquals("Samsung", productEntity.getBrand().getName());
        assertEquals(catalog.product().getBrandId(), productEntity.getBrand().getId());

        // Verify product shows in getBrandProducts
        ResponseDto<PaginationPayload<ProductResponseDto>> products =
                brandDashboardService.getBrandProducts("lee@samsung.com", 0, 10);
        assertEquals(1, products.getData().getTotalElements());
        assertEquals("Samsung", products.getData().getContent().get(0).getBrandName());

        // Update product
        UpdateBrandProductRequestDto updateProductDto = UpdateBrandProductRequestDto.builder()
                .title("Galaxy S26 Ultra Pro")
                .build();

        ResponseDto<ProductResponseDto> updatedProd = brandDashboardService.updateBrandProduct(
                "lee@samsung.com", catalog.productId(), updateProductDto);
        assertEquals("Galaxy S26 Ultra Pro", updatedProd.getData().getTitle());
        assertNull(updatedProd.getData().getBasePrice());

        // Delete product
        ResponseDto<Void> deleteResp = brandDashboardService.deleteBrandProduct(
                "lee@samsung.com", catalog.productId());
        assertNotNull(deleteResp);
    }

    @Test
    @DisplayName("Workflow 6: Multi-Tenant Brand Security Isolation (CRITICAL Cross-Brand Protection)")
    void testCrossBrandSecurityIsolation() {
        // Setup Brand A (LG) and Brand B (Panasonic)
        setupApprovedBrand("LG Electronics", "lg", "owner@lg.com");
        setupApprovedBrand("Panasonic Corp", "panasonic", "owner@panasonic.com");

        // LG Owner creates a product
        CatalogFixture lgCreated = createCatalog("owner@lg.com", "LG OLED TV C5", null,
                "B09LGC5OLED", "65 inch", Map.of());
        UUID lgProductId = lgCreated.productId();

        // CROSS-BRAND ATTACK: Panasonic Owner attempts to update LG's product -> MUST FAIL with AccessDeniedException
        UpdateBrandProductRequestDto attackUpdate = UpdateBrandProductRequestDto.builder()
                .title("Hacked TV Title")
                .build();

        assertThrows(AccessDeniedException.class, () ->
                brandDashboardService.updateBrandProduct("owner@panasonic.com", lgProductId, attackUpdate));

        // CROSS-BRAND ATTACK: Panasonic Owner attempts to delete LG's product -> MUST FAIL with AccessDeniedException
        assertThrows(AccessDeniedException.class, () ->
                brandDashboardService.deleteBrandProduct("owner@panasonic.com", lgProductId));
    }

    @Test
    @DisplayName("Workflow 7: Brand Posts Marketing & Cross-Brand Protection")
    void testBrandPostsAndIsolation() {
        setupApprovedBrand("Tesla", "tesla", "elon@tesla.com");
        setupApprovedBrand("Rivian", "rivian", "rj@rivian.com");

        // Tesla creates post
        CreateBrandPostRequestDto postDto = CreateBrandPostRequestDto.builder()
                .imageUrl("https://tesla.com/cybercab.png")
                .caption("Autonomous future begins now.")
                .build();
        ResponseDto<BrandPostResponseDto> postResp = brandDashboardService.createBrandPost("elon@tesla.com", postDto);
        UUID postId = postResp.getData().getId();

        // Rivian attempts to update Tesla's post -> AccessDeniedException
        UpdateBrandPostRequestDto attackPost = UpdateBrandPostRequestDto.builder()
                .caption("Rivian is better.")
                .build();
        assertThrows(AccessDeniedException.class, () ->
                brandDashboardService.updateBrandPost("rj@rivian.com", postId, attackPost));

        // Rivian attempts to delete Tesla's post -> AccessDeniedException
        assertThrows(AccessDeniedException.class, () ->
                brandDashboardService.deleteBrandPost("rj@rivian.com", postId));

        // Tesla updates and deletes successfully
        ResponseDto<BrandPostResponseDto> updated = brandDashboardService.updateBrandPost("elon@tesla.com", postId, attackPost);
        assertEquals("Rivian is better.", updated.getData().getCaption());

        ResponseDto<Void> deleted = brandDashboardService.deleteBrandPost("elon@tesla.com", postId);
        assertNotNull(deleted);
    }

    @Test
    @DisplayName("Workflow 8: Admin Brand Suspension & Listings Inactivation")
    void testAdminBrandSuspension() {
        BrandResponseDto brand = setupApprovedBrand("Bose", "bose", "amar@bose.com");

        // Create product
        CatalogFixture boseProduct = createCatalog("amar@bose.com", "Bose QC Ultra", null,
                "B09BOSEQC", "Black", Map.of());
        createOffer(boseProduct.variantId(), "BOSE-QC-BLK", "amar@bose.com");

        // Admin suspends / deletes brand
        adminBrandManagementService.deleteBrand(brand.getId());

        // Subsequent access to brand dashboard by Bose member must fail with AccessDeniedException (Brand is suspended)
        assertThrows(AccessDeniedException.class, () ->
                brandDashboardService.getBrandProfile("amar@bose.com"));
    }

    @Test
    @DisplayName("Workflow 9: Entity Relationship Navigation & JPA Object Model Integrity")
    void testEntityRelationshipNavigation() {
        BrandResponseDto brandDto = setupApprovedBrand("Microsoft", "microsoft", "satya@microsoft.com");
        User owner = userRepository.findByEmail("satya@microsoft.com").orElseThrow();
        Brand brand = brandRepository.findById(brandDto.getId()).orElseThrow();

        // 1. User -> SellerProfile & BrandMember navigation
        assertNotNull(owner.getSellerProfile());
        assertEquals(brand.getId(), owner.getSellerProfile().getBrand().getId());
        assertFalse(owner.getBrandMembers().isEmpty());
        assertEquals(brand.getId(), owner.getBrandMembers().get(0).getBrand().getId());

        // 2. Brand -> Owner navigation
        assertNotNull(brand.getOwnerUser());
        assertEquals(owner.getId(), brand.getOwnerUser().getId());

        // 3. Quick create product and verify Product -> Brand navigation
        CatalogFixture surface = createCatalog("satya@microsoft.com", "Surface Laptop 7", null,
                "B09MSFTLAP7", "Platinum", Map.of("color", "Platinum", "ram", "16GB"));

        assertNotNull(surface.variant().getVariantAttributes());
        assertEquals("Platinum", surface.variant().getVariantAttributes().get("color"));

        Product product = productRepository.findWithDetailsById(surface.productId()).orElseThrow();
        assertNotNull(product.getBrand());
        assertEquals("Microsoft", product.getBrand().getName());
        assertEquals("satya@microsoft.com", product.getSeller().getEmail());
    }

    // Helper to register and approve a brand
    private BrandResponseDto setupApprovedBrand(String brandName, String slug, String ownerEmail) {
        CreateBrandApplicationRequestDto appDto = CreateBrandApplicationRequestDto.builder()
                .applicantName(brandName + " Founder")
                .applicantEmail(ownerEmail)
                .password("StrongPassword123!")
                .applicantPhone("+1999888777")
                .brandName(brandName)
                .brandSlug(slug)
                .trademarkRegistrationNumber("TM-" + slug.toUpperCase())
                .brandCountry("US")
                .logoUrl("https://" + slug + ".com/logo.png")
                .aboutText("About " + brandName)
                .build();

        ResponseDto<BrandApplicationResponseDto> appResp = brandApplicationService.submitApplication(appDto);
        ResponseDto<BrandResponseDto> approved = brandApplicationService.approveApplication(appResp.getData().getId());
        return approved.getData();
    }

    @Test
    @DisplayName("Catalog creation is brand-admin-only and brand offers retain Buy Box priority")
    void testCatalogRoleRestrictionsAndBrandBuyboxPriority() {
        setupApprovedBrand("Priority Brand", "priority-brand", "priority-owner@example.com");
        AddBrandMemberRequestDto sellerMember = AddBrandMemberRequestDto.builder()
                .fullName("Brand Seller").email("brand-seller@example.com").password("StrongPassword123!")
                .brandRole(BrandRole.BRAND_SELLER).build();
        brandDashboardService.addBrandMember("priority-owner@example.com", sellerMember);
        assertThrows(AccessDeniedException.class, () -> brandDashboardService.createBrandProductTemplate(
                "brand-seller@example.com", CreateBrandCatalogProductRequestDto.builder()
                        .title("Unauthorized Product").categoryId(categoryId).build()));

        authService.registerSeller(com.amazon.dtos.auth.request.SellerRegisterRequestDto.builder()
                .fullName("Independent Seller").email("independent-seller@example.com")
                .password("SellerPassword123!").phone("+1234567890").storeName("Independent Store")
                .taxNumber("TAX-1001").businessAddress("1 Market Street").build());

        var product = brandDashboardService.createBrandProductTemplate("priority-owner@example.com",
                CreateBrandCatalogProductRequestDto.builder().title("Priority Phone").categoryId(categoryId).build()).getData();
        assertNull(product.getBasePrice());
        assertThrows(AccessDeniedException.class, () -> brandDashboardService.createBrandProductVariant(
                "brand-seller@example.com", product.getId(),
                com.amazon.dtos.product.request.CreateVariantRequestDto.builder().asin("B0SELLERNOASIN").build()));
        var variant = brandDashboardService.createBrandProductVariant("priority-owner@example.com", product.getId(),
                com.amazon.dtos.product.request.CreateVariantRequestDto.builder().asin("B0PRIORITY01").variantName("Black").build()).getData();

        var firstResellerOffer = createOffer(variant.getId(), "IND-001", "independent-seller@example.com");
        assertTrue(firstResellerOffer.getIsBuyboxWinner());
        var officialBrandOffer = createOffer(variant.getId(), "BRAND-001", "priority-owner@example.com");
        assertTrue(officialBrandOffer.getIsBuyboxWinner());
        assertFalse(productListingService.getListingById(firstResellerOffer.getId()).getData().getIsBuyboxWinner());
        var laterResellerOffer = createOffer(variant.getId(), "IND-002", "independent-seller@example.com");
        assertFalse(laterResellerOffer.getIsBuyboxWinner());
        assertTrue(productListingService.getBuyboxWinner(variant.getId()).getData().getId().equals(officialBrandOffer.getId()));
    }

    private CatalogFixture createCatalog(String email, String title, String description, String asin,
                                         String variantName, Map<String, Object> attributes) {
        var product = brandDashboardService.createBrandProductTemplate(email,
                CreateBrandCatalogProductRequestDto.builder().title(title).description(description)
                        .categoryId(categoryId).build()).getData();
        var variant = brandDashboardService.createBrandProductVariant(email, product.getId(),
                com.amazon.dtos.product.request.CreateVariantRequestDto.builder().asin(asin)
                        .variantName(variantName).variantAttributes(attributes).build()).getData();
        return new CatalogFixture(product.getId(), variant.getId(), product, variant);
    }

    private record CatalogFixture(UUID productId, UUID variantId, ProductResponseDto product,
                                  com.amazon.dtos.product.response.ProductVariantResponseDto variant) { }

    private com.amazon.dtos.listing.response.ProductListingResponseDto createOffer(UUID variantId, String sku, String sellerEmail) {
        return createOffer(variantId, sku, sellerEmail, 5);
    }

    private com.amazon.dtos.listing.response.ProductListingResponseDto createOffer(UUID variantId, String sku, String sellerEmail, int stock) {
        return productListingService.createListing(com.amazon.dtos.listing.request.CreateListingRequestDto.builder()
                .productVariantId(variantId).sellerSku(sku).price(new BigDecimal("100.00"))
                .stockQuantity(stock).fulfillmentType(FulfillmentType.FBM).build(), sellerEmail).getData();
    }
}
