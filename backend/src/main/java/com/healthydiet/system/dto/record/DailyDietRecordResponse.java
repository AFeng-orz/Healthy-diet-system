package com.healthydiet.system.dto.record;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class DailyDietRecordResponse {

    private LocalDate recordDate;

    private BigDecimal totalCalories;

    private BigDecimal totalProtein;

    private BigDecimal totalFat;

    private BigDecimal totalCarbs;

    private BigDecimal targetCalories;

    private BigDecimal targetProtein;

    private BigDecimal targetFat;

    private BigDecimal targetCarbs;

    private Integer caloriesRate;

    private Integer proteinRate;

    private Integer fatRate;

    private Integer carbsRate;

    private List<DietRecordItemResponse> records;
}
