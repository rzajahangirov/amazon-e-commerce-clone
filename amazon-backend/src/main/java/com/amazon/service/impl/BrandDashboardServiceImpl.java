package com.amazon.service.impl;

import com.amazon.dtos.brand.request.*;
import com.amazon.dtos.brand.response.*;
import com.amazon.dtos.product.response.ProductResponseDto;
import com.amazon.dtos.product.response.ProductVariantResponseDto;
import com.amazon.entity.*;
import com.amazon.enums.*;
import com.amazon.exception.BusinessRuleException;
import com.amazon.exception.DuplicateResourceException;
import com.amazon.exception.ResourceNotFoundException;
import com.amazon.payloads.ApiResponse;
import com.amazon.payloads.BrandError;
import com.amazon.payloads.CatalogError;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;
import com.amazon.repository.*;
import com.amazon.service.BrandDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of {@link BrandDashboardService} handling brand dashboard management.
 * Enforces strict multi-tenant brand isolation and role-based permissions for brand team members.
 * Adheres to Senior Backend Developer Guidelines Section 1, 5, 6, 8, 9, 10.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BrandDashboardServiceImpl implements BrandDashboardService {

    private final BrandMemberRepository brandMemberRepository;
    private final BrandRepository brandRepository;
    private final BrandUpdateRequestRepository brandUpdateRequestRepository;
    private final BrandPostRepository brandPostRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductListingRepository productListingRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    // Allowed role sets
    private static final Set<BrandRole> PRODUCT_MANAGEMENT_ROLES = EnumSet.of(
            BrandRole.BRAND_OWNER,
            BrandRole.BRAND_SUPER_ADMIN,
            BrandRole.BRAND_ADMIN,
            BrandRole.BRAND_SELLER
    );

    private static final Set<BrandRole> PRODUCT_DELETE_ROLES = EnumSet.of(
            BrandRole.BRAND_OWNER,
            BrandRole.BRAND_SUPER_ADMIN
    );

    private static final Set<BrandRole> MARKETING_ROLES = EnumSet.of(
            BrandRole.BRAND_OWNER,
            BrandRole.BRAND_SUPER_ADMIN,
            BrandRole.BRAND_ADMIN,
            BrandRole.BRAND_MARKETING_MEMBER
    );

    private static final Set<BrandRole> UPDATE_REQUEST_ROLES = EnumSet.of(
            BrandRole.BRAND_OWNER,
            BrandRole.BRAND_SUPER_ADMIN,
            BrandRole.BRAND_ADMIN
    );

    // ==========================================
    // Profile & Proposals
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<BrandProfileResponseDto> getBrandProfile(String callerEmail) {
        BrandMember callerMember = resolveCallerMember(callerEmail);
        Brand brand = callerMember.getBrand();

        List<BrandMember> members = brandMemberRepository.findByBrandId(brand.getId());
        List<BrandMemberResponseDto> memberDtos = members.stream()
                .map(this::mapToMemberResponse)
                .toList();

        BrandProfileResponseDto profile = BrandProfileResponseDto.builder()
                .brand(mapToBrandResponse(brand))
                .teamMembers(memberDtos)
                .build();

        return ApiResponse.success(profile, "Brand profile retrieved successfully");
    }

    @Override
    @Transactional
    public ResponseDto<BrandUpdateRequestResponseDto> submitUpdateRequest(
            String callerEmail, CreateBrandUpdateRequestDto request) {
        BrandMember callerMember = resolveCallerMember(callerEmail);

        if (!UPDATE_REQUEST_ROLES.contains(callerMember.getBrandRole())) {
            throw new AccessDeniedException(BrandError.INSUFFICIENT_BRAND_PERMISSIONS.getMessage());
        }

        BrandUpdateRequest updateRequest = BrandUpdateRequest.builder()
                .brand(callerMember.getBrand())
                .requestedByUser(callerMember.getUser())
                .proposedBrandName(request.getProposedBrandName())
                .proposedLogoUrl(request.getProposedLogoUrl())
                .proposedAboutText(request.getProposedAboutText())
                .proposedTrademarkNo(request.getProposedTrademarkNo())
                .status(BrandUpdateRequestStatus.PENDING)
                .build();

        BrandUpdateRequest saved = brandUpdateRequestRepository.save(updateRequest);
        log.info("Brand update request submitted by user: {} for brand: {}", callerEmail, callerMember.getBrand().getId());

        return ApiResponse.success(mapToUpdateRequestResponse(saved), "Brand update request submitted successfully");
    }

    // ==========================================
    // Team Management (BRAND_OWNER only)
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<List<BrandMemberResponseDto>> getBrandMembers(String callerEmail) {
        BrandMember callerMember = resolveCallerMember(callerEmail);

        List<BrandMember> members = brandMemberRepository.findByBrandId(callerMember.getBrand().getId());
        List<BrandMemberResponseDto> dtos = members.stream()
                .map(this::mapToMemberResponse)
                .toList();

        return ApiResponse.success(dtos, "Brand members retrieved successfully");
    }

    @Override
    @Transactional
    public ResponseDto<BrandMemberResponseDto> addBrandMember(String callerEmail, AddBrandMemberRequestDto request) {
        BrandMember callerMember = resolveCallerMember(callerEmail);
        enforceBrandOwner(callerMember);

        Role sellerRole = roleRepository.findByName("ROLE_SELLER")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("ROLE_SELLER")
                        .description("Seller role")
                        .build()));

        // Find or create employee user
        User employee = userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .fullName(request.getFullName())
                            .email(request.getEmail())
                            .passwordHash(passwordEncoder.encode(request.getPassword()))
                            .phone(request.getPhone())
                            .status(UserStatus.ACTIVE)
                            .roles(new HashSet<>())
                            .build();
                    return userRepository.save(newUser);
                });

        employee.addRole(sellerRole);
        User savedEmployee = userRepository.save(employee);

        if (brandMemberRepository.existsByBrandIdAndUserId(callerMember.getBrand().getId(), savedEmployee.getId())) {
            throw new DuplicateResourceException(BrandError.MEMBER_ALREADY_EXISTS.getMessage());
        }

        // Create or link SellerProfile for employee
        SellerProfile sellerProfile = sellerProfileRepository.findByUserId(savedEmployee.getId())
                .orElseGet(() -> SellerProfile.builder()
                        .user(savedEmployee)
                        .build());
        sellerProfile.setBrand(callerMember.getBrand());
        if (sellerProfile.getStoreName() == null || sellerProfile.getStoreName().isBlank()) {
            sellerProfile.setStoreName(callerMember.getBrand().getName() + " - " + savedEmployee.getFullName());
        }
        sellerProfile.setIsVerified(true);
        SellerProfile savedSellerProfile = sellerProfileRepository.save(sellerProfile);
        savedEmployee.setSellerProfile(savedSellerProfile);
        callerMember.getBrand().getSellerProfiles().add(savedSellerProfile);

        BrandMember member = BrandMember.builder()
                .brand(callerMember.getBrand())
                .user(savedEmployee)
                .brandRole(request.getBrandRole())
                .assignedAt(LocalDateTime.now())
                .build();

        BrandMember saved = brandMemberRepository.save(member);
        savedEmployee.getBrandMembers().add(saved);
        callerMember.getBrand().getMembers().add(saved);

        log.info("Brand member added: user={}, role={}, brand={}",
                savedEmployee.getEmail(), request.getBrandRole(), callerMember.getBrand().getId());

        return ApiResponse.success(mapToMemberResponse(saved), "Brand member added successfully");
    }

    @Override
    @Transactional
    public ResponseDto<BrandMemberResponseDto> updateBrandMemberRole(
            String callerEmail, UUID memberId, UpdateBrandMemberRoleRequestDto request) {
        BrandMember callerMember = resolveCallerMember(callerEmail);
        enforceBrandOwner(callerMember);

        BrandMember targetMember = brandMemberRepository.findWithDetailsById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException(BrandError.MEMBER_NOT_FOUND.getMessage() + memberId));

        validateBrandIsolation(targetMember.getBrand().getId(), callerMember);

        if (targetMember.getBrandRole() == BrandRole.BRAND_OWNER) {
            throw new BusinessRuleException(BrandError.CANNOT_CHANGE_OWNER_ROLE.getMessage());
        }

        targetMember.setBrandRole(request.getBrandRole());
        BrandMember updated = brandMemberRepository.save(targetMember);
        log.info("Brand member role updated: memberId={}, newRole={}", memberId, request.getBrandRole());

        return ApiResponse.success(mapToMemberResponse(updated), "Brand member role updated successfully");
    }

    @Override
    @Transactional
    public ResponseDto<Void> removeBrandMember(String callerEmail, UUID memberId) {
        BrandMember callerMember = resolveCallerMember(callerEmail);
        enforceBrandOwner(callerMember);

        BrandMember targetMember = brandMemberRepository.findWithDetailsById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException(BrandError.MEMBER_NOT_FOUND.getMessage() + memberId));

        validateBrandIsolation(targetMember.getBrand().getId(), callerMember);

        if (targetMember.getBrandRole() == BrandRole.BRAND_OWNER) {
            throw new BusinessRuleException(BrandError.CANNOT_REMOVE_OWNER.getMessage());
        }

        targetMember.setDeletedAt(LocalDateTime.now());
        brandMemberRepository.save(targetMember);
        log.info("Brand member removed: memberId={}, brandId={}", memberId, callerMember.getBrand().getId());

        return ApiResponse.success(null, "Brand member removed successfully");
    }

    // ==========================================
    // Products & Stock Management
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<PaginationPayload<ProductResponseDto>> getBrandProducts(String callerEmail, int page, int size) {
        BrandMember callerMember = resolveCallerMember(callerEmail);
        enforcePermission(callerMember, PRODUCT_MANAGEMENT_ROLES, "Insufficient permissions to view brand products");

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> productsPage = productRepository.findByBrandId(callerMember.getBrand().getId(), pageable);

        PaginationPayload<ProductResponseDto> payload = PaginationPayload.<ProductResponseDto>builder()
                .content(productsPage.getContent().stream().map(this::mapToProductResponse).toList())
                .pageNumber(productsPage.getNumber())
                .pageSize(productsPage.getSize())
                .totalElements(productsPage.getTotalElements())
                .totalPages(productsPage.getTotalPages())
                .last(productsPage.isLast())
                .build();

        return ApiResponse.success(payload, "Brand products retrieved successfully");
    }

    @Override
    @Transactional
    public ResponseDto<QuickCreateProductResponseDto> quickCreateProduct(
            String callerEmail, QuickCreateProductRequestDto request) {
        BrandMember callerMember = resolveCallerMember(callerEmail);
        enforcePermission(callerMember, PRODUCT_MANAGEMENT_ROLES, "Insufficient permissions to create products");

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.CATEGORY_NOT_FOUND.getMessage() + request.getCategoryId()));

        // 1. Create Product
        Product product = Product.builder()
                .seller(callerMember.getUser())
                .brand(callerMember.getBrand())
                .category(category)
                .title(request.getTitle())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .status(ProductStatus.ACTIVE)
                .build();
        Product savedProduct = productRepository.save(product);
        callerMember.getBrand().getProducts().add(savedProduct);

        // 2. Create ProductVariant
        ProductVariant variant = ProductVariant.builder()
                .product(savedProduct)
                .asin(request.getAsin())
                .variantName(request.getVariantName())
                .variantAttributes(request.getVariantAttributes() != null ? request.getVariantAttributes() : new HashMap<>())
                .build();
        ProductVariant savedVariant = productVariantRepository.save(variant);

        // 3. Create ProductListing
        ProductListing listing = ProductListing.builder()
                .productVariant(savedVariant)
                .seller(callerMember.getUser())
                .sellerSku(request.getSellerSku())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .fulfillmentType(request.getFulfillmentType() != null ? request.getFulfillmentType() : FulfillmentType.FBM)
                .isBuyboxWinner(true)
                .status(ListingStatus.ACTIVE)
                .build();
        ProductListing savedListing = productListingRepository.save(listing);

        log.info("Product quick-created atomically: productId={}, variantId={}, listingId={}, brandId={}",
                savedProduct.getId(), savedVariant.getId(), savedListing.getId(), callerMember.getBrand().getId());

        QuickCreateProductResponseDto response = QuickCreateProductResponseDto.builder()
                .productId(savedProduct.getId())
                .productTitle(savedProduct.getTitle())
                .variantId(savedVariant.getId())
                .asin(savedVariant.getAsin())
                .variantName(savedVariant.getVariantName())
                .variantAttributes(savedVariant.getVariantAttributes())
                .listingId(savedListing.getId())
                .sellerSku(savedListing.getSellerSku())
                .price(savedListing.getPrice())
                .stockQuantity(savedListing.getStockQuantity())
                .brandId(callerMember.getBrand().getId())
                .build();

        return ApiResponse.success(response, "Product, variant, and listing quick-created successfully");
    }

    @Override
    @Transactional
    public ResponseDto<ProductResponseDto> updateBrandProduct(
            String callerEmail, UUID productId, UpdateBrandProductRequestDto request) {
        BrandMember callerMember = resolveCallerMember(callerEmail);
        enforcePermission(callerMember, PRODUCT_MANAGEMENT_ROLES, "Insufficient permissions to update products");

        Product product = productRepository.findWithDetailsById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.PRODUCT_NOT_FOUND.getMessage() + productId));

        validateBrandIsolation(product.getBrandId(), callerMember);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            product.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getBasePrice() != null) {
            product.setBasePrice(request.getBasePrice());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(CatalogError.CATEGORY_NOT_FOUND.getMessage() + request.getCategoryId()));
            product.setCategory(category);
        }

        Product updated = productRepository.save(product);
        log.info("Brand product updated: productId={}, brandId={}", productId, callerMember.getBrand().getId());

        return ApiResponse.success(mapToProductResponse(updated), "Brand product updated successfully");
    }

    @Override
    @Transactional
    public ResponseDto<Void> deleteBrandProduct(String callerEmail, UUID productId) {
        BrandMember callerMember = resolveCallerMember(callerEmail);
        enforcePermission(callerMember, PRODUCT_DELETE_ROLES, "Only BRAND_OWNER or BRAND_SUPER_ADMIN can delete brand products");

        Product product = productRepository.findWithDetailsById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.PRODUCT_NOT_FOUND.getMessage() + productId));

        validateBrandIsolation(product.getBrandId(), callerMember);

        LocalDateTime now = LocalDateTime.now();
        product.setStatus(ProductStatus.REMOVED);
        product.setDeletedAt(now);
        productRepository.save(product);

        // Soft-delete associated listings
        if (product.getVariants() != null) {
            for (ProductVariant variant : product.getVariants()) {
                variant.setDeletedAt(now);
                productVariantRepository.save(variant);
                List<ProductListing> listings = productListingRepository.findByProductVariantId(variant.getId());
                for (ProductListing listing : listings) {
                    listing.setStatus(ListingStatus.INACTIVE);
                    listing.setDeletedAt(now);
                }
                productListingRepository.saveAll(listings);
            }
        }

        log.info("Brand product soft-deleted: productId={}, brandId={}", productId, callerMember.getBrand().getId());

        return ApiResponse.success(null, "Brand product deleted successfully");
    }

    // ==========================================
    // Brand Posts & Marketing
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<PaginationPayload<BrandPostResponseDto>> getBrandPosts(String callerEmail, int page, int size) {
        BrandMember callerMember = resolveCallerMember(callerEmail);
        enforcePermission(callerMember, MARKETING_ROLES, "Insufficient permissions to view brand posts");

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BrandPost> postsPage = brandPostRepository.findByBrandId(callerMember.getBrand().getId(), pageable);

        PaginationPayload<BrandPostResponseDto> payload = PaginationPayload.<BrandPostResponseDto>builder()
                .content(postsPage.getContent().stream().map(this::mapToPostResponse).toList())
                .pageNumber(postsPage.getNumber())
                .pageSize(postsPage.getSize())
                .totalElements(postsPage.getTotalElements())
                .totalPages(postsPage.getTotalPages())
                .last(postsPage.isLast())
                .build();

        return ApiResponse.success(payload, "Brand posts retrieved successfully");
    }

    @Override
    @Transactional
    public ResponseDto<BrandPostResponseDto> createBrandPost(String callerEmail, CreateBrandPostRequestDto request) {
        BrandMember callerMember = resolveCallerMember(callerEmail);
        enforcePermission(callerMember, MARKETING_ROLES, "Insufficient permissions to create brand posts");

        BrandPost post = BrandPost.builder()
                .brand(callerMember.getBrand())
                .authorUser(callerMember.getUser())
                .imageUrl(request.getImageUrl())
                .caption(request.getCaption())
                .build();

        BrandPost saved = brandPostRepository.save(post);
        log.info("Brand post created: postId={}, brandId={}", saved.getId(), callerMember.getBrand().getId());

        return ApiResponse.success(mapToPostResponse(saved), "Brand post created successfully");
    }

    @Override
    @Transactional
    public ResponseDto<BrandPostResponseDto> updateBrandPost(
            String callerEmail, UUID postId, UpdateBrandPostRequestDto request) {
        BrandMember callerMember = resolveCallerMember(callerEmail);
        enforcePermission(callerMember, MARKETING_ROLES, "Insufficient permissions to update brand posts");

        BrandPost post = brandPostRepository.findWithDetailsById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(BrandError.POST_NOT_FOUND.getMessage() + postId));

        validateBrandIsolation(post.getBrand().getId(), callerMember);

        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            post.setImageUrl(request.getImageUrl());
        }
        if (request.getCaption() != null) {
            post.setCaption(request.getCaption());
        }

        BrandPost updated = brandPostRepository.save(post);
        log.info("Brand post updated: postId={}, brandId={}", postId, callerMember.getBrand().getId());

        return ApiResponse.success(mapToPostResponse(updated), "Brand post updated successfully");
    }

    @Override
    @Transactional
    public ResponseDto<Void> deleteBrandPost(String callerEmail, UUID postId) {
        BrandMember callerMember = resolveCallerMember(callerEmail);
        enforcePermission(callerMember, MARKETING_ROLES, "Insufficient permissions to delete brand posts");

        BrandPost post = brandPostRepository.findWithDetailsById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(BrandError.POST_NOT_FOUND.getMessage() + postId));

        validateBrandIsolation(post.getBrand().getId(), callerMember);

        post.setDeletedAt(LocalDateTime.now());
        brandPostRepository.save(post);
        log.info("Brand post deleted: postId={}, brandId={}", postId, callerMember.getBrand().getId());

        return ApiResponse.success(null, "Brand post deleted successfully");
    }

    // ==========================================
    // Security & Multi-Tenant Helpers
    // ==========================================

    private BrandMember resolveCallerMember(String callerEmail) {
        User user = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + callerEmail));

        BrandMember member = brandMemberRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    SellerProfile profile = sellerProfileRepository.findByUserId(user.getId())
                            .orElseThrow(() -> new AccessDeniedException(BrandError.NOT_A_BRAND_MEMBER.getMessage()));
                    if (profile.getBrand() == null) {
                        throw new AccessDeniedException(BrandError.NOT_A_BRAND_MEMBER.getMessage());
                    }
                    return brandMemberRepository.findByBrandIdAndUserId(profile.getBrand().getId(), user.getId())
                            .orElseThrow(() -> new AccessDeniedException(BrandError.NOT_A_BRAND_MEMBER.getMessage()));
                });

        if (member.getBrand() == null || member.getBrand().getStatus() == BrandStatus.SUSPENDED
                || member.getBrand().getDeletedAt() != null) {
            throw new AccessDeniedException(BrandError.BRAND_SUSPENDED.getMessage());
        }

        return member;
    }

    private void validateBrandIsolation(UUID targetBrandId, BrandMember callerMember) {
        if (targetBrandId == null || !targetBrandId.equals(callerMember.getBrand().getId())) {
            throw new AccessDeniedException(BrandError.UNAUTHORIZED_BRAND_ACCESS.getMessage());
        }
    }

    private void enforceBrandOwner(BrandMember callerMember) {
        if (callerMember.getBrandRole() != BrandRole.BRAND_OWNER) {
            throw new AccessDeniedException("Only BRAND_OWNER can manage team members");
        }
    }

    private void enforcePermission(BrandMember callerMember, Set<BrandRole> allowedRoles, String errorMessage) {
        if (!allowedRoles.contains(callerMember.getBrandRole())) {
            throw new AccessDeniedException(errorMessage);
        }
    }

    // ==========================================
    // Mappers
    // ==========================================

    private BrandResponseDto mapToBrandResponse(Brand brand) {
        return BrandResponseDto.builder()
                .id(brand.getId())
                .name(brand.getName())
                .slug(brand.getSlug())
                .trademarkRegistrationNumber(brand.getTrademarkRegistrationNumber())
                .brandCountry(brand.getBrandCountry())
                .logoUrl(brand.getLogoUrl())
                .aboutText(brand.getAboutText())
                .status(brand.getStatus())
                .ownerUserId(brand.getOwnerUser() != null ? brand.getOwnerUser().getId() : null)
                .ownerName(brand.getOwnerUser() != null ? brand.getOwnerUser().getFullName() : null)
                .createdAt(brand.getCreatedAt())
                .updatedAt(brand.getUpdatedAt())
                .build();
    }

    private BrandMemberResponseDto mapToMemberResponse(BrandMember m) {
        return BrandMemberResponseDto.builder()
                .id(m.getId())
                .brandId(m.getBrand().getId())
                .brandName(m.getBrand().getName())
                .userId(m.getUser().getId())
                .userFullName(m.getUser().getFullName())
                .userEmail(m.getUser().getEmail())
                .brandRole(m.getBrandRole())
                .assignedAt(m.getAssignedAt())
                .build();
    }

    private BrandUpdateRequestResponseDto mapToUpdateRequestResponse(BrandUpdateRequest req) {
        return BrandUpdateRequestResponseDto.builder()
                .id(req.getId())
                .brandId(req.getBrand().getId())
                .brandName(req.getBrand().getName())
                .requestedByUserId(req.getRequestedByUser().getId())
                .requestedByUserEmail(req.getRequestedByUser().getEmail())
                .proposedBrandName(req.getProposedBrandName())
                .proposedLogoUrl(req.getProposedLogoUrl())
                .proposedAboutText(req.getProposedAboutText())
                .proposedTrademarkNo(req.getProposedTrademarkNo())
                .status(req.getStatus())
                .rejectionReason(req.getRejectionReason())
                .createdAt(req.getCreatedAt())
                .updatedAt(req.getUpdatedAt())
                .build();
    }

    private BrandPostResponseDto mapToPostResponse(BrandPost post) {
        return BrandPostResponseDto.builder()
                .id(post.getId())
                .brandId(post.getBrand().getId())
                .brandName(post.getBrand().getName())
                .authorUserId(post.getAuthorUser().getId())
                .authorName(post.getAuthorUser().getFullName())
                .imageUrl(post.getImageUrl())
                .caption(post.getCaption())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    private ProductResponseDto mapToProductResponse(Product product) {
        List<ProductVariantResponseDto> variantDtos = product.getVariants() != null
                ? product.getVariants().stream().map(v -> ProductVariantResponseDto.builder()
                        .id(v.getId())
                        .productId(product.getId())
                        .asin(v.getAsin())
                        .variantName(v.getVariantName())
                        .variantAttributes(v.getVariantAttributes())
                        .createdAt(v.getCreatedAt())
                        .build()).toList()
                : List.of();

        return ProductResponseDto.builder()
                .id(product.getId())
                .sellerId(product.getSeller() != null ? product.getSeller().getId() : null)
                .sellerName(product.getSeller() != null ? product.getSeller().getFullName() : null)
                .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .title(product.getTitle())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .status(product.getStatus())
                .variants(variantDtos)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
