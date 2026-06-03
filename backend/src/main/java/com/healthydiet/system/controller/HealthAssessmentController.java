package com.healthydiet.system.controller;

import com.healthydiet.system.common.Result;
import com.healthydiet.system.dto.health.AssessmentResponse;
import com.healthydiet.system.dto.health.AssessmentSubmitRequest;
import com.healthydiet.system.dto.health.QuestionResponse;
import com.healthydiet.system.security.AuthInterceptor;
import com.healthydiet.system.security.JwtClaims;
import com.healthydiet.system.service.HealthAssessmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthAssessmentController {

    private final HealthAssessmentService healthAssessmentService;

    @GetMapping("/questions")
    public Result<List<QuestionResponse>> getQuestions() {
        return Result.success(healthAssessmentService.getQuestions());
    }

    @PostMapping("/assessment")
    public Result<AssessmentResponse> submit(@Valid @RequestBody AssessmentSubmitRequest requestBody, HttpServletRequest request) {
        return Result.success(healthAssessmentService.submit(currentUserId(request), requestBody));
    }

    @GetMapping("/assessment/latest")
    public Result<AssessmentResponse> latest(HttpServletRequest request) {
        return Result.success(healthAssessmentService.getLatest(currentUserId(request)));
    }

    private Long currentUserId(HttpServletRequest request) {
        JwtClaims claims = (JwtClaims) request.getAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE);
        return claims.getUserId();
    }
}