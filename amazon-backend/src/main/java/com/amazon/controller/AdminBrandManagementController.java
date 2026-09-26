package com.amazon.controller;

import com.amazon.dtos.brand.request.RejectApplicationRequestDto;
import com.amazon.dtos.brand.request.RejectUpdateRequestDto;
import com.amazon.dtos.brand.response.BrandApplicationResponseDto;
import com.amazon.dtos.brand.response.BrandResponseDto;
import com.amazon.dtos.brand.response.BrandUpdateRequestResponseDto;
import com.amazon.enums.BrandApplicationStatus;
import com.amazon.enums.BrandUpdateRequestStatus;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;
import com.amazon.service.AdminBrandManagementService;
import com.amazon.service.BrandApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for Platform Administrator Brand Governance.
 * Protected with ROLE_ADMIN authorization.
 * Adheres to Senior Backend Developer Guidelines Section 1, 3, 4, 10.3.
 */
@RestController
@RequestMapping("/v1/api/admin/brand-management")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminBrandManagementController {

    private final BrandApplicationService brandApplicationService;
    private final AdminBrandManagementService adminBrandManagementService;

    // ==========================================
    // Applications Review
    // ==========================================

    @GetMapping("/applications")
    public ResponseEntity<ResponseDto<PaginationPayload<BrandApplicationResponseDto>>> getApplications(
            @RequestParam(required = false) BrandApplicationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(brandApplicationService.getApplications(status, page, size));
    }

    @PostMapping("/applications/{id}/approve")
    public ResponseEntity<ResponseDto<BrandResponseDto>> approveApplication(@PathVariable UUID id) {
        return ResponseEntity.ok(brandApplicationService.approveApplication(id));
    }

    @PostMapping("/applications/{id}/reject")
    public ResponseEntity<ResponseDto<BrandApplicationResponseDto>> rejectApplication(
            @PathVariable UUID id,
            @Valid @RequestBody RejectApplicationRequestDto request) {
        return ResponseEntity.ok(brandApplicationService.rejectApplication(id, request));
    }

    // ==========================================
    // Brand Update Requests
    // ==========================================

    @GetMapping("/update-requests")
    public ResponseEntity<ResponseDto<PaginationPayload<BrandUpdateRequestResponseDto>>> getUpdateRequests(
            @RequestParam(required = false) BrandUpdateRequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminBrandManagementService.getUpdateRequests(status, page, size));
    }

    @PostMapping("/update-requests/{id}/approve")
    public ResponseEntity<ResponseDto<BrandResponseDto>> approveUpdateRequest(@PathVariable UUID id) {
        return ResponseEntity.ok(adminBrandManagementService.approveUpdateRequest(id));
    }

    @PostMapping("/update-requests/{id}/reject")
    public ResponseEntity<ResponseDto<BrandUpdateRequestResponseDto>> rejectUpdateRequest(
            @PathVariable UUID id,
            @Valid @RequestBody RejectUpdateRequestDto request) {
        return ResponseEntity.ok(adminBrandManagementService.rejectUpdateRequest(id, request));
    }

    // ==========================================
    // Brands Directory & Lifecycle
    // ==========================================

    @GetMapping("/brands")
    public ResponseEntity<ResponseDto<PaginationPayload<BrandResponseDto>>> getAllBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminBrandManagementService.getAllBrands(page, size));
    }

    @DeleteMapping("/brands/{id}")
    public ResponseEntity<ResponseDto<Void>> deleteBrand(@PathVariable UUID id) {
        return ResponseEntity.ok(adminBrandManagementService.deleteBrand(id));
    }
}
