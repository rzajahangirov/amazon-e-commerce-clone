package com.amazon.dtos.wishlist.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO representing a product in the user's wishlist.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Wishlist item containing product summary details")
public class WishlistItemResponseDto {

    private UUID wishlistItemId;
    private UUID productId;
    private String productTitle;
    private String productDescription;
    private BigDecimal basePrice;
    private BigDecimal productBasePrice;
    private String productMainImageUrl;
    private String mainImageUrl;
    private UUID listingId;
    private String brandName;
    private String categoryName;
    @Builder.Default
    private Double averageRating = 0.0;
    @Builder.Default
    private Double productAverageRating = 0.0;
    @Builder.Default
    private Integer totalReviews = 0;
    private LocalDateTime addedAt;
}
