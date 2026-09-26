package com.amazon.controller;

import com.amazon.dtos.auth.request.LoginRequestDto;
import com.amazon.dtos.auth.request.RegisterRequestDto;
import com.amazon.dtos.auth.request.SellerRegisterRequestDto;
import com.amazon.dtos.auth.response.AuthResponseDto;
import com.amazon.payloads.ResponseDto;
import com.amazon.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints (register and login).
 */
@RestController
@RequestMapping("v1/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration and login endpoints")
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account and returns a JWT token")
    public ResponseEntity<ResponseDto<AuthResponseDto>> register(
            @Valid @RequestBody RegisterRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @PostMapping("/seller/register")
    @Operation(summary = "Register a new 3P seller", description = "Atomically registers a seller account with SellerProfile and returns a JWT token")
    public ResponseEntity<ResponseDto<AuthResponseDto>> registerSeller(
            @Valid @RequestBody SellerRegisterRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.registerSeller(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates user credentials and returns a JWT token")
    public ResponseEntity<ResponseDto<AuthResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
