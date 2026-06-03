package com.healthydiet.system.controller;

import com.healthydiet.system.common.Result;
import com.healthydiet.system.dto.diet.DietPlanResponse;
import com.healthydiet.system.security.AuthInterceptor;
import com.healthydiet.system.security.JwtClaims;
import com.healthydiet.system.service.DietPlanService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diet-plans")
@RequiredArgsConstructor
public class DietPlanController {

    private final DietPlanService dietPlanService;

    @PostMapping("/generate")
    public Result<DietPlanResponse> generate(HttpServletRequest request) {
        return Result.success(dietPlanService.generate(currentUserId(request)));
    }

    @GetMapping("/latest")
    public Result<DietPlanResponse> getLatest(HttpServletRequest request) {
        return Result.success(dietPlanService.getLatest(currentUserId(request)));
    }

    @GetMapping("/{id}")
    public Result<DietPlanResponse> getPlan(@PathVariable Long id, HttpServletRequest request) {
        return Result.success(dietPlanService.getPlan(currentUserId(request), id));
    }

    private Long currentUserId(HttpServletRequest request) {
        JwtClaims claims = (JwtClaims) request.getAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE);
        return claims.getUserId();
    }
}
