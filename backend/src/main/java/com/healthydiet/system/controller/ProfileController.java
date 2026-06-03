package com.healthydiet.system.controller;

import com.healthydiet.system.common.Result;
import com.healthydiet.system.dto.profile.ProfileMetricsResponse;
import com.healthydiet.system.dto.profile.ProfileRequest;
import com.healthydiet.system.dto.profile.ProfileResponse;
import com.healthydiet.system.security.AuthInterceptor;
import com.healthydiet.system.security.JwtClaims;
import com.healthydiet.system.service.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public Result<ProfileResponse> getProfile(HttpServletRequest request) {
        return Result.success(userProfileService.getProfile(currentUserId(request)));
    }

    @PutMapping
    public Result<ProfileResponse> saveProfile(@Valid @RequestBody ProfileRequest requestBody, HttpServletRequest request) {
        return Result.success(userProfileService.saveProfile(currentUserId(request), requestBody));
    }

    @GetMapping("/metrics")
    public Result<ProfileMetricsResponse> getMetrics(HttpServletRequest request) {
        return Result.success(userProfileService.getMetrics(currentUserId(request)));
    }

    private Long currentUserId(HttpServletRequest request) {
        JwtClaims claims = (JwtClaims) request.getAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE);
        return claims.getUserId();
    }
}