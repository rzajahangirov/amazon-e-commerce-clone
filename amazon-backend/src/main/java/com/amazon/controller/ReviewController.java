package com.amazon.controller;

import com.amazon.dtos.review.request.CreateReviewRequestDto;
import com.amazon.dtos.review.request.UpdateReviewRequestDto;
import com.amazon.dtos.review.response.ReviewResponseDto;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;
import com.amazon.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

/**
 * REST controller for Product Reviews and Ratings.
 * Strictly adheres to Senior Backend Developer Guidelines (Principal pattern, zero entity leakage).
 */
@RestController
@RequestMapping("/v1/api/products/{productId}/reviews")
@RequiredArgsConstructor
@Tag(name = "Product Reviews", description = "Endpoints for submitting, managing, and viewing product reviews and ratings")
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit a product review", description = "Allows an authenticated customer to submit a 1-5 star rating and optional review")
    public ResponseEntity<ResponseDto<ReviewResponseDto>> createReview(
            @PathVariable UUID productId,
            @Valid @RequestBody CreateReviewRequestDto request,
            Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required to submit a review");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(principal.getName(), productId, request));
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    @Operation(summary = "Get product reviews", description = "Retrieves a paginated list of reviews for a specific product")
    public ResponseEntity<ResponseDto<PaginationPayload<ReviewResponseDto>>> getProductReviews(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId, page, size));
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update a review", description = "Allows the review author or an ADMIN to update rating and comment")
    public ResponseEntity<ResponseDto<ReviewResponseDto>> updateReview(
            @PathVariable UUID productId,
            @PathVariable UUID reviewId,
            @Valid @RequestBody UpdateReviewRequestDto request,
            Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required to update a review");
        }
        return ResponseEntity.ok(reviewService.updateReview(principal.getName(), productId, reviewId, request));
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete a review", description = "Allows the review author or an ADMIN to delete a review")
    public ResponseEntity<ResponseDto<Void>> deleteReview(
            @PathVariable UUID productId,
            @PathVariable UUID reviewId,
            Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required to delete a review");
        }
        return ResponseEntity.ok(reviewService.deleteReview(principal.getName(), productId, reviewId));
    }

    @GetMapping("/{reviewId}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Get review by ID", description = "Retrieves details of a specific review")
    public ResponseEntity<ResponseDto<ReviewResponseDto>> getReviewById(
            @PathVariable UUID productId,
            @PathVariable UUID reviewId) {
        return ResponseEntity.ok(reviewService.getReviewById(productId, reviewId));
    }

    @GetMapping("/my-review")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user's review", description = "Retrieves the authenticated user's review for this product if one exists")
    public ResponseEntity<ResponseDto<ReviewResponseDto>> getMyReviewForProduct(
            @PathVariable UUID productId,
            Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required to fetch user review");
        }
        return ResponseEntity.ok(reviewService.getMyReviewForProduct(principal.getName(), productId));
    }
}
