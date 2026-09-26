package com.amazon.service.impl;

import com.amazon.dtos.brand.request.CreateBrandApplicationRequestDto;
import com.amazon.dtos.brand.request.RejectApplicationRequestDto;
import com.amazon.dtos.brand.response.BrandApplicationResponseDto;
import com.amazon.dtos.brand.response.BrandResponseDto;
import com.amazon.entity.*;
import com.amazon.enums.BrandApplicationStatus;
import com.amazon.enums.BrandRole;
import com.amazon.enums.BrandStatus;
import com.amazon.enums.UserStatus;
import com.amazon.exception.BusinessRuleException;
import com.amazon.exception.DuplicateResourceException;
import com.amazon.exception.ResourceNotFoundException;
import com.amazon.payloads.ApiResponse;
import com.amazon.payloads.BrandError;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;
import com.amazon.repository.*;
import com.amazon.service.BrandApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

/**
 * Implementation of {@link BrandApplicationService} handling brand onboarding applications.
 * Adheres to Senior Backend Developer Guidelines Section 1, 5, 6, 8, 9.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BrandApplicationServiceImpl implements BrandApplicationService {

    private final BrandApplicationRepository brandApplicationRepository;
    private final BrandRepository brandRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final BrandMemberRepository brandMemberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ResponseDto<BrandApplicationResponseDto> submitApplication(CreateBrandApplicationRequestDto request) {
        if (brandRepository.existsBySlug(request.getBrandSlug())) {
            throw new DuplicateResourceException(BrandError.BRAND_SLUG_ALREADY_EXISTS.getMessage() + request.getBrandSlug());
        }

        if (brandApplicationRepository.existsByBrandSlug(request.getBrandSlug())) {
            throw new DuplicateResourceException("An application with brand slug already exists: " + request.getBrandSlug());
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        BrandApplication application = BrandApplication.builder()
                .applicantName(request.getApplicantName())
                .applicantEmail(request.getApplicantEmail())
                .passwordHash(encodedPassword)
                .applicantPhone(request.getApplicantPhone())
                .brandName(request.getBrandName())
                .brandSlug(request.getBrandSlug().toLowerCase().trim())
                .trademarkRegistrationNumber(request.getTrademarkRegistrationNumber())
                .brandCountry(request.getBrandCountry())
                .logoUrl(request.getLogoUrl())
                .aboutText(request.getAboutText())
                .status(BrandApplicationStatus.PENDING)
                .build();

        BrandApplication saved = brandApplicationRepository.save(application);
        log.info("Brand application submitted successfully with id: {} for brand: {}", saved.getId(), saved.getBrandName());

        return ApiResponse.success(mapToResponseDto(saved), "Brand application submitted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<PaginationPayload<BrandApplicationResponseDto>> getApplications(
            BrandApplicationStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BrandApplication> applicationPage = (status != null)
                ? brandApplicationRepository.findByStatus(status, pageable)
                : brandApplicationRepository.findAll(pageable);

        PaginationPayload<BrandApplicationResponseDto> payload = PaginationPayload.<BrandApplicationResponseDto>builder()
                .content(applicationPage.getContent().stream().map(this::mapToResponseDto).toList())
                .pageNumber(applicationPage.getNumber())
                .pageSize(applicationPage.getSize())
                .totalElements(applicationPage.getTotalElements())
                .totalPages(applicationPage.getTotalPages())
                .last(applicationPage.isLast())
                .build();

        return ApiResponse.success(payload, "Brand applications retrieved successfully");
    }

    @Override
    @Transactional
    public ResponseDto<BrandResponseDto> approveApplication(UUID applicationId) {
        BrandApplication application = brandApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(BrandError.APPLICATION_NOT_FOUND.getMessage() + applicationId));

        if (application.getStatus() != BrandApplicationStatus.PENDING) {
            throw new BusinessRuleException(BrandError.APPLICATION_ALREADY_PROCESSED.getMessage());
        }

        if (brandRepository.existsBySlug(application.getBrandSlug())) {
            throw new DuplicateResourceException(BrandError.BRAND_SLUG_ALREADY_EXISTS.getMessage() + application.getBrandSlug());
        }

        // 1. Resolve or create Roles
        Role brandOwnerRole = roleRepository.findByName("ROLE_BRAND_OWNER")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("ROLE_BRAND_OWNER")
                        .description("Brand Owner role with administrative brand privileges")
                        .build()));

        Role sellerRole = roleRepository.findByName("ROLE_SELLER")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("ROLE_SELLER")
                        .description("Seller role for inventory management")
                        .build()));

        // 2. Resolve or create User
        User user = userRepository.findByEmail(application.getApplicantEmail())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(application.getApplicantEmail())
                            .fullName(application.getApplicantName())
                            .passwordHash(application.getPasswordHash())
                            .phone(application.getApplicantPhone())
                            .status(UserStatus.ACTIVE)
                            .roles(new HashSet<>())
                            .build();
                    return userRepository.save(newUser);
                });

        user.addRole(brandOwnerRole);
        user.addRole(sellerRole);
        User savedUser = userRepository.save(user);

        // 3. Create Brand
        Brand brand = Brand.builder()
                .name(application.getBrandName())
                .slug(application.getBrandSlug())
                .trademarkRegistrationNumber(application.getTrademarkRegistrationNumber())
                .brandCountry(application.getBrandCountry())
                .logoUrl(application.getLogoUrl())
                .aboutText(application.getAboutText())
                .status(BrandStatus.ACTIVE)
                .ownerUser(savedUser)
                .build();
        Brand savedBrand = brandRepository.save(brand);

        // 4. Create or link SellerProfile
        SellerProfile sellerProfile = sellerProfileRepository.findByUserId(savedUser.getId())
                .orElseGet(() -> SellerProfile.builder()
                        .user(savedUser)
                        .build());
        sellerProfile.setBrand(savedBrand);
        sellerProfile.setStoreName(savedBrand.getName());
        sellerProfile.setIsVerified(true);
        SellerProfile savedSellerProfile = sellerProfileRepository.save(sellerProfile);
        savedUser.setSellerProfile(savedSellerProfile);
        savedBrand.getSellerProfiles().add(savedSellerProfile);

        // 5. Assign BrandMember as BRAND_OWNER
        BrandMember brandMember = BrandMember.builder()
                .brand(savedBrand)
                .user(savedUser)
                .brandRole(BrandRole.BRAND_OWNER)
                .assignedAt(LocalDateTime.now())
                .build();
        BrandMember savedBrandMember = brandMemberRepository.save(brandMember);
        savedUser.getBrandMembers().add(savedBrandMember);
        savedBrand.getMembers().add(savedBrandMember);
        savedUser.getOwnedBrands().add(savedBrand);

        // 6. Update application status
        application.setStatus(BrandApplicationStatus.APPROVED);
        brandApplicationRepository.save(application);

        log.info("Brand application approved: id={}, brand={}, owner={}",
                applicationId, savedBrand.getName(), savedUser.getEmail());

        return ApiResponse.success(mapToBrandResponse(savedBrand), "Brand application approved successfully");
    }

    @Override
    @Transactional
    public ResponseDto<BrandApplicationResponseDto> rejectApplication(UUID applicationId, RejectApplicationRequestDto request) {
        BrandApplication application = brandApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(BrandError.APPLICATION_NOT_FOUND.getMessage() + applicationId));

        if (application.getStatus() != BrandApplicationStatus.PENDING) {
            throw new BusinessRuleException(BrandError.APPLICATION_ALREADY_PROCESSED.getMessage());
        }

        application.setStatus(BrandApplicationStatus.REJECTED);
        application.setRejectionReason(request.getRejectionReason());
        BrandApplication updated = brandApplicationRepository.save(application);

        log.info("Brand application rejected: id={}, reason={}", applicationId, request.getRejectionReason());

        return ApiResponse.success(mapToResponseDto(updated), "Brand application rejected successfully");
    }

    private BrandApplicationResponseDto mapToResponseDto(BrandApplication app) {
        return BrandApplicationResponseDto.builder()
                .id(app.getId())
                .applicantName(app.getApplicantName())
                .applicantEmail(app.getApplicantEmail())
                .applicantPhone(app.getApplicantPhone())
                .brandName(app.getBrandName())
                .brandSlug(app.getBrandSlug())
                .trademarkRegistrationNumber(app.getTrademarkRegistrationNumber())
                .brandCountry(app.getBrandCountry())
                .logoUrl(app.getLogoUrl())
                .aboutText(app.getAboutText())
                .status(app.getStatus())
                .rejectionReason(app.getRejectionReason())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
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
}
