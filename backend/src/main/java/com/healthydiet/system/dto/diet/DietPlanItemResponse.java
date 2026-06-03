package com.healthydiet.system.dto.diet;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DietPlanItemResponse {

    private Long id;

    private Long foodId;

    private String mealType;

    private String mealName;

    private String foodName;

    private String foodCategory;

    private BigDecimal grams;

    private BigDecimal calories;

    private BigDecimal protein;

    private BigDecimal fat;

    private BigDecimal carbs;

    private String reason;

    private Integer sortOrder;
}
