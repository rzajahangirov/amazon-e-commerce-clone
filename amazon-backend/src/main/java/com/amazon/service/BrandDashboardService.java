package com.amazon.service;

import com.amazon.dtos.brand.request.*;
import com.amazon.dtos.brand.response.*;
import com.amazon.dtos.product.request.CreateVariantRequestDto;
import com.amazon.dtos.product.response.ProductResponseDto;
import com.amazon.dtos.product.response.ProductVariantResponseDto;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Brand Dashboard operations, encompassing profile management,
 * employee team management, brand catalog quick-creation, and marketing posts.
 * Enforces strict multi-tenant brand security isolation.
 */
public interface BrandDashboardService {

    // Profile & Change Proposals
    ResponseDto<BrandProfileResponseDto> getBrandProfile(String callerEmail);

    ResponseDto<BrandUpdateRequestResponseDto> submitUpdateRequest(String callerEmail, CreateBrandUpdateRequestDto request);

    // Team Management (BRAND_OWNER only)
    ResponseDto<List<BrandMemberResponseDto>> getBrandMembers(String callerEmail);

    ResponseDto<BrandMemberResponseDto> addBrandMember(String callerEmail, AddBrandMemberRequestDto request);

    ResponseDto<BrandMemberResponseDto> updateBrandMemberRole(String callerEmail, UUID memberId, UpdateBrandMemberRoleRequestDto request);

    ResponseDto<Void> removeBrandMember(String callerEmail, UUID memberId);

    // Products & Stock Management
    ResponseDto<PaginationPayload<ProductResponseDto>> getBrandProducts(String callerEmail, int page, int size);

    ResponseDto<ProductResponseDto> createBrandProductTemplate(String callerEmail, CreateBrandCatalogProductRequestDto request);

    ResponseDto<ProductVariantResponseDto> createBrandProductVariant(String callerEmail, UUID productId, CreateVariantRequestDto request);

    ResponseDto<ProductResponseDto> updateBrandProduct(String callerEmail, UUID productId, UpdateBrandProductRequestDto request);

    ResponseDto<Void> deleteBrandProduct(String callerEmail, UUID productId);

    // Brand Posts & Marketing
    ResponseDto<PaginationPayload<BrandPostResponseDto>> getBrandPosts(String callerEmail, int page, int size);

    ResponseDto<BrandPostResponseDto> createBrandPost(String callerEmail, CreateBrandPostRequestDto request);

    ResponseDto<BrandPostResponseDto> updateBrandPost(String callerEmail, UUID postId, UpdateBrandPostRequestDto request);

    ResponseDto<Void> deleteBrandPost(String callerEmail, UUID postId);

    // Category Taxonomy Proposals (BRAND_OWNER & BRAND_SUPER_ADMIN only)
    ResponseDto<com.amazon.dtos.category.response.CategoryResponseDto> proposeCategory(
            String callerEmail, com.amazon.dtos.category.request.CreateCategoryRequestDto request);

    // Brand Analytics & Statistics
    ResponseDto<BrandAnalyticsResponseDto> getBrandAnalytics(String callerEmail);
}
