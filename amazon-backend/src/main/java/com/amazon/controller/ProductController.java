package com.amazon.controller;

import com.amazon.dtos.listing.request.CreateListingRequestDto;
import com.amazon.dtos.listing.request.UpdateListingStockRequestDto;
import com.amazon.dtos.listing.response.ProductListingResponseDto;
import com.amazon.dtos.product.request.CreateProductRequestDto;
import com.amazon.dtos.product.request.CreateVariantRequestDto;
import com.amazon.dtos.product.request.UpdateProductRequestDto;
import com.amazon.dtos.product.response.ProductResponseDto;
import com.amazon.dtos.product.response.ProductVariantResponseDto;
import com.amazon.enums.ProductStatus;
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

    @PostMapping
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Create product", description = "Creates a master product entry in the catalog")
    public ResponseEntity<ResponseDto<ProductResponseDto>> createProduct(
            @Valid @RequestBody CreateProductRequestDto request,
            Principal principal) {
        String sellerEmail = principal.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request, sellerEmail));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves product details with all associated variants")
    public ResponseEntity<ResponseDto<ProductResponseDto>> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping
    @Operation(summary = "Get products", description = "Paginated list of products optionally filtered by category")
    public ResponseEntity<ResponseDto<PaginationPayload<ProductResponseDto>>> getProducts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.getProducts(categoryId, status, page, size));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Update product", description = "Updates a product's details")
    public ResponseEntity<ResponseDto<ProductResponseDto>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequestDto request,
            Principal principal) {
        String sellerEmail = principal.getName();
        return ResponseEntity.ok(productService.updateProduct(id, request, sellerEmail));
    }

    // --- Product Variant Endpoints ---

    @PostMapping("/{id}/variants")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Create product variant", description = "Creates a new sellable variant with ASIN under a product")
    public ResponseEntity<ResponseDto<ProductVariantResponseDto>> createVariant(
            @PathVariable UUID id,
            @Valid @RequestBody CreateVariantRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productVariantService.createVariant(id, request));
    }

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
