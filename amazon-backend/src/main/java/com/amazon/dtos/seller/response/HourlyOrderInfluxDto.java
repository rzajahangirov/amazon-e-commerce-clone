package com.amazon.dtos.seller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data point representing order volume for a specific hour of the day.
 * Designed for rendering hourly influx charts on the Seller Dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HourlyOrderInfluxDto {
    private String hour;
    private Long orderCount;
}
