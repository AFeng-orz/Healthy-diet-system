package com.healthydiet.system.controller;

import com.healthydiet.system.common.Result;
import com.healthydiet.system.dto.report.WeeklyReportResponse;
import com.healthydiet.system.security.AuthInterceptor;
import com.healthydiet.system.security.JwtClaims;
import com.healthydiet.system.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/weekly")
    public Result<WeeklyReportResponse> getWeeklyReport(HttpServletRequest request) {
        return Result.success(reportService.getWeeklyReport(currentUserId(request)));
    }

    private Long currentUserId(HttpServletRequest request) {
        JwtClaims claims = (JwtClaims) request.getAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE);
        return claims.getUserId();
    }
}
