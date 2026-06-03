package com.healthydiet.system.service;

import com.healthydiet.system.dto.profile.ProfileMetricsResponse;
import com.healthydiet.system.entity.UserProfile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class NutritionCalculator {

    public ProfileMetricsResponse calculate(UserProfile profile) {
        double heightCm = profile.getHeightCm().doubleValue();
        double heightM = heightCm / 100.0;
        double weightKg = profile.getWeightKg().doubleValue();
        int age = profile.getAge();

        double bmi = weightKg / (heightM * heightM);
        double bmr = calculateBmr(profile.getGender(), weightKg, heightCm, age);
        double tdee = bmr * activityFactor(profile.getActivityLevel());
        double calories = recommendedCalories(tdee, profile.getDietGoal());
        double protein = proteinTarget(weightKg, profile.getDietGoal());
        double fat = calories * 0.25 / 9.0;
        double carbs = Math.max((calories - protein * 4.0 - fat * 9.0) / 4.0, 0);

        return new ProfileMetricsResponse(
                oneDecimal(bmi),
                bmiLevel(bmi),
                zeroDecimal(bmr),
                zeroDecimal(tdee),
                zeroDecimal(calories),
                oneDecimal(protein),
                oneDecimal(fat),
                oneDecimal(carbs)
        );
    }

    private double calculateBmr(String gender, double weightKg, double heightCm, int age) {
        double base = 10.0 * weightKg + 6.25 * heightCm - 5.0 * age;
        if ("male".equals(gender)) {
            return base + 5.0;
        }
        return base - 161.0;
    }

    private double activityFactor(String activityLevel) {
        return switch (activityLevel) {
            case "light" -> 1.375;
            case "moderate" -> 1.55;
            case "active" -> 1.725;
            default -> 1.2;
        };
    }

    private double recommendedCalories(double tdee, String dietGoal) {
        return switch (dietGoal) {
            case "fat_loss" -> Math.max(tdee - 400.0, 1200.0);
            case "muscle_gain" -> tdee + 300.0;
            default -> tdee;
        };
    }

    private double proteinTarget(double weightKg, String dietGoal) {
        return switch (dietGoal) {
            case "fat_loss", "muscle_gain" -> weightKg * 1.6;
            default -> weightKg * 1.2;
        };
    }

    private String bmiLevel(double bmi) {
        if (bmi < 18.5) {
            return "偏瘦";
        }
        if (bmi < 24.0) {
            return "正常";
        }
        if (bmi < 28.0) {
            return "超重";
        }
        return "肥胖";
    }

    private BigDecimal oneDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP);
    }

    private BigDecimal zeroDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(0, RoundingMode.HALF_UP);
    }
}