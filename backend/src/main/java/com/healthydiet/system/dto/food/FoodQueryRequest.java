package com.healthydiet.system.dto.food;

import lombok.Data;

@Data
public class FoodQueryRequest {

    private Long pageNum = 1L;

    private Long pageSize = 10L;

    private String keyword;

    private String category;

    private String mealTag;

    private Boolean highSugar;

    private Boolean highFat;
}