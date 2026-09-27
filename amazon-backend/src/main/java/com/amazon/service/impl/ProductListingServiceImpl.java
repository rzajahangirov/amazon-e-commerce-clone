package com.amazon.service.impl;

import com.amazon.dtos.listing.request.CreateListingRequestDto;
import com.amazon.dtos.listing.request.UpdateListingStockRequestDto;
import com.amazon.dtos.listing.response.ProductListingResponseDto;
import com.amazon.entity.ProductListing;
import com.amazon.entity.ProductVariant;
import com.amazon.entity.User;
import com.amazon.entity.Brand;
import com.amazon.enums.FulfillmentType;
import com.amazon.enums.ListingStatus;
import com.amazon.exception.BusinessRuleException;
import com.amazon.exception.ResourceNotFoundException;
import com.amazon.payloads.ApiResponse;
import com.amazon.payloads.AuthError;
import com.amazon.payloads.CatalogError;
import com.amazon.payloads.ResponseDto;
import com.amazon.repository.ProductListingRepository;
import com.amazon.repository.ProductVariantRepository;
import com.amazon.repository.UserRepository;
import com.amazon.repository.SellerProfileRepository;
import com.amazon.service.ProductListingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service implementation for managing seller product listings.
 * Strictly adheres to Senior Backend Developer Guidelines.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductListingServiceImpl implements ProductListingService {

    private final ProductListingRepository productListingRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;

    @Override
    @Transactional
    public ResponseDto<ProductListingResponseDto> createListing(CreateListingRequestDto request, String sellerEmail) {
        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new ResourceNotFoundException(AuthError.USER_NOT_FOUND.getMessage()));

        ProductVariant variant = productVariantRepository.findByIdForUpdate(request.getProductVariantId())
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.VARIANT_NOT_FOUND.getMessage()));

        if (variant.getProduct().getBrand() == null
                || variant.getProduct().getBrand().getStatus() != com.amazon.enums.BrandStatus.ACTIVE
                || variant.getProduct().getStatus() != com.amazon.enums.ProductStatus.ACTIVE
                || !Boolean.TRUE.equals(variant.getProduct().getCategory().getIsApproved())) {
            throw new BusinessRuleException("Seller offers can only be attached to an active brand catalog ASIN in an approved category");
        }

        List<ProductListing> existingListings = productListingRepository.findByProductVariantId(variant.getId());
        boolean isBrandOffer = isBrandOffer(seller, variant.getProduct().getBrand());
        boolean hasBuyboxWinner = existingListings.stream().anyMatch(listing -> Boolean.TRUE.equals(listing.getIsBuyboxWinner()));
        boolean isBuyboxWinner = request.getStatus() != ListingStatus.INACTIVE
                && (isBrandOffer || !hasBuyboxWinner);

        if (isBrandOffer && isBuyboxWinner) {
            existingListings.stream().filter(listing -> Boolean.TRUE.equals(listing.getIsBuyboxWinner()))
                    .forEach(listing -> listing.setIsBuyboxWinner(false));
            productListingRepository.saveAll(existingListings);
        }

        ProductListing listing = ProductListing.builder()
                .productVariant(variant)
                .seller(seller)
                .sellerSku(request.getSellerSku().trim())
                .price(request.getPrice())
                .minPriceFloor(request.getMinPriceFloor())
                .stockQuantity(request.getStockQuantity())
                .fulfillmentType(request.getFulfillmentType() != null ? request.getFulfillmentType() : FulfillmentType.FBM)
                .status(request.getStatus() != null ? request.getStatus() : ListingStatus.ACTIVE)
                .isBuyboxWinner(isBuyboxWinner)
                .build();

        ProductListing saved = productListingRepository.save(listing);
        log.info("Product listing created successfully with id: {} by seller: {}", saved.getId(), sellerEmail);

        return ApiResponse.success(mapToResponseDto(saved), "Listing created successfully");
    }

    private boolean isBrandOffer(User seller, Brand brand) {
        if (brand.getOwnerUser() != null && brand.getOwnerUser().getId().equals(seller.getId())) {
            return true;
        }
        return sellerProfileRepository.findByUserId(seller.getId())
                .map(profile -> profile.getBrand() != null && profile.getBrand().getId().equals(brand.getId()))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<ProductListingResponseDto> getListingById(UUID id) {
        ProductListing listing = productListingRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.LISTING_NOT_FOUND.getMessage()));

        return ApiResponse.success(mapToResponseDto(listing), "Listing retrieved successfully");
    }

    @Override
    @Transactional
    public ResponseDto<ProductListingResponseDto> updateStock(
            UUID listingId, UpdateListingStockRequestDto request, String sellerEmail) {
        ProductListing listing = productListingRepository.findWithDetailsById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.LISTING_NOT_FOUND.getMessage()));

        User user = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new ResourceNotFoundException(AuthError.USER_NOT_FOUND.getMessage()));

        boolean isSeller = listing.getSeller().getId().equals(user.getId());
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        if (!isSeller && !isAdmin) {
            throw new BusinessRuleException(CatalogError.SELLER_NOT_AUTHORIZED.getMessage());
        }

        listing.setStockQuantity(request.getStockQuantity());
        if (request.getPrice() != null) {
            listing.setPrice(request.getPrice());
        }
        if (request.getMinPriceFloor() != null) {
            listing.setMinPriceFloor(request.getMinPriceFloor());
        }

        ProductListing updated = productListingRepository.save(listing);
        log.info("Stock updated for listing id: {} to quantity: {} by: {}", listingId, request.getStockQuantity(), sellerEmail);

        return ApiResponse.success(mapToResponseDto(updated), "Listing stock updated successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<List<ProductListingResponseDto>> getListingsByVariantId(UUID variantId) {
        List<ProductListing> listings = productListingRepository.findByProductVariantId(variantId);
        List<ProductListingResponseDto> responseDtos = listings.stream()
                .map(this::mapToResponseDto)
                .toList();

        return ApiResponse.success(responseDtos, "Listings retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<ProductListingResponseDto> getBuyboxWinner(UUID variantId) {
        ProductListing winner = productListingRepository.findByProductVariantIdAndIsBuyboxWinnerTrue(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("No Buy Box winner found for variant"));

        return ApiResponse.success(mapToResponseDto(winner), "Buy Box winner retrieved successfully");
    }

    @Override
    @Transactional
    public ResponseDto<ProductListingResponseDto> setBuyboxWinner(UUID listingId) {
        ProductListing targetListing = productListingRepository.findWithDetailsById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.LISTING_NOT_FOUND.getMessage()));

        UUID variantId = targetListing.getProductVariant().getId();
        List<ProductListing> listings = productListingRepository.findByProductVariantId(variantId);

        for (ProductListing listing : listings) {
            if (listing.getId().equals(listingId)) {
                listing.setIsBuyboxWinner(true);
            } else if (Boolean.TRUE.equals(listing.getIsBuyboxWinner())) {
                listing.setIsBuyboxWinner(false);
            }
        }

        productListingRepository.saveAll(listings);
        log.info("New Buy Box winner set for variant: {} -> listing: {}", variantId, listingId);

        return ApiResponse.success(mapToResponseDto(targetListing), "Buy Box winner updated successfully");
    }

    private ProductListingResponseDto mapToResponseDto(ProductListing listing) {
        return ProductListingResponseDto.builder()
                .id(listing.getId())
                .productVariantId(listing.getProductVariant() != null ? listing.getProductVariant().getId() : null)
                .variantAsin(listing.getProductVariant() != null ? listing.getProductVariant().getAsin() : null)
                .variantName(listing.getProductVariant() != null ? listing.getProductVariant().getVariantName() : null)
                .sellerId(listing.getSeller() != null ? listing.getSeller().getId() : null)
                .sellerName(listing.getSeller() != null ? listing.getSeller().getFullName() : null)
                .sellerSku(listing.getSellerSku())
                .price(listing.getPrice())
                .minPriceFloor(listing.getMinPriceFloor())
                .stockQuantity(listing.getStockQuantity())
                .fulfillmentType(listing.getFulfillmentType())
                .isBuyboxWinner(listing.getIsBuyboxWinner())
                .status(listing.getStatus())
                .createdAt(listing.getCreatedAt())
                .build();
    }
}
