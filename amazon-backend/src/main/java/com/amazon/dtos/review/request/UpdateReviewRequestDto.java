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
 * Request DTO for updating an existing product review and rating.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for updating an existing product review")
public class UpdateReviewRequestDto implements ApiPayload {

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1 star")
    @Max(value = 5, message = "Rating cannot exceed 5 stars")
    @Schema(description = "Updated rating score between 1 and 5", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer rating;

    @Size(max = 2000, message = "Review comment cannot exceed 2000 characters")
    @Schema(description = "Updated written review comment", example = "After 2 months of use, battery life is good but not great.")
    private String comment;
}
