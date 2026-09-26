package com.amazon.service;

import com.amazon.dtos.listing.request.CreateListingRequestDto;
import com.amazon.dtos.listing.request.UpdateListingStockRequestDto;
import com.amazon.dtos.listing.response.ProductListingResponseDto;
import com.amazon.payloads.ResponseDto;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for ProductListing (seller offer) operations.
 */
public interface ProductListingService {

    ResponseDto<ProductListingResponseDto> createListing(CreateListingRequestDto request, String sellerEmail);

    ResponseDto<ProductListingResponseDto> getListingById(UUID id);

    ResponseDto<ProductListingResponseDto> updateStock(UUID listingId, UpdateListingStockRequestDto request, String sellerEmail);

    ResponseDto<List<ProductListingResponseDto>> getListingsByVariantId(UUID variantId);

    ResponseDto<ProductListingResponseDto> getBuyboxWinner(UUID variantId);

    ResponseDto<ProductListingResponseDto> setBuyboxWinner(UUID listingId);
}
