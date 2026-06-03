package com.healthydiet.system.controller;

import com.healthydiet.system.common.Result;
import com.healthydiet.system.dto.auth.AuthUserResponse;
import com.healthydiet.system.dto.auth.LoginRequest;
import com.healthydiet.system.dto.auth.LoginResponse;
import com.healthydiet.system.dto.auth.RegisterRequest;
import com.healthydiet.system.security.AuthInterceptor;
import com.healthydiet.system.security.JwtClaims;
import com.healthydiet.system.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @GetMapping("/me")
    public Result<AuthUserResponse> me(HttpServletRequest request) {
        JwtClaims claims = (JwtClaims) request.getAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE);
        return Result.success(authService.getCurrentUser(claims));
    }
}