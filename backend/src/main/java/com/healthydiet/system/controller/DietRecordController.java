package com.healthydiet.system.controller;

import com.healthydiet.system.common.Result;
import com.healthydiet.system.dto.record.DailyDietRecordResponse;
import com.healthydiet.system.dto.record.DietRecordItemResponse;
import com.healthydiet.system.dto.record.DietRecordRequest;
import com.healthydiet.system.security.AuthInterceptor;
import com.healthydiet.system.security.JwtClaims;
import com.healthydiet.system.service.DietRecordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/diet-records")
@RequiredArgsConstructor
public class DietRecordController {

    private final DietRecordService dietRecordService;

    @PostMapping
    public Result<DietRecordItemResponse> createRecord(@Valid @RequestBody DietRecordRequest requestBody, HttpServletRequest request) {
        return Result.success(dietRecordService.createRecord(currentUserId(request), requestBody));
    }

    @GetMapping("/daily")
    public Result<DailyDietRecordResponse> getDailyRecords(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest request
    ) {
        return Result.success(dietRecordService.getDailyRecords(currentUserId(request), date));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRecord(@PathVariable Long id, HttpServletRequest request) {
        dietRecordService.deleteRecord(currentUserId(request), id);
        return Result.success();
    }

    private Long currentUserId(HttpServletRequest request) {
        JwtClaims claims = (JwtClaims) request.getAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE);
        return claims.getUserId();
    }
}
