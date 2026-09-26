package com.amazon.service;

import com.amazon.dtos.review.request.CreateReviewRequestDto;
import com.amazon.dtos.review.request.UpdateReviewRequestDto;
import com.amazon.dtos.review.response.ReviewResponseDto;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;

import java.util.UUID;

/**
 * Service interface for Product Review and 1-5 Star Rating operations.
 * Strictly adheres to Senior Backend Developer Guidelines (zero entity leakage).
 */
public interface ReviewService {

    /**
     * Submits a rating (1-5) and an optional written review for a product.
     * Enforces one review per user per product.
     */
    ResponseDto<ReviewResponseDto> createReview(String callerEmail, UUID productId, CreateReviewRequestDto request);

    /**
     * Retrieves a paginated list of reviews for a specific product.
     */
    ResponseDto<PaginationPayload<ReviewResponseDto>> getProductReviews(UUID productId, int page, int size);

    /**
     * Updates an existing review/rating by its author or an ADMIN.
     */
    ResponseDto<ReviewResponseDto> updateReview(String callerEmail, UUID productId, UUID reviewId, UpdateReviewRequestDto request);

    /**
     * Deletes an existing review by its author or an ADMIN.
     */
    ResponseDto<Void> deleteReview(String callerEmail, UUID productId, UUID reviewId);

    /**
     * Retrieves a single review by its ID and productId.
     */
    ResponseDto<ReviewResponseDto> getReviewById(UUID productId, UUID reviewId);

    /**
     * Retrieves the authenticated user's review for a given product if it exists.
     */
    ResponseDto<ReviewResponseDto> getMyReviewForProduct(String callerEmail, UUID productId);
}
