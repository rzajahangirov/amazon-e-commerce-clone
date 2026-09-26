package com.amazon.service;

import com.amazon.dtos.brand.request.AddBrandMemberRequestDto;
import com.amazon.dtos.brand.request.CreateBrandApplicationRequestDto;
import com.amazon.dtos.brand.response.BrandApplicationResponseDto;
import com.amazon.dtos.brand.response.BrandResponseDto;
import com.amazon.dtos.category.request.CreateCategoryRequestDto;
import com.amazon.dtos.category.response.CategoryResponseDto;
import com.amazon.dtos.product.response.ProductResponseDto;
import com.amazon.enums.BrandRole;
import com.amazon.enums.FulfillmentType;
import com.amazon.exception.BusinessRuleException;
import com.amazon.exception.DuplicateResourceException;
import com.amazon.exception.ResourceNotFoundException;
import com.amazon.payloads.ResponseDto;
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
class CategoryModuleIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandDashboardService brandDashboardService;

    @Autowired
    private BrandApplicationService brandApplicationService;

    @Test
    @DisplayName("Requirement 1: Automated SEO Slug Generation and Strict Duplicate Validations")
    void testAutomatedSlugGenerationAndValidations() {
        // 1. Auto-generate slug from category name with special characters and accents
        CreateCategoryRequestDto request = CreateCategoryRequestDto.builder()
                .name("Smartphones & Wearable Devices")
                .build();

        ResponseDto<CategoryResponseDto> created = categoryService.createCategory(request);
        assertNotNull(created.getData());
        assertEquals("Smartphones & Wearable Devices", created.getData().getName());
        assertEquals("smartphones-and-wearable-devices", created.getData().getSlug());
        assertTrue(created.getData().getIsApproved());

        // 2. Duplicate Category Name validation (case-insensitive)
        CreateCategoryRequestDto duplicateNameRequest = CreateCategoryRequestDto.builder()
                .name("smartphones & wearable devices")
                .build();

        assertThrows(DuplicateResourceException.class, () ->
                categoryService.createCategory(duplicateNameRequest));

        // 3. Duplicate Category Slug validation
        CreateCategoryRequestDto duplicateSlugRequest = CreateCategoryRequestDto.builder()
                .name("Different Name Same Slug")
                .slug("smartphones-and-wearable-devices")
                .build();

        assertThrows(DuplicateResourceException.class, () ->
                categoryService.createCategory(duplicateSlugRequest));
    }

    @Test
    @DisplayName("Requirement 2 & 3: Platform Admin Direct Creation - Immediately Approved, Public & Usable")
    void testPlatformAdminCreationFlow() {
        CreateCategoryRequestDto request = CreateCategoryRequestDto.builder()
                .name("Home Appliances")
                .build();

        ResponseDto<CategoryResponseDto> adminCategory = categoryService.createCategory(request);
        UUID categoryId = adminCategory.getData().getId();
        assertNotNull(categoryId);
        assertTrue(adminCategory.getData().getIsApproved());
        assertEquals("home-appliances", adminCategory.getData().getSlug());

        // Immediately visible in public read by ID
        ResponseDto<CategoryResponseDto> byId = categoryService.getCategoryById(categoryId);
        assertNotNull(byId.getData());
        assertEquals("Home Appliances", byId.getData().getName());

        // Immediately visible in public read by Slug
        ResponseDto<CategoryResponseDto> bySlug = categoryService.getCategoryBySlug("home-appliances");
        assertNotNull(bySlug.getData());
        assertEquals(categoryId, bySlug.getData().getId());

        // Immediately visible in public root categories tree
        ResponseDto<List<CategoryResponseDto>> rootCategories = categoryService.getAllRootCategories();
        assertTrue(rootCategories.getData().stream().anyMatch(c -> c.getId().equals(categoryId)));
    }

    @Test
    @DisplayName("Requirement 2: Brand Dashboard Proposal - Role Enforcement (Only OWNER & SUPER_ADMIN)")
    void testBrandDashboardProposalRoleEnforcement() {
        BrandResponseDto brand = setupApprovedBrand("Anker Innovations", "anker", "ceo@anker.com");

        // 1. Add BRAND_SELLER member
        AddBrandMemberRequestDto addSeller = AddBrandMemberRequestDto.builder()
                .fullName("Anker Seller Rep")
                .email("seller@anker.com")
                .brandRole(BrandRole.BRAND_SELLER)
                .password("SellerSecret123!")
                .build();
        brandDashboardService.addBrandMember("ceo@anker.com", addSeller);

        // 2. BRAND_SELLER attempt to propose category -> Denied (403)
        CreateCategoryRequestDto proposalBySeller = CreateCategoryRequestDto.builder()
                .name("GaN Chargers & Docks")
                .build();

        assertThrows(AccessDeniedException.class, () ->
                brandDashboardService.proposeCategory("seller@anker.com", proposalBySeller));

        // 3. BRAND_OWNER attempt to propose category -> Success with isApproved = false
        ResponseDto<CategoryResponseDto> proposed = brandDashboardService.proposeCategory("ceo@anker.com", proposalBySeller);
        assertNotNull(proposed.getData());
        assertEquals("GaN Chargers & Docks", proposed.getData().getName());
        assertEquals("gan-chargers-and-docks", proposed.getData().getSlug());
        assertFalse(proposed.getData().getIsApproved());
    }

    @Test
    @DisplayName("Requirement 2 & 3: Unapproved Category Complete Isolation Across Platform")
    void testUnapprovedCategoryCompleteIsolation() {
        BrandResponseDto brand = setupApprovedBrand("Logitech", "logitech", "owner@logitech.com");

        // Brand Owner proposes category -> isApproved = false
        CreateCategoryRequestDto proposal = CreateCategoryRequestDto.builder()
                .name("Ergonomic Keyboards")
                .build();
        ResponseDto<CategoryResponseDto> proposed = brandDashboardService.proposeCategory("owner@logitech.com", proposal);
        UUID pendingCatId = proposed.getData().getId();
        assertFalse(proposed.getData().getIsApproved());

        // 1. Public API getCategoryById -> 404 Not Found
        assertThrows(ResourceNotFoundException.class, () ->
                categoryService.getCategoryById(pendingCatId));

        // 2. Public API getCategoryBySlug -> 404 Not Found
        assertThrows(ResourceNotFoundException.class, () ->
                categoryService.getCategoryBySlug("ergonomic-keyboards"));

        // 3. Excluded from public root category list
        ResponseDto<List<CategoryResponseDto>> publicRoots = categoryService.getAllRootCategories();
        assertFalse(publicRoots.getData().stream().anyMatch(c -> c.getId().equals(pendingCatId)));

        // 4. Product Creation via ProductService fails because category is unapproved
        assertThrows(BusinessRuleException.class, () ->
                brandDashboardService.createBrandProductTemplate("owner@logitech.com",
                        com.amazon.dtos.brand.request.CreateBrandCatalogProductRequestDto.builder()
                                .title("Ergo Wave K860").description("Ergonomic split keyboard")
                                .categoryId(pendingCatId).build()));

        // 5. Brand catalog product creation fails because category is unapproved
        assertThrows(BusinessRuleException.class, () ->
                brandDashboardService.createBrandProductTemplate("owner@logitech.com",
                        com.amazon.dtos.brand.request.CreateBrandCatalogProductRequestDto.builder()
                                .title("Ergo Wave K860 Quick").description("Ergonomic split keyboard")
                                .categoryId(pendingCatId).build()));
    }

    @Test
    @DisplayName("Requirement 2 & 3: Platform Admin Approval Flow - Transitions to Approved and Becomes Active")
    void testAdminApprovalFlowActivatesCategory() {
        BrandResponseDto brand = setupApprovedBrand("Razer", "razer", "min@razer.com");

        // 1. Propose category (isApproved = false)
        CreateCategoryRequestDto proposal = CreateCategoryRequestDto.builder()
                .name("Haptic Gaming Chairs")
                .build();
        ResponseDto<CategoryResponseDto> proposed = brandDashboardService.proposeCategory("min@razer.com", proposal);
        UUID categoryId = proposed.getData().getId();
        assertFalse(proposed.getData().getIsApproved());

        // 2. Admin inspects pending category via admin query
        ResponseDto<CategoryResponseDto> adminView = categoryService.getCategoryByIdForAdmin(categoryId);
        assertNotNull(adminView.getData());
        assertFalse(adminView.getData().getIsApproved());

        // 3. Admin approves category
        ResponseDto<CategoryResponseDto> approved = categoryService.approveCategory(categoryId);
        assertNotNull(approved.getData());
        assertTrue(approved.getData().getIsApproved());

        // 4. Now immediately active and accessible publicly
        ResponseDto<CategoryResponseDto> publicView = categoryService.getCategoryById(categoryId);
        assertNotNull(publicView.getData());
        assertTrue(publicView.getData().getIsApproved());
        assertEquals("haptic-gaming-chairs", publicView.getData().getSlug());

        // 5. Create catalog template and ASIN without offer price or inventory.
        var template = brandDashboardService.createBrandProductTemplate("min@razer.com",
                com.amazon.dtos.brand.request.CreateBrandCatalogProductRequestDto.builder()
                        .title("Razer Enki Pro HyperSense")
                        .description("All-Day comfort gaming chair with advanced haptics")
                        .categoryId(categoryId).build());
        assertNotNull(template.getData());
        assertEquals("Razer Enki Pro HyperSense", template.getData().getTitle());
        assertNull(template.getData().getBasePrice());
        var variant = brandDashboardService.createBrandProductVariant("min@razer.com", template.getData().getId(),
                com.amazon.dtos.product.request.CreateVariantRequestDto.builder()
                        .asin("B09RAZERCH1").variantName("Black / Green")
                        .variantAttributes(Map.of("color", "Black/Green")).build());
        assertEquals("B09RAZERCH1", variant.getData().getAsin());
    }

    // Helper method
    private BrandResponseDto setupApprovedBrand(String brandName, String slug, String ownerEmail) {
        CreateBrandApplicationRequestDto appDto = CreateBrandApplicationRequestDto.builder()
                .applicantName(brandName + " Founder")
                .applicantEmail(ownerEmail)
                .password("StrongPassword123!")
                .applicantPhone("+1888777666")
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
