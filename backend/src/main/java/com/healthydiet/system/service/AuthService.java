package com.healthydiet.system.service;

import com.healthydiet.system.dto.auth.AuthUserResponse;
import com.healthydiet.system.dto.auth.LoginRequest;
import com.healthydiet.system.dto.auth.LoginResponse;
import com.healthydiet.system.dto.auth.RegisterRequest;
import com.healthydiet.system.security.JwtClaims;

public interface AuthService {

    LoginResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    AuthUserResponse getCurrentUser(JwtClaims claims);
}