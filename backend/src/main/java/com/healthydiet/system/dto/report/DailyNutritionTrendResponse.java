package com.healthydiet.system.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DailyNutritionTrendResponse {

    private LocalDate date;

    private BigDecimal calories;

    private BigDecimal protein;

    private BigDecimal fat;

    private BigDecimal carbs;

    private Integer caloriesRate;
}
