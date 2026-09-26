package com.amazon.controller;

import com.amazon.dtos.category.request.CreateCategoryRequestDto;
import com.amazon.dtos.category.response.CategoryResponseDto;
import com.amazon.payloads.ResponseDto;
import com.amazon.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Platform Administrator Category Taxonomy Governance.
 * Protected with ROLE_ADMIN authorization.
 * Adheres to Senior Backend Developer Guidelines Section 1, 3, 4, 10.3.
 */
@RestController
@RequestMapping("/v1/api/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Categories", description = "Platform administrator category governance endpoints")
@Slf4j
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Create category as platform admin", description = "Creates an approved category immediately active across the platform")
    public ResponseEntity<ResponseDto<CategoryResponseDto>> createCategory(
            @Valid @RequestBody CreateCategoryRequestDto request) {
        log.info("Admin creating platform category: {}", request.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(request));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve category proposal", description = "Approves a brand-proposed category making it active platform-wide")
    public ResponseEntity<ResponseDto<CategoryResponseDto>> approveCategoryPost(@PathVariable UUID id) {
        log.info("Admin approving category id: {}", id);
        return ResponseEntity.ok(categoryService.approveCategory(id));
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Approve category proposal (PATCH)", description = "Approves a brand-proposed category making it active platform-wide")
    public ResponseEntity<ResponseDto<CategoryResponseDto>> approveCategoryPatch(@PathVariable UUID id) {
        log.info("Admin approving category id via PATCH: {}", id);
        return ResponseEntity.ok(categoryService.approveCategory(id));
    }

    @GetMapping
    @Operation(summary = "Get all categories for admin", description = "Retrieves all categories with optional approval status filter")
    public ResponseEntity<ResponseDto<List<CategoryResponseDto>>> getAllCategories(
            @RequestParam(required = false) Boolean isApproved) {
        return ResponseEntity.ok(categoryService.getAllCategoriesForAdmin(isApproved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID for admin", description = "Retrieves any category by ID regardless of approval status")
    public ResponseEntity<ResponseDto<CategoryResponseDto>> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getCategoryByIdForAdmin(id));
    }
}
