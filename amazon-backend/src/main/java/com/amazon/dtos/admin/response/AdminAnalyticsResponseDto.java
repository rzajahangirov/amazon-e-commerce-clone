package com.amazon.dtos.admin.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAnalyticsResponseDto {
    private BigDecimal totalGMV;
    private Long totalOrdersCount;
    private Long totalActiveUsers;
    private Long totalActiveSellers;
    private Long totalActiveBrands;
    private Long totalPendingBrandApplications;
    private List<TopMetricResponseDto> topPerformingBrands;
    private List<TopMetricResponseDto> topPerformingCategories;
    private List<DailyGmvPointDto> dailyGmvTrajectory;
}
