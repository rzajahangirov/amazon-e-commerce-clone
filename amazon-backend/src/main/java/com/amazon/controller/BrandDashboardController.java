package com.amazon.controller;

import com.amazon.dtos.brand.request.*;
import com.amazon.dtos.brand.response.*;
import com.amazon.dtos.product.response.ProductResponseDto;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;
import com.amazon.service.BrandDashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for Brand Management Dashboard.
 * Injects Principal and passes ONLY the email string to the service layer.
 * Adheres to Senior Backend Developer Guidelines Section 1, 3, 4, 10.2.
 */
@RestController
@RequestMapping("/v1/api/brand-dashboard")
@RequiredArgsConstructor
public class BrandDashboardController {

    private final BrandDashboardService brandDashboardService;

    // ==========================================
    // Profile & Change Proposals
    // ==========================================

    @GetMapping("/profile")
    public ResponseEntity<ResponseDto<BrandProfileResponseDto>> getBrandProfile(Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(brandDashboardService.getBrandProfile(email));
    }

    @PostMapping("/update-request")
    public ResponseEntity<ResponseDto<BrandUpdateRequestResponseDto>> submitUpdateRequest(
            Principal principal,
            @Valid @RequestBody CreateBrandUpdateRequestDto request) {
        String email = principal.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(brandDashboardService.submitUpdateRequest(email, request));
    }

    // ==========================================
    // Team Management (BRAND_OWNER only)
    // ==========================================

    @GetMapping("/members")
    public ResponseEntity<ResponseDto<List<BrandMemberResponseDto>>> getBrandMembers(Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(brandDashboardService.getBrandMembers(email));
    }

    @PostMapping("/members")
    public ResponseEntity<ResponseDto<BrandMemberResponseDto>> addBrandMember(
            Principal principal,
            @Valid @RequestBody AddBrandMemberRequestDto request) {
        String email = principal.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(brandDashboardService.addBrandMember(email, request));
    }

    @PutMapping("/members/{memberId}/role")
    public ResponseEntity<ResponseDto<BrandMemberResponseDto>> updateBrandMemberRole(
            Principal principal,
            @PathVariable UUID memberId,
            @Valid @RequestBody UpdateBrandMemberRoleRequestDto request) {
        String email = principal.getName();
        return ResponseEntity.ok(brandDashboardService.updateBrandMemberRole(email, memberId, request));
    }

    @DeleteMapping("/members/{memberId}")
    public ResponseEntity<ResponseDto<Void>> removeBrandMember(
            Principal principal,
            @PathVariable UUID memberId) {
        String email = principal.getName();
        return ResponseEntity.ok(brandDashboardService.removeBrandMember(email, memberId));
    }

    // ==========================================
    // Products & Stock Management
    // ==========================================

    @GetMapping("/products")
    public ResponseEntity<ResponseDto<PaginationPayload<ProductResponseDto>>> getBrandProducts(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String email = principal.getName();
        return ResponseEntity.ok(brandDashboardService.getBrandProducts(email, page, size));
    }

    @PostMapping("/products/quick-create")
    public ResponseEntity<ResponseDto<QuickCreateProductResponseDto>> quickCreateProduct(
            Principal principal,
            @Valid @RequestBody QuickCreateProductRequestDto request) {
        String email = principal.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(brandDashboardService.quickCreateProduct(email, request));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ResponseDto<ProductResponseDto>> updateBrandProduct(
            Principal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBrandProductRequestDto request) {
        String email = principal.getName();
        return ResponseEntity.ok(brandDashboardService.updateBrandProduct(email, id, request));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<ResponseDto<Void>> deleteBrandProduct(
            Principal principal,
            @PathVariable UUID id) {
        String email = principal.getName();
        return ResponseEntity.ok(brandDashboardService.deleteBrandProduct(email, id));
    }

    // ==========================================
    // Brand Posts & Marketing
    // ==========================================

    @GetMapping("/posts")
    public ResponseEntity<ResponseDto<PaginationPayload<BrandPostResponseDto>>> getBrandPosts(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String email = principal.getName();
        return ResponseEntity.ok(brandDashboardService.getBrandPosts(email, page, size));
    }

    @PostMapping("/posts")
    public ResponseEntity<ResponseDto<BrandPostResponseDto>> createBrandPost(
            Principal principal,
            @Valid @RequestBody CreateBrandPostRequestDto request) {
        String email = principal.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(brandDashboardService.createBrandPost(email, request));
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<ResponseDto<BrandPostResponseDto>> updateBrandPost(
            Principal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBrandPostRequestDto request) {
        String email = principal.getName();
        return ResponseEntity.ok(brandDashboardService.updateBrandPost(email, id, request));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<ResponseDto<Void>> deleteBrandPost(
            Principal principal,
            @PathVariable UUID id) {
        String email = principal.getName();
        return ResponseEntity.ok(brandDashboardService.deleteBrandPost(email, id));
    }
}
