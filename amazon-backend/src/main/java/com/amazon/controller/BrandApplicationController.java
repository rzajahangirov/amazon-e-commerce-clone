package com.amazon.controller;

import com.amazon.dtos.brand.request.CreateBrandApplicationRequestDto;
import com.amazon.dtos.brand.response.BrandApplicationResponseDto;
import com.amazon.payloads.ResponseDto;
import com.amazon.service.BrandApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public REST controller for submitting Brand Registration applications.
 * Adheres to Senior Backend Developer Guidelines Section 1, 3, 4.
 */
@RestController
@RequestMapping("/v1/api/brand-applications")
@RequiredArgsConstructor
public class BrandApplicationController {

    private final BrandApplicationService brandApplicationService;

    @PostMapping
    public ResponseEntity<ResponseDto<BrandApplicationResponseDto>> submitApplication(
            @Valid @RequestBody CreateBrandApplicationRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(brandApplicationService.submitApplication(request));
    }
}
