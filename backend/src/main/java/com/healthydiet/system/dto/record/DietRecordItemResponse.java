package com.healthydiet.system.dto.record;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DietRecordItemResponse {

    private Long id;

    private Long foodId;

    private LocalDate recordDate;

    private String mealType;

    private String mealName;

    private String foodName;

    private String foodCategory;

    private BigDecimal grams;

    private BigDecimal calories;

    private BigDecimal protein;

    private BigDecimal fat;

    private BigDecimal carbs;

    private String remark;

    private LocalDateTime createTime;
}
