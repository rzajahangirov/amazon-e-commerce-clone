package com.amazon.controller;

import com.amazon.dtos.admin.request.*;
import com.amazon.dtos.admin.response.*;
import com.amazon.dtos.user.response.UserResponseDto;
import com.amazon.enums.OrderStatus;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;
import com.amazon.service.AdminGovernanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminGovernanceController {
    private final AdminGovernanceService adminService;

    @GetMapping("/v1/api/admin/users")
    public ResponseEntity<ResponseDto<PaginationPayload<UserResponseDto>>> getUsers(
            @RequestParam(required = false) String role, @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String email, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getUsers(role, active, email, page, size));
    }

    @PutMapping("/v1/api/admin/users/{id}/status")
    public ResponseEntity<ResponseDto<UserResponseDto>> updateUserStatus(@PathVariable UUID id,
            @Valid @RequestBody AdminUserStatusRequestDto request) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, request.getActive()));
    }

    @PutMapping("/v1/api/admin/users/{id}/roles")
    public ResponseEntity<ResponseDto<UserResponseDto>> updateUserRoles(@PathVariable UUID id,
            @Valid @RequestBody AdminUserRolesRequestDto request) {
        return ResponseEntity.ok(adminService.updateUserRoles(id, request.getRoles()));
    }

    @GetMapping("/v1/api/admin/sellers")
    public ResponseEntity<ResponseDto<PaginationPayload<AdminSellerResponseDto>>> getSellers(
            @RequestParam(required = false) Boolean verified, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getSellers(verified, page, size));
    }

    @PutMapping("/v1/api/admin/sellers/{id}/verify")
    public ResponseEntity<ResponseDto<AdminSellerResponseDto>> verifySeller(@PathVariable UUID id,
            @Valid @RequestBody AdminBooleanStatusRequestDto request) {
        return ResponseEntity.ok(adminService.updateSellerVerification(id, request.getActive()));
    }

    @GetMapping("/v1/api/admin/orders")
    public ResponseEntity<ResponseDto<PaginationPayload<AdminOrderResponseDto>>> getOrders(
            @RequestParam(required = false) OrderStatus status, @RequestParam(required = false) UUID sellerId,
            @RequestParam(required = false) UUID buyerId, @RequestParam(required = false) String orderNumber,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getOrders(status, sellerId, buyerId, orderNumber, page, size));
    }

    @PutMapping("/v1/api/admin/orders/{id}/override-status")
    public ResponseEntity<ResponseDto<AdminOrderResponseDto>> overrideOrderStatus(@PathVariable UUID id,
            @Valid @RequestBody AdminOrderStatusRequestDto request) {
        return ResponseEntity.ok(adminService.overrideOrderStatus(id, request.getStatus()));
    }

    @GetMapping("/v1/api/admin/analytics")
    public ResponseEntity<ResponseDto<AdminAnalyticsResponseDto>> getAnalytics() {
        return ResponseEntity.ok(adminService.getAnalytics());
    }
}
