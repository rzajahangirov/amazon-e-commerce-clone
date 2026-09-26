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
 * REST controller for Product Category taxonomy endpoints.
 * Strictly adheres to Senior Backend Developer Guidelines.
 */
@RestController
@RequestMapping("v1/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Product category taxonomy management")
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Operation(summary = "Create category", description = "Creates a new category in the catalog taxonomy")
    public ResponseEntity<ResponseDto<CategoryResponseDto>> createCategory(
            @Valid @RequestBody CreateCategoryRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieves category information by UUID")
    public ResponseEntity<ResponseDto<CategoryResponseDto>> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get category by slug", description = "Retrieves category information by URL slug")
    public ResponseEntity<ResponseDto<CategoryResponseDto>> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getCategoryBySlug(slug));
    }

    @GetMapping
    @Operation(summary = "Get root categories", description = "Retrieves all root categories with their subcategory hierarchy")
    public ResponseEntity<ResponseDto<List<CategoryResponseDto>>> getAllRootCategories() {
        return ResponseEntity.ok(categoryService.getAllRootCategories());
    }
}
