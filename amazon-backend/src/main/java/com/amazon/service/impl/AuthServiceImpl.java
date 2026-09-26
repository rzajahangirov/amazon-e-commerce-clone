package com.amazon.service.impl;

import com.amazon.dtos.auth.request.LoginRequestDto;
import com.amazon.dtos.auth.request.RegisterRequestDto;
import com.amazon.dtos.auth.response.AuthResponseDto;
import com.amazon.dtos.user.response.UserResponseDto;
import com.amazon.entity.User;
import com.amazon.enums.RoleType;
import com.amazon.exception.DuplicateResourceException;
import com.amazon.exception.InvalidCredentialsException;
import com.amazon.payloads.ApiResponse;
import com.amazon.payloads.AuthError;
import com.amazon.payloads.ResponseDto;
import com.amazon.repository.UserRepository;
import com.amazon.security.JwtService;
import com.amazon.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link AuthService} handling user registration and login.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public ResponseDto<AuthResponseDto> register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(AuthError.EMAIL_ALREADY_EXISTS.getMessage());
        }

        User user = User.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(RoleType.USER)
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        String token = jwtService.generateToken(user.getEmail());

        AuthResponseDto authResponse = AuthResponseDto.builder()
                .token(token)
                .user(modelMapper.map(user, UserResponseDto.class))
                .build();

        return ApiResponse.success(authResponse, "Registration successful");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<AuthResponseDto> login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException(
                        AuthError.INVALID_CREDENTIALS.getMessage()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(AuthError.INVALID_CREDENTIALS.getMessage());
        }

        log.info("User logged in: {}", user.getEmail());

        String token = jwtService.generateToken(user.getEmail());

        AuthResponseDto authResponse = AuthResponseDto.builder()
                .token(token)
                .user(modelMapper.map(user, UserResponseDto.class))
                .build();

        return ApiResponse.success(authResponse, "Login successful");
    }
}
