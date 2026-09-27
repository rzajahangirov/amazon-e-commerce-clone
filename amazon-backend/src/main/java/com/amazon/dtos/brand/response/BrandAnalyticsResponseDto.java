package com.amazon.dtos.brand.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for Brand Dashboard Analytics & Statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandAnalyticsResponseDto {

    private Long totalBrandProducts;
    private Long totalBrandVariants;
    private Long totalActiveListings;
    private Long totalTeamMembers;
    private Long totalBrandPosts;
    private BigDecimal totalBrandRevenue;
    private Long totalUnitsSold;
    private Long unauthorizedSellerAlertsCount;
    private Long pendingCatalogCount;
    private List<TopSellingProductDto> topSellingProducts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopSellingProductDto {
        private String productTitle;
        private String asin;
        private Long totalUnitsSold;
        private BigDecimal totalRevenue;
    }
}
