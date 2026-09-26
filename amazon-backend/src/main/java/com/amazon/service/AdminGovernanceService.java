package com.amazon.service;

import com.amazon.dtos.admin.response.*;
import com.amazon.dtos.user.response.UserResponseDto;
import com.amazon.enums.OrderStatus;
import com.amazon.payloads.PaginationPayload;
import com.amazon.payloads.ResponseDto;
import java.util.UUID;

public interface AdminGovernanceService {
    ResponseDto<PaginationPayload<UserResponseDto>> getUsers(String role, Boolean active, String email, int page, int size);
    ResponseDto<UserResponseDto> updateUserStatus(UUID id, boolean active);
    ResponseDto<UserResponseDto> updateUserRoles(UUID id, java.util.Set<String> roles);
    ResponseDto<PaginationPayload<AdminSellerResponseDto>> getSellers(Boolean verified, int page, int size);
    ResponseDto<AdminSellerResponseDto> updateSellerVerification(UUID id, boolean verified);
    ResponseDto<PaginationPayload<AdminOrderResponseDto>> getOrders(OrderStatus status, UUID sellerId, UUID buyerId, String orderNumber, int page, int size);
    ResponseDto<AdminOrderResponseDto> overrideOrderStatus(UUID id, OrderStatus status);
    ResponseDto<AdminAnalyticsResponseDto> getAnalytics();
}
