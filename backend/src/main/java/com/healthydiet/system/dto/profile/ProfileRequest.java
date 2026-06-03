package com.healthydiet.system.dto.profile;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProfileRequest {

    @NotBlank(message = "请选择性别")
    @Pattern(regexp = "male|female", message = "性别参数不正确")
    private String gender;

    @NotNull(message = "请输入年龄")
    @Min(value = 12, message = "年龄不能小于12岁")
    @Max(value = 100, message = "年龄不能超过100岁")
    private Integer age;

    @NotNull(message = "请输入身高")
    @DecimalMin(value = "80.0", message = "身高不能小于80cm")
    @DecimalMax(value = "230.0", message = "身高不能超过230cm")
    private BigDecimal heightCm;

    @NotNull(message = "请输入当前体重")
    @DecimalMin(value = "25.0", message = "体重不能小于25kg")
    @DecimalMax(value = "250.0", message = "体重不能超过250kg")
    private BigDecimal weightKg;

    @NotNull(message = "请输入目标体重")
    @DecimalMin(value = "25.0", message = "目标体重不能小于25kg")
    @DecimalMax(value = "250.0", message = "目标体重不能超过250kg")
    private BigDecimal targetWeightKg;

    @NotBlank(message = "请选择运动频率")
    @Pattern(regexp = "sedentary|light|moderate|active", message = "运动频率参数不正确")
    private String activityLevel;

    @NotBlank(message = "请选择饮食目标")
    @Pattern(regexp = "fat_loss|maintain|muscle_gain", message = "饮食目标参数不正确")
    private String dietGoal;

    @Size(max = 500, message = "忌口不能超过500个字符")
    private String avoidFoods;

    @Size(max = 500, message = "过敏信息不能超过500个字符")
    private String allergies;

    @Size(max = 500, message = "饮食偏好不能超过500个字符")
    private String preferences;
}