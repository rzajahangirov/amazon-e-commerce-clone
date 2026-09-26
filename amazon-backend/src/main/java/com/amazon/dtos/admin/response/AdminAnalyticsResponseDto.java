package com.amazon.dtos.admin.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class AdminAnalyticsResponseDto {
    private BigDecimal totalGMV;
    private Long totalOrdersCount;
    private Long totalActiveUsers;
    private Long totalActiveSellers;
    private Long totalActiveBrands;
    private List<TopMetricResponseDto> topPerformingBrands;
    private List<TopMetricResponseDto> topPerformingCategories;
}
