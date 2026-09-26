package com.amazon.service.impl;

import com.amazon.dtos.brand.request.RejectUpdateRequestDto;
import com.amazon.dtos.brand.response.BrandResponseDto;
import com.amazon.dtos.brand.response.BrandUpdateRequestResponseDto;
import com.amazon.entity.Brand;
import com.amazon.entity.BrandUpdateRequest;
import com.amazon.entity.ProductListing;
import com.amazon.enums.BrandStatus;
import com.amazon.enums.BrandUpdateRequestStatus;
import com.amazon.enums.ListingStatus;
import com.amazon.exception.BusinessRuleException;
import com.amazon.exception.ResourceNotFoundException;
import com.amazon.payloads.ApiResponse;
import com.amazon.payloads.BrandError;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;
import com.amazon.repository.BrandRepository;
import com.amazon.repository.BrandUpdateRequestRepository;
import com.amazon.repository.ProductListingRepository;
import com.amazon.service.AdminBrandManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link AdminBrandManagementService} for platform administrator governance.
 * Adheres to Senior Backend Developer Guidelines Section 1, 5, 6, 8, 9.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBrandManagementServiceImpl implements AdminBrandManagementService {

    private final BrandRepository brandRepository;
    private final BrandUpdateRequestRepository brandUpdateRequestRepository;
    private final ProductListingRepository productListingRepository;

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<PaginationPayload<BrandUpdateRequestResponseDto>> getUpdateRequests(
            BrandUpdateRequestStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BrandUpdateRequest> requestsPage = (status != null)
                ? brandUpdateRequestRepository.findByStatus(status, pageable)
                : brandUpdateRequestRepository.findAllWithDetails(pageable);

        PaginationPayload<BrandUpdateRequestResponseDto> payload = PaginationPayload.<BrandUpdateRequestResponseDto>builder()
                .content(requestsPage.getContent().stream().map(this::mapToUpdateRequestResponse).toList())
                .pageNumber(requestsPage.getNumber())
                .pageSize(requestsPage.getSize())
                .totalElements(requestsPage.getTotalElements())
                .totalPages(requestsPage.getTotalPages())
                .last(requestsPage.isLast())
                .build();

        return ApiResponse.success(payload, "Brand update requests retrieved successfully");
    }

    @Override
    @Transactional
    public ResponseDto<BrandResponseDto> approveUpdateRequest(UUID requestId) {
        BrandUpdateRequest request = brandUpdateRequestRepository.findWithDetailsById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(BrandError.UPDATE_REQUEST_NOT_FOUND.getMessage() + requestId));

        if (request.getStatus() != BrandUpdateRequestStatus.PENDING) {
            throw new BusinessRuleException(BrandError.UPDATE_REQUEST_ALREADY_PROCESSED.getMessage());
        }

        Brand brand = request.getBrand();

        if (request.getProposedBrandName() != null && !request.getProposedBrandName().isBlank()) {
            brand.setName(request.getProposedBrandName());
        }
        if (request.getProposedLogoUrl() != null && !request.getProposedLogoUrl().isBlank()) {
            brand.setLogoUrl(request.getProposedLogoUrl());
        }
        if (request.getProposedAboutText() != null && !request.getProposedAboutText().isBlank()) {
            brand.setAboutText(request.getProposedAboutText());
        }
        if (request.getProposedTrademarkNo() != null && !request.getProposedTrademarkNo().isBlank()) {
            brand.setTrademarkRegistrationNumber(request.getProposedTrademarkNo());
        }

        Brand savedBrand = brandRepository.save(brand);

        request.setStatus(BrandUpdateRequestStatus.APPROVED);
        brandUpdateRequestRepository.save(request);

        log.info("Brand update request approved: id={}, brandId={}", requestId, brand.getId());

        return ApiResponse.success(mapToBrandResponse(savedBrand), "Brand update request approved successfully");
    }

    @Override
    @Transactional
    public ResponseDto<BrandUpdateRequestResponseDto> rejectUpdateRequest(UUID requestId, RejectUpdateRequestDto requestDto) {
        BrandUpdateRequest request = brandUpdateRequestRepository.findWithDetailsById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(BrandError.UPDATE_REQUEST_NOT_FOUND.getMessage() + requestId));

        if (request.getStatus() != BrandUpdateRequestStatus.PENDING) {
            throw new BusinessRuleException(BrandError.UPDATE_REQUEST_ALREADY_PROCESSED.getMessage());
        }

        request.setStatus(BrandUpdateRequestStatus.REJECTED);
        request.setRejectionReason(requestDto.getRejectionReason());
        BrandUpdateRequest updated = brandUpdateRequestRepository.save(request);

        log.info("Brand update request rejected: id={}, reason={}", requestId, requestDto.getRejectionReason());

        return ApiResponse.success(mapToUpdateRequestResponse(updated), "Brand update request rejected successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<PaginationPayload<BrandResponseDto>> getAllBrands(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Brand> brandPage = brandRepository.findAll(pageable);

        PaginationPayload<BrandResponseDto> payload = PaginationPayload.<BrandResponseDto>builder()
                .content(brandPage.getContent().stream().map(this::mapToBrandResponse).toList())
                .pageNumber(brandPage.getNumber())
                .pageSize(brandPage.getSize())
                .totalElements(brandPage.getTotalElements())
                .totalPages(brandPage.getTotalPages())
                .last(brandPage.isLast())
                .build();

        return ApiResponse.success(payload, "Brands retrieved successfully");
    }

    @Override
    @Transactional
    public ResponseDto<Void> deleteBrand(UUID brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException(BrandError.BRAND_NOT_FOUND.getMessage() + brandId));

        // Soft-delete brand
        brand.setStatus(BrandStatus.SUSPENDED);
        brand.setDeletedAt(LocalDateTime.now());
        brandRepository.save(brand);

        // Suspend and soft-delete all listings of this brand
        List<ProductListing> listings = productListingRepository.findByBrandId(brandId);
        for (ProductListing listing : listings) {
            listing.setStatus(ListingStatus.INACTIVE);
            listing.setDeletedAt(LocalDateTime.now());
        }
        productListingRepository.saveAll(listings);

        log.info("Brand and associated listings soft-deleted: brandId={}, listingsCount={}", brandId, listings.size());

        return ApiResponse.success(null, "Brand and its listings suspended successfully");
    }

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
}
