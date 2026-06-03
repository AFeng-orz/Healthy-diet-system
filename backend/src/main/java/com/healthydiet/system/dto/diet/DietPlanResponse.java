package com.healthydiet.system.dto.diet;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class DietPlanResponse {

    private Long id;

    private LocalDate planDate;

    private BigDecimal targetCalories;

    private BigDecimal totalCalories;

    private BigDecimal totalProtein;

    private BigDecimal totalFat;

    private BigDecimal totalCarbs;

    private String summary;

    private List<DietPlanItemResponse> items;

    private LocalDateTime createTime;
}
