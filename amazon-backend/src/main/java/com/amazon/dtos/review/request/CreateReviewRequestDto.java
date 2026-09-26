package com.amazon.dtos.review.request;

import com.amazon.payloads.ApiPayload;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for submitting a product review and rating (1 to 5 stars).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for creating a product review and rating")
public class CreateReviewRequestDto implements ApiPayload {

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1 star")
    @Max(value = 5, message = "Rating cannot exceed 5 stars")
    @Schema(description = "Rating score between 1 and 5", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer rating;

    @Size(max = 2000, message = "Review comment cannot exceed 2000 characters")
    @Schema(description = "Optional written customer review comment", example = "Outstanding build quality and fast shipping!")
    private String comment;
}
