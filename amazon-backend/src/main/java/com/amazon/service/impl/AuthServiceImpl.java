package com.amazon.service.impl;

import com.amazon.dtos.auth.request.LoginRequestDto;
import com.amazon.dtos.auth.request.RegisterRequestDto;
import com.amazon.dtos.auth.response.AuthResponseDto;
import com.amazon.dtos.user.response.UserResponseDto;
import com.amazon.entity.Role;
import com.amazon.entity.User;
import com.amazon.enums.UserStatus;
import com.amazon.exception.BusinessRuleException;
import com.amazon.exception.DuplicateResourceException;
import com.amazon.exception.InvalidCredentialsException;
import com.amazon.payloads.ApiResponse;
import com.amazon.payloads.AuthError;
import com.amazon.payloads.ResponseDto;
import com.amazon.repository.RoleRepository;
import com.amazon.repository.UserRepository;
import com.amazon.security.JwtService;
import com.amazon.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link AuthService} handling user registration and login.
 * Adheres to Senior Developer Guidelines Section 6, 8, 9, 10.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private static final String DEFAULT_ROLE = "ROLE_CUSTOMER";

    @Override
    @Transactional
    public ResponseDto<AuthResponseDto> register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(AuthError.EMAIL_ALREADY_EXISTS.getMessage());
        }

        Role defaultRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(DEFAULT_ROLE)
                        .description("Default customer role")
                        .build()));

        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase().trim())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .roles(roles)
                .build();

        userRepository.save(user);
        log.info("New user registered successfully with email: {}", user.getEmail());

        String token = jwtService.generateToken(user.getEmail());

        AuthResponseDto authResponse = AuthResponseDto.builder()
                .token(token)
                .user(mapToUserResponseDto(user))
                .build();

        return ApiResponse.success(authResponse, "Registration successful");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<AuthResponseDto> login(LoginRequestDto request) {
        User user = userRepository.findByEmailWithRoles(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new InvalidCredentialsException(
                        AuthError.INVALID_CREDENTIALS.getMessage()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Failed login attempt for user: {}", request.getEmail());
            throw new InvalidCredentialsException(AuthError.INVALID_CREDENTIALS.getMessage());
        }

        if (!user.isActive()) {
            throw new BusinessRuleException(AuthError.ACCOUNT_INACTIVE.getMessage());
        }

        log.info("User logged in successfully: {}", user.getEmail());

        String token = jwtService.generateToken(user.getEmail());

        AuthResponseDto authResponse = AuthResponseDto.builder()
                .token(token)
                .user(mapToUserResponseDto(user))
                .build();

        return ApiResponse.success(authResponse, "Login successful");
    }

    private UserResponseDto mapToUserResponseDto(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return UserResponseDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .roles(roleNames)
                .build();
    }
}
