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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.amazon.dtos.auth.request.SellerRegisterRequestDto;
import com.amazon.entity.SellerProfile;
import com.amazon.payloads.SellerError;
import com.amazon.repository.SellerProfileRepository;

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
    private final SellerProfileRepository sellerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private static final String DEFAULT_ROLE = "ROLE_CUSTOMER";
    private static final String SELLER_ROLE = "ROLE_SELLER";

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
    @Transactional
    public ResponseDto<AuthResponseDto> registerSeller(SellerRegisterRequestDto request) {
        String email = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException(AuthError.EMAIL_ALREADY_EXISTS.getMessage());
        }

        String storeName = request.getStoreName().trim();
        if (sellerProfileRepository.existsByStoreName(storeName)) {
            throw new DuplicateResourceException(SellerError.STORE_NAME_ALREADY_EXISTS.getMessage());
        }

        Role sellerRole = roleRepository.findByName(SELLER_ROLE)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(SELLER_ROLE)
                        .description("Seller role for inventory management")
                        .build()));

        Role customerRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(DEFAULT_ROLE)
                        .description("Default customer role")
                        .build()));

        Set<Role> roles = new HashSet<>();
        roles.add(sellerRole);
        roles.add(customerRole);

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(email)
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        SellerProfile sellerProfile = SellerProfile.builder()
                .user(savedUser)
                .storeName(storeName)
                .taxNumber(request.getTaxNumber().trim())
                .businessAddress(request.getBusinessAddress().trim())
                .bankAccountDetails(request.getBankAccountDetails() != null ? request.getBankAccountDetails().trim() : null)
                .isVerified(true)
                .build();

        SellerProfile savedProfile = sellerProfileRepository.save(sellerProfile);
        savedUser.setSellerProfile(savedProfile);

        log.info("New standalone 3P seller registered successfully with email: {}, store: {}", email, storeName);

        String token = jwtService.generateToken(savedUser.getEmail());

        AuthResponseDto authResponse = AuthResponseDto.builder()
                .token(token)
                .user(mapToUserResponseDto(savedUser))
                .build();

        return ApiResponse.success(authResponse, "Seller registration successful");
    }

    @Override
    @Transactional
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

        user.setLastActiveAt(LocalDateTime.now());
        userRepository.save(user);

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
                .avatarUrl(user.getAvatarUrl())
                .lastActiveAt(user.getLastActiveAt())
                .status(user.getStatus())
                .roles(roleNames)
                .build();
    }
}
