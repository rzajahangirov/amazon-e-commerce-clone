package com.amazon.dtos.admin.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data point representing aggregated daily GMV on a specific calendar date.
 * Designed for rendering timeline charts on the Enterprise Dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyGmvPointDto {
    private LocalDate date;
    private BigDecimal gmv;
}
