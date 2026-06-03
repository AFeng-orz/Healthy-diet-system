package com.healthydiet.system.dto.health;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnswerRequest {

    @NotBlank(message = "题目标识不能为空")
    private String questionKey;

    @NotNull(message = "请选择问卷答案")
    @Min(value = 0, message = "答案分值不能小于0")
    @Max(value = 2, message = "答案分值不能大于2")
    private Integer score;
}