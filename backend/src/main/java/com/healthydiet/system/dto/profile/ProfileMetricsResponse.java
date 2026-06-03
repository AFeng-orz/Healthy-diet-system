package com.healthydiet.system.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ProfileMetricsResponse {

    private BigDecimal bmi;

    private String bmiLevel;

    private BigDecimal bmr;

    private BigDecimal tdee;

    private BigDecimal recommendedCalories;

    private BigDecimal proteinTarget;

    private BigDecimal fatTarget;

    private BigDecimal carbsTarget;
}