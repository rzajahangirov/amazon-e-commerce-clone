package com.amazon.service;

import com.amazon.dtos.auth.request.LoginRequestDto;
import com.amazon.dtos.auth.request.RegisterRequestDto;
import com.amazon.dtos.auth.response.AuthResponseDto;
import com.amazon.payloads.ResponseDto;

/**
 * Service interface for authentication operations.
 */
public interface AuthService {

    ResponseDto<AuthResponseDto> register(RegisterRequestDto request);

    ResponseDto<AuthResponseDto> registerSeller(com.amazon.dtos.auth.request.SellerRegisterRequestDto request);

    ResponseDto<AuthResponseDto> login(LoginRequestDto request);
}
