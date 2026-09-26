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
    @DisplayName("Workflow 5: Atomic Quick-Create Product & Permissions")
    void testQuickCreateProductAndPermissions() {
        setupApprovedBrand("Samsung", "samsung", "lee@samsung.com");

        QuickCreateProductRequestDto quickDto = QuickCreateProductRequestDto.builder()
                .title("Galaxy S26 Ultra")
                .description("Flagship AI smartphone")
                .categoryId(categoryId)
                .basePrice(new BigDecimal("1199.99"))
                .asin("B09SAMS26U")
                .variantName("Titanium Gray / 512GB")
                .sellerSku("SAM-S26U-512-GRY")
                .price(new BigDecimal("1199.99"))
                .stockQuantity(150)
                .fulfillmentType(FulfillmentType.FBA)
                .build();

        ResponseDto<QuickCreateProductResponseDto> quickResp = brandDashboardService.quickCreateProduct("lee@samsung.com", quickDto);
        assertNotNull(quickResp.getData());
        assertNotNull(quickResp.getData().getProductId());
        assertNotNull(quickResp.getData().getVariantId());
        assertNotNull(quickResp.getData().getListingId());
        assertEquals("Galaxy S26 Ultra", quickResp.getData().getProductTitle());
        assertEquals(150, quickResp.getData().getStockQuantity());

        // Verify Product entity relationship to Brand
        Product productEntity = productRepository.findWithDetailsById(quickResp.getData().getProductId()).orElseThrow();
        assertNotNull(productEntity.getBrand());
        assertEquals("Samsung", productEntity.getBrand().getName());
        assertEquals(quickResp.getData().getBrandId(), productEntity.getBrand().getId());

        // Verify product shows in getBrandProducts
        ResponseDto<PaginationPayload<ProductResponseDto>> products =
                brandDashboardService.getBrandProducts("lee@samsung.com", 0, 10);
        assertEquals(1, products.getData().getTotalElements());
        assertEquals("Samsung", products.getData().getContent().get(0).getBrandName());

        // Update product
        UpdateBrandProductRequestDto updateProductDto = UpdateBrandProductRequestDto.builder()
                .title("Galaxy S26 Ultra Pro")
                .basePrice(new BigDecimal("1249.99"))
                .build();

        ResponseDto<ProductResponseDto> updatedProd = brandDashboardService.updateBrandProduct(
                "lee@samsung.com", quickResp.getData().getProductId(), updateProductDto);
        assertEquals("Galaxy S26 Ultra Pro", updatedProd.getData().getTitle());
        assertEquals(new BigDecimal("1249.99"), updatedProd.getData().getBasePrice());

        // Delete product
        ResponseDto<Void> deleteResp = brandDashboardService.deleteBrandProduct(
                "lee@samsung.com", quickResp.getData().getProductId());
        assertNotNull(deleteResp);
    }

    @Test
    @DisplayName("Workflow 6: Multi-Tenant Brand Security Isolation (CRITICAL Cross-Brand Protection)")
    void testCrossBrandSecurityIsolation() {
        // Setup Brand A (LG) and Brand B (Panasonic)
        setupApprovedBrand("LG Electronics", "lg", "owner@lg.com");
        setupApprovedBrand("Panasonic Corp", "panasonic", "owner@panasonic.com");

        // LG Owner creates a product
        QuickCreateProductRequestDto lgProduct = QuickCreateProductRequestDto.builder()
                .title("LG OLED TV C5")
                .categoryId(categoryId)
                .basePrice(new BigDecimal("1799.99"))
                .asin("B09LGC5OLED")
                .variantName("65 inch")
                .sellerSku("LG-C5-65")
                .price(new BigDecimal("1799.99"))
                .stockQuantity(40)
                .build();
        ResponseDto<QuickCreateProductResponseDto> lgCreated =
                brandDashboardService.quickCreateProduct("owner@lg.com", lgProduct);
        UUID lgProductId = lgCreated.getData().getProductId();

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
        QuickCreateProductRequestDto boseProduct = QuickCreateProductRequestDto.builder()
                .title("Bose QC Ultra")
                .categoryId(categoryId)
                .basePrice(new BigDecimal("379.00"))
                .asin("B09BOSEQC")
                .variantName("Black")
                .sellerSku("BOSE-QC-BLK")
                .price(new BigDecimal("379.00"))
                .stockQuantity(100)
                .build();
        brandDashboardService.quickCreateProduct("amar@bose.com", boseProduct);

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
        QuickCreateProductRequestDto surface = QuickCreateProductRequestDto.builder()
                .title("Surface Laptop 7")
                .categoryId(categoryId)
                .basePrice(new BigDecimal("1299.99"))
                .asin("B09MSFTLAP7")
                .variantName("Platinum")
                .sellerSku("MS-SL7-PLAT")
                .price(new BigDecimal("1299.99"))
                .stockQuantity(50)
                .build();
        ResponseDto<QuickCreateProductResponseDto> quickResp =
                brandDashboardService.quickCreateProduct("satya@microsoft.com", surface);

        Product product = productRepository.findWithDetailsById(quickResp.getData().getProductId()).orElseThrow();
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
}
