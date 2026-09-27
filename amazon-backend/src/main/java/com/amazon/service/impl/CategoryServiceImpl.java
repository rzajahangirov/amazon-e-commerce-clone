package com.amazon.service.impl;

import com.amazon.dtos.category.request.CreateCategoryRequestDto;
import com.amazon.dtos.category.response.CategoryResponseDto;
import com.amazon.entity.Category;
import com.amazon.exception.BusinessRuleException;
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

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Service implementation for managing product taxonomy categories.
 * Enforces automated SEO URL generation, duplicate validations, and dual-endpoint approval flow.
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
        Category saved = createCategoryInternal(request, true);
        log.info("Platform admin category created successfully with id: {} and slug: {}", saved.getId(), saved.getSlug());
        return ApiResponse.success(mapToResponseDto(saved), "Category created successfully");
    }

    @Override
    @Transactional
    public ResponseDto<CategoryResponseDto> createBrandCategory(CreateCategoryRequestDto request) {
        Category saved = createCategoryInternal(request, false);
        log.info("Brand proposed category created successfully with id: {} and slug: {}, pending admin approval",
                saved.getId(), saved.getSlug());
        return ApiResponse.success(mapToResponseDto(saved), "Category proposal submitted for admin approval");
    }

    @Override
    @Transactional
    public ResponseDto<CategoryResponseDto> approveCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.CATEGORY_NOT_FOUND.getMessage()));

        category.setIsApproved(true);
        category.setRejectionReason(null);
        Category saved = categoryRepository.save(category);
        log.info("Category approved by platform admin: id={}, name={}, slug={}", saved.getId(), saved.getName(), saved.getSlug());

        return ApiResponse.success(mapToResponseDto(saved), "Category approved successfully");
    }

    @Override
    @Transactional
    public ResponseDto<CategoryResponseDto> rejectCategory(UUID id, String reason) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.CATEGORY_NOT_FOUND.getMessage()));
        if (Boolean.TRUE.equals(category.getIsApproved())) {
            throw new BusinessRuleException("Approved categories cannot be rejected");
        }
        category.setRejectionReason(reason == null || reason.isBlank() ? null : reason.trim());
        Category saved = categoryRepository.save(category);
        return ApiResponse.success(mapToResponseDto(saved), "Category rejected successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<List<CategoryResponseDto>> getPendingCategories() {
        List<CategoryResponseDto> pending = categoryRepository.findByIsApprovedFalseAndRejectionReasonIsNull()
                .stream().map(this::mapToResponseDto).toList();
        return ApiResponse.success(pending, "Pending categories retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<CategoryResponseDto> getCategoryById(UUID id) {
        Category category = categoryRepository.findByIdAndIsApprovedTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.CATEGORY_NOT_FOUND.getMessage()));

        return ApiResponse.success(mapToResponseDto(category), "Category retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<CategoryResponseDto> getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlugAndIsApprovedTrue(slug.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.CATEGORY_NOT_FOUND.getMessage()));

        return ApiResponse.success(mapToResponseDto(category), "Category retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<List<CategoryResponseDto>> getAllRootCategories() {
        List<Category> rootCategories = categoryRepository.findApprovedRootCategoriesWithChildren();
        List<CategoryResponseDto> responseDtos = rootCategories.stream()
                .map(this::mapToResponseDtoWithChildren)
                .toList();

        return ApiResponse.success(responseDtos, "Root categories retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<List<CategoryResponseDto>> getAllCategoriesForAdmin(Boolean isApproved) {
        List<Category> categories = isApproved != null
                ? categoryRepository.findByIsApproved(isApproved)
                : categoryRepository.findAll();

        List<CategoryResponseDto> responseDtos = categories.stream()
                .map(this::mapToResponseDto)
                .toList();

        return ApiResponse.success(responseDtos, "Categories retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<CategoryResponseDto> getCategoryByIdForAdmin(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.CATEGORY_NOT_FOUND.getMessage()));

        return ApiResponse.success(mapToResponseDto(category), "Category retrieved successfully");
    }

    private Category createCategoryInternal(CreateCategoryRequestDto request, boolean isApproved) {
        String name = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException(CatalogError.CATEGORY_NAME_EXISTS.getMessage());
        }

        String rawSlugSource = (request.getSlug() != null && !request.getSlug().isBlank())
                ? request.getSlug()
                : name;
        String slug = generateSlug(rawSlugSource);

        if (categoryRepository.existsBySlug(slug)) {
            throw new DuplicateResourceException(CatalogError.CATEGORY_SLUG_EXISTS.getMessage());
        }

        Category parent = null;
        int level = 0;

        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(CatalogError.PARENT_CATEGORY_NOT_FOUND.getMessage()));
            if (!Boolean.TRUE.equals(parent.getIsApproved())) {
                throw new BusinessRuleException("Cannot set unapproved category as parent");
            }
            level = parent.getLevel() + 1;
        } else if (request.getLevel() != null) {
            level = request.getLevel();
        }

        Category category = Category.builder()
                .name(name)
                .slug(slug)
                .parent(parent)
                .level(level)
                .commercialJustification(request.getCommercialJustification())
                .isApproved(isApproved)
                .build();

        return categoryRepository.save(category);
    }

    public static String generateSlug(String input) {
        if (input == null || input.isBlank()) {
            throw new BusinessRuleException("Input text cannot be blank for slug generation");
        }
        String normalized = Normalizer.normalize(input.trim(), Normalizer.Form.NFD);
        String slug = normalized
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ENGLISH)
                .replaceAll("&", "and")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        if (slug.isBlank()) {
            throw new BusinessRuleException("Unable to generate valid slug from: " + input);
        }
        return slug;
    }

    private CategoryResponseDto mapToResponseDto(Category category) {
        return CategoryResponseDto.builder()
                .id(category.getId())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .name(category.getName())
                .slug(category.getSlug())
                .level(category.getLevel())
                .commercialJustification(category.getCommercialJustification())
                .isApproved(category.getIsApproved())
                .rejectionReason(category.getRejectionReason())
                .createdAt(category.getCreatedAt())
                .build();
    }

    private CategoryResponseDto mapToResponseDtoWithChildren(Category category) {
        List<CategoryResponseDto> subDtos = category.getSubCategories() != null
                ? category.getSubCategories().stream()
                        .filter(sub -> Boolean.TRUE.equals(sub.getIsApproved()))
                        .map(this::mapToResponseDto)
                        .toList()
                : List.of();

        return CategoryResponseDto.builder()
                .id(category.getId())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .name(category.getName())
                .slug(category.getSlug())
                .level(category.getLevel())
                .commercialJustification(category.getCommercialJustification())
                .isApproved(category.getIsApproved())
                .rejectionReason(category.getRejectionReason())
                .subCategories(subDtos)
                .createdAt(category.getCreatedAt())
                .build();
    }
}
