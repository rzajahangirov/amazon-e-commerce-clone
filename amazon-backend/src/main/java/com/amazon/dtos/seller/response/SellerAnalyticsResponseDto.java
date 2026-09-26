package com.amazon.dtos.seller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for Seller Dashboard Analytics & Statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerAnalyticsResponseDto {

    private Long totalActiveListings;
    private Long totalProducts;
    private BigDecimal totalRevenue;
    private Long totalUnitsSold;
    private Long totalPendingOrders;
    private Long totalShippedOrders;
    private Long totalDeliveredOrders;
    private List<TopSellingListingDto> topSellingListings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopSellingListingDto {
        private String productTitle;
        private String sellerSku;
        private Long totalUnitsSold;
        private BigDecimal totalRevenue;
    }
}
