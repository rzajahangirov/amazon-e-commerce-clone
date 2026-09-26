package com.amazon.service.impl;

import com.amazon.dtos.review.request.CreateReviewRequestDto;
import com.amazon.dtos.review.request.UpdateReviewRequestDto;
import com.amazon.dtos.review.response.ReviewResponseDto;
import com.amazon.entity.Product;
import com.amazon.entity.Review;
import com.amazon.entity.User;
import com.amazon.enums.ProductStatus;
import com.amazon.exception.BusinessRuleException;
import com.amazon.exception.DuplicateResourceException;
import com.amazon.exception.ResourceNotFoundException;
import com.amazon.payloads.*;
import com.amazon.repository.ProductRepository;
import com.amazon.repository.ReviewRepository;
import com.amazon.repository.UserRepository;
import com.amazon.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link ReviewService}.
 * Enforces one review per user per product, role-based authorization,
 * and dynamically calculates product rating metrics without null pointer issues.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ResponseDto<ReviewResponseDto> createReview(String callerEmail, UUID productId, CreateReviewRequestDto request) {
        User user = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new ResourceNotFoundException(AuthError.USER_NOT_FOUND.getMessage()));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.PRODUCT_NOT_FOUND.getMessage()));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BusinessRuleException(ReviewError.PRODUCT_NOT_ACTIVE.getMessage());
        }

        if (reviewRepository.existsByProductIdAndUserId(productId, user.getId())) {
            throw new DuplicateResourceException(ReviewError.DUPLICATE_REVIEW.getMessage());
        }

        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment() != null ? request.getComment().trim() : null)
                .build();

        Review savedReview = reviewRepository.saveAndFlush(review);
        recalculateProductRating(productId);

        log.info("User {} created review {} for product {} with rating {}", callerEmail, savedReview.getId(), productId, request.getRating());
        return ApiResponse.success(mapToResponseDto(savedReview), "Review submitted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<PaginationPayload<ReviewResponseDto>> getProductReviews(UUID productId, int page, int size) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException(CatalogError.PRODUCT_NOT_FOUND.getMessage());
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviewPage = reviewRepository.findByProductId(productId, pageRequest);

        List<ReviewResponseDto> content = reviewPage.getContent().stream()
                .map(this::mapToResponseDto)
                .toList();

        PaginationPayload<ReviewResponseDto> paginationPayload = PaginationPayload.<ReviewResponseDto>builder()
                .content(content)
                .pageNumber(reviewPage.getNumber())
                .pageSize(reviewPage.getSize())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .last(reviewPage.isLast())
                .build();

        return ApiResponse.success(paginationPayload, "Reviews retrieved successfully");
    }

    @Override
    @Transactional
    public ResponseDto<ReviewResponseDto> updateReview(String callerEmail, UUID productId, UUID reviewId, UpdateReviewRequestDto request) {
        User user = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new ResourceNotFoundException(AuthError.USER_NOT_FOUND.getMessage()));

        Review review = reviewRepository.findWithDetailsById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(ReviewError.REVIEW_NOT_FOUND.getMessage()));

        if (!review.getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException(ReviewError.REVIEW_PRODUCT_MISMATCH.getMessage());
        }

        boolean isAuthor = review.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException(ReviewError.REVIEW_ACCESS_DENIED.getMessage());
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment() != null ? request.getComment().trim() : null);

        Review updatedReview = reviewRepository.saveAndFlush(review);
        recalculateProductRating(productId);

        log.info("User {} updated review {} for product {} to rating {}", callerEmail, reviewId, productId, request.getRating());
        return ApiResponse.success(mapToResponseDto(updatedReview), "Review updated successfully");
    }

    @Override
    @Transactional
    public ResponseDto<Void> deleteReview(String callerEmail, UUID productId, UUID reviewId) {
        User user = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new ResourceNotFoundException(AuthError.USER_NOT_FOUND.getMessage()));

        Review review = reviewRepository.findWithDetailsById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(ReviewError.REVIEW_NOT_FOUND.getMessage()));

        if (!review.getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException(ReviewError.REVIEW_PRODUCT_MISMATCH.getMessage());
        }

        boolean isAuthor = review.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException(ReviewError.REVIEW_ACCESS_DENIED.getMessage());
        }

        reviewRepository.delete(review);
        reviewRepository.flush();
        recalculateProductRating(productId);

        log.info("User {} deleted review {} for product {}", callerEmail, reviewId, productId);
        return ApiResponse.success(null, "Review deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<ReviewResponseDto> getReviewById(UUID productId, UUID reviewId) {
        Review review = reviewRepository.findWithDetailsById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(ReviewError.REVIEW_NOT_FOUND.getMessage()));

        if (!review.getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException(ReviewError.REVIEW_PRODUCT_MISMATCH.getMessage());
        }

        return ApiResponse.success(mapToResponseDto(review), "Review retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<ReviewResponseDto> getMyReviewForProduct(String callerEmail, UUID productId) {
        User user = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new ResourceNotFoundException(AuthError.USER_NOT_FOUND.getMessage()));

        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException(CatalogError.PRODUCT_NOT_FOUND.getMessage());
        }

        Review review = reviewRepository.findByProductIdAndUserId(productId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ReviewError.REVIEW_NOT_FOUND.getMessage()));

        return ApiResponse.success(mapToResponseDto(review), "User review retrieved successfully");
    }

    private void recalculateProductRating(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(CatalogError.PRODUCT_NOT_FOUND.getMessage()));

        Double avg = reviewRepository.calculateAverageRatingByProductId(productId);
        long count = reviewRepository.countByProductId(productId);

        double averageRating = (avg != null && count > 0) ? Math.round(avg * 10.0) / 10.0 : 0.0;
        int totalReviews = (int) count;

        product.setAverageRating(averageRating);
        product.setTotalReviews(totalReviews);
        productRepository.save(product);
        log.info("Recalculated rating for product {}: averageRating={}, totalReviews={}", productId, averageRating, totalReviews);
    }

    private ReviewResponseDto mapToResponseDto(Review review) {
        return ReviewResponseDto.builder()
                .id(review.getId())
                .productId(review.getProduct() != null ? review.getProduct().getId() : null)
                .userId(review.getUser() != null ? review.getUser().getId() : null)
                .userFullName(review.getUser() != null ? review.getUser().getFullName() : null)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
