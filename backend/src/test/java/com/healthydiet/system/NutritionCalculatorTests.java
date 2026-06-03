package com.healthydiet.system;

import com.healthydiet.system.dto.profile.ProfileMetricsResponse;
import com.healthydiet.system.entity.UserProfile;
import com.healthydiet.system.service.NutritionCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NutritionCalculatorTests {

    private final NutritionCalculator calculator = new NutritionCalculator();

    @Test
    void shouldCalculateMetricsForFatLossProfile() {
        UserProfile profile = new UserProfile();
        profile.setGender("male");
        profile.setAge(25);
        profile.setHeightCm(new BigDecimal("175"));
        profile.setWeightKg(new BigDecimal("70"));
        profile.setActivityLevel("moderate");
        profile.setDietGoal("fat_loss");

        ProfileMetricsResponse metrics = calculator.calculate(profile);

        assertEquals(new BigDecimal("22.9"), metrics.getBmi());
        assertEquals("正常", metrics.getBmiLevel());
        assertEquals(new BigDecimal("1674"), metrics.getBmr());
        assertEquals(new BigDecimal("2594"), metrics.getTdee());
        assertEquals(new BigDecimal("2194"), metrics.getRecommendedCalories());
        assertEquals(new BigDecimal("112.0"), metrics.getProteinTarget());
    }
}
