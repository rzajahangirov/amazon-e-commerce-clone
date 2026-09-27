package com.amazon.dtos.listing.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Enterprise Merchant Portal response DTO alias for product listing offers.
 * Supports minimum repricing boundaries with {@code minPriceFloor}.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ListingResponseDto extends ProductListingResponseDto {
}
