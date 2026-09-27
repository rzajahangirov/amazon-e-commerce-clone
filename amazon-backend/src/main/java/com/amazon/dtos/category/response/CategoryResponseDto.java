package com.amazon.dtos.category.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for categories, supporting hierarchical trees.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponseDto {

    private UUID id;
    private UUID parentId;
    private String name;
    private String slug;
    private Integer level;
    private String commercialJustification;
    private Boolean isApproved;
    private String rejectionReason;
    private List<CategoryResponseDto> subCategories;
    private LocalDateTime createdAt;
}
