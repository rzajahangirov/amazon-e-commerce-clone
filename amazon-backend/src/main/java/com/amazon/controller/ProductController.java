package com.amazon.controller;

import com.amazon.dtos.listing.request.CreateListingRequestDto;
import com.amazon.dtos.listing.request.UpdateListingStockRequestDto;
import com.amazon.dtos.listing.response.ProductListingResponseDto;
import com.amazon.dtos.product.request.ProductSearchRequestDto;
import com.amazon.dtos.product.response.ProductResponseDto;
import com.amazon.dtos.product.response.ProductVariantResponseDto;
import com.amazon.enums.ProductSortBy;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;
import com.amazon.service.ProductListingService;
import com.amazon.service.ProductService;
import com.amazon.service.ProductVariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for Product catalog, variants, and seller listings.
 * Strictly adheres to Senior Backend Developer Guidelines (Principal pattern, zero entity leakage).
 */
@RestController
@RequestMapping("v1/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog, variants, and seller listing offers")
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final ProductVariantService productVariantService;
    private final ProductListingService productListingService;

    // --- Product Endpoints ---

    @GetMapping("/search")
    @Operation(summary = "Search and discover products", description = "Amazon-style dynamic multi-criteria search engine supporting keyword, category hierarchy, brand, price, rating, and sorting")
    public ResponseEntity<ResponseDto<PaginationPayload<ProductResponseDto>>> searchProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) UUID brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "FEATURED") ProductSortBy sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        String email = principal != null ? principal.getName() : null;
        ProductSearchRequestDto request = ProductSearchRequestDto.builder()
                .query(query)
                .categoryId(categoryId)
                .categorySlug(categorySlug)
                .brandId(brandId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minRating(minRating)
                .sortBy(sortBy)
                .page(page)
                .size(size)
                .build();
        return ResponseEntity.ok(productService.searchProducts(request, email));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves product details with all associated variants and favorite status")
    public ResponseEntity<ResponseDto<ProductResponseDto>> getProductById(
            @PathVariable UUID id,
            Principal principal) {
        String email = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(productService.getProductById(id, email));
    }

    @GetMapping
    @Operation(summary = "Get products", description = "Paginated list of products optionally filtered by category and favorite status")
    public ResponseEntity<ResponseDto<PaginationPayload<ProductResponseDto>>> getProducts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        String email = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(productService.getProducts(categoryId, page, size, email));
    }

    @GetMapping("/{id}/frequently-bought-together")
    @Operation(summary = "Get frequently bought together", description = "Returns up to 3 recommended complementary products for the 'Frequently Bought Together' section")
    public ResponseEntity<ResponseDto<List<ProductResponseDto>>> getFrequentlyBoughtTogether(
            @PathVariable UUID id,
            Principal principal) {
        String email = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(productService.getFrequentlyBoughtTogether(id, email));
    }

    // --- Product Variant Endpoints ---

    @GetMapping("/{id}/variants")
    @Operation(summary = "Get variants by product ID", description = "Retrieves all variants belonging to a product")
    public ResponseEntity<ResponseDto<List<ProductVariantResponseDto>>> getVariantsByProductId(
            @PathVariable UUID id) {
        return ResponseEntity.ok(productVariantService.getVariantsByProductId(id));
    }

    @GetMapping("/variants/asin/{asin}")
    @Operation(summary = "Get variant by ASIN", description = "Retrieves a variant by its Amazon Standard Identification Number")
    public ResponseEntity<ResponseDto<ProductVariantResponseDto>> getVariantByAsin(@PathVariable String asin) {
        return ResponseEntity.ok(productVariantService.getVariantByAsin(asin));
    }

    // --- Product Listing (Offer / Inventory) Endpoints ---

    @PostMapping("/listings")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Create seller listing", description = "Publishes a seller's price and inventory offer for a variant")
    public ResponseEntity<ResponseDto<ProductListingResponseDto>> createListing(
            @Valid @RequestBody CreateListingRequestDto request,
            Principal principal) {
        String sellerEmail = principal.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productListingService.createListing(request, sellerEmail));
    }

    @GetMapping("/listings/{id}")
    @Operation(summary = "Get listing by ID", description = "Retrieves a specific seller listing offer")
    public ResponseEntity<ResponseDto<ProductListingResponseDto>> getListingById(@PathVariable UUID id) {
        return ResponseEntity.ok(productListingService.getListingById(id));
    }

    @PutMapping("/listings/{id}/stock")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Update listing stock", description = "Updates stock inventory and price for a listing offer")
    public ResponseEntity<ResponseDto<ProductListingResponseDto>> updateListingStock(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateListingStockRequestDto request,
            Principal principal) {
        String sellerEmail = principal.getName();
        return ResponseEntity.ok(productListingService.updateStock(id, request, sellerEmail));
    }

    @GetMapping("/variants/{variantId}/listings")
    @Operation(summary = "Get listings for variant", description = "Retrieves all competing seller offers for a variant")
    public ResponseEntity<ResponseDto<List<ProductListingResponseDto>>> getListingsByVariantId(
            @PathVariable UUID variantId) {
        return ResponseEntity.ok(productListingService.getListingsByVariantId(variantId));
    }

    @GetMapping("/variants/{variantId}/buybox")
    @Operation(summary = "Get Buy Box winner", description = "Retrieves the current featured Buy Box offer for a variant")
    public ResponseEntity<ResponseDto<ProductListingResponseDto>> getBuyboxWinner(@PathVariable UUID variantId) {
        return ResponseEntity.ok(productListingService.getBuyboxWinner(variantId));
    }
}
