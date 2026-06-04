package com.healthydiet.system.dto.record;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DietRecordRequest {

    @NotNull(message = "请选择食物")
    private Long foodId;

    private LocalDate recordDate;

    @NotBlank(message = "请选择餐次")
    private String mealType;

    @NotNull(message = "请输入克数")
    @DecimalMin(value = "1", message = "克数必须大于 0")
    private BigDecimal grams;

    @Size(max = 255, message = "备注不能超过 255 个字符")
    private String remark;
}
