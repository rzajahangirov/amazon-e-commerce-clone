package com.amazon.dtos.user.response;

import com.amazon.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Response DTO for user information returned in API responses.
 * Never leaks password hashes or internal-only fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private UUID id;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private LocalDateTime lastActiveAt;
    private UserStatus status;
    private Set<String> roles;
}
