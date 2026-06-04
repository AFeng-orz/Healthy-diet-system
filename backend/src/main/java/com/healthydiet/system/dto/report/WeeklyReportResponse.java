package com.healthydiet.system.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class WeeklyReportResponse {

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal targetCalories;

    private BigDecimal targetProtein;

    private BigDecimal targetFat;

    private BigDecimal targetCarbs;

    private BigDecimal todayCalories;

    private BigDecimal todayProtein;

    private BigDecimal todayFat;

    private BigDecimal todayCarbs;

    private Integer todayCaloriesRate;

    private Integer todayProteinRate;

    private Integer todayFatRate;

    private Integer todayCarbsRate;

    private BigDecimal averageCalories;

    private Integer recordedDays;

    private String summary;

    private List<DailyNutritionTrendResponse> trends;
}
