package com.amazon.service;

import com.amazon.dtos.category.request.CreateCategoryRequestDto;
import com.amazon.dtos.category.response.CategoryResponseDto;
import com.amazon.payloads.ResponseDto;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Category domain operations.
 */
public interface CategoryService {

    ResponseDto<CategoryResponseDto> createCategory(CreateCategoryRequestDto request);

    ResponseDto<CategoryResponseDto> createBrandCategory(CreateCategoryRequestDto request);

    ResponseDto<CategoryResponseDto> approveCategory(UUID id);

    ResponseDto<CategoryResponseDto> rejectCategory(UUID id, String reason);

    ResponseDto<List<CategoryResponseDto>> getPendingCategories();

    ResponseDto<CategoryResponseDto> getCategoryById(UUID id);

    ResponseDto<CategoryResponseDto> getCategoryBySlug(String slug);

    ResponseDto<List<CategoryResponseDto>> getAllRootCategories();

    ResponseDto<List<CategoryResponseDto>> getAllCategoriesForAdmin(Boolean isApproved);

    ResponseDto<CategoryResponseDto> getCategoryByIdForAdmin(UUID id);
}
