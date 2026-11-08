package org.example.umc11th.domain.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.umc11th.domain.auth.dto.LoginRequest;
import org.example.umc11th.domain.auth.dto.LoginResponse;
import org.example.umc11th.domain.auth.service.AuthService;
import org.example.umc11th.global.apiPayload.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ApiResponse.onSuccess("로그인에 성공했습니다.", authService.login(request));
    }
}
