package com.amazon.service.impl;

import com.amazon.dtos.category.request.CreateCategoryRequestDto;
import com.amazon.dtos.category.response.CategoryResponseDto;
import com.amazon.entity.Category;
import com.amazon.exception.DuplicateResourceException;
import com.amazon.exception.ResourceNotFoundException;
import com.amazon.payloads.ApiResponse;
import com.amazon.payloads.CatalogError;
import com.amazon.payloads.ResponseDto;
import com.amazon.repository.CategoryRepository;
import com.amazon.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service implementation for managing product taxonomy categories.
 * Strictly adheres to Senior Backend Developer Guidelines.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public ResponseDto<CategoryResponseDto> createCategory(CreateCategoryRequestDto request) {
        String slug = request.getSlug().toLowerCase().trim();
        if (categoryRepository.existsBySlug(slug)) {
            throw new DuplicateResourceException(CatalogError.CATEGORY_SLUG_EXISTS.getMessage());
        }

        Category parent = null;
        int level = 0;

        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(CatalogError.PARENT_CATEGORY_NOT_FOUND.getMessage()));
            level = parent.getLevel() + 1;
        } else if (request.getLevel() != null) {
            level = request.getLevel();
        }

        Category category = Category.builder()
                .name(request.getName().trim())
                .slug(slug)
                .parent(parent)
                .level(level)
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Category created successfully with id: {} and slug: {}", saved.getId(), saved.getSlug());

        return ApiResponse.success(mapToResponseDto(saved), "Category created successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<CategoryResponseDto> getCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.CATEGORY_NOT_FOUND.getMessage()));

        return ApiResponse.success(mapToResponseDto(category), "Category retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<CategoryResponseDto> getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.CATEGORY_NOT_FOUND.getMessage()));

        return ApiResponse.success(mapToResponseDto(category), "Category retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<List<CategoryResponseDto>> getAllRootCategories() {
        List<Category> rootCategories = categoryRepository.findRootCategoriesWithChildren();
        List<CategoryResponseDto> responseDtos = rootCategories.stream()
                .map(this::mapToResponseDtoWithChildren)
                .toList();

        return ApiResponse.success(responseDtos, "Root categories retrieved successfully");
    }

    private CategoryResponseDto mapToResponseDto(Category category) {
        return CategoryResponseDto.builder()
                .id(category.getId())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .name(category.getName())
                .slug(category.getSlug())
                .level(category.getLevel())
                .createdAt(category.getCreatedAt())
                .build();
    }

    private CategoryResponseDto mapToResponseDtoWithChildren(Category category) {
        List<CategoryResponseDto> subDtos = category.getSubCategories() != null
                ? category.getSubCategories().stream().map(this::mapToResponseDto).toList()
                : List.of();

        return CategoryResponseDto.builder()
                .id(category.getId())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .name(category.getName())
                .slug(category.getSlug())
                .level(category.getLevel())
                .subCategories(subDtos)
                .createdAt(category.getCreatedAt())
                .build();
    }
}
