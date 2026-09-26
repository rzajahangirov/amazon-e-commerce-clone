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

    ResponseDto<CategoryResponseDto> getCategoryById(UUID id);

    ResponseDto<CategoryResponseDto> getCategoryBySlug(String slug);

    ResponseDto<List<CategoryResponseDto>> getAllRootCategories();
}
