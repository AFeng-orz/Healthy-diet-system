package com.healthydiet.system.dto.food;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FoodRequest {

    @NotBlank(message = "食物名称不能为空")
    @Size(max = 100, message = "食物名称不能超过100个字符")
    private String name;

    @NotBlank(message = "请选择食物分类")
    @Size(max = 50, message = "食物分类不能超过50个字符")
    private String category;

    @NotNull(message = "请输入每100g热量")
    @DecimalMin(value = "0", message = "热量不能小于0")
    @DecimalMax(value = "1000", message = "热量不能超过1000kcal")
    private BigDecimal calories;

    @NotNull(message = "请输入每100g蛋白质")
    @DecimalMin(value = "0", message = "蛋白质不能小于0")
    @DecimalMax(value = "100", message = "蛋白质不能超过100g")
    private BigDecimal protein;

    @NotNull(message = "请输入每100g脂肪")
    @DecimalMin(value = "0", message = "脂肪不能小于0")
    @DecimalMax(value = "100", message = "脂肪不能超过100g")
    private BigDecimal fat;

    @NotNull(message = "请输入每100g碳水")
    @DecimalMin(value = "0", message = "碳水不能小于0")
    @DecimalMax(value = "100", message = "碳水不能超过100g")
    private BigDecimal carbs;

    @Size(max = 100, message = "适合餐次不能超过100个字符")
    private String mealTags;

    private Boolean highSugar;

    private Boolean highFat;

    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}