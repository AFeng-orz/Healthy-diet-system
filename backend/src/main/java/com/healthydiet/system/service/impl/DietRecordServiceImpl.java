package com.healthydiet.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthydiet.system.common.BusinessException;
import com.healthydiet.system.dto.profile.ProfileMetricsResponse;
import com.healthydiet.system.dto.record.DailyDietRecordResponse;
import com.healthydiet.system.dto.record.DietRecordItemResponse;
import com.healthydiet.system.dto.record.DietRecordRequest;
import com.healthydiet.system.entity.DietRecord;
import com.healthydiet.system.entity.Food;
import com.healthydiet.system.entity.UserProfile;
import com.healthydiet.system.mapper.DietRecordMapper;
import com.healthydiet.system.mapper.UserProfileMapper;
import com.healthydiet.system.service.DietRecordService;
import com.healthydiet.system.service.FoodService;
import com.healthydiet.system.service.NutritionCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DietRecordServiceImpl extends ServiceImpl<DietRecordMapper, DietRecord> implements DietRecordService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final FoodService foodService;
    private final UserProfileMapper userProfileMapper;
    private final NutritionCalculator nutritionCalculator;

    @Override
    @Transactional
    public DietRecordItemResponse createRecord(Long userId, DietRecordRequest request) {
        Food food = foodService.getById(request.getFoodId());
        if (food == null) {
            throw new BusinessException("食物不存在或已删除");
        }

        BigDecimal grams = request.getGrams().setScale(1, RoundingMode.HALF_UP);
        DietRecord record = new DietRecord();
        record.setUserId(userId);
        record.setFoodId(food.getId());
        record.setRecordDate(request.getRecordDate() == null ? LocalDate.now() : request.getRecordDate());
        record.setMealType(cleanMealType(request.getMealType()));
        record.setFoodName(food.getName());
        record.setFoodCategory(food.getCategory());
        record.setGrams(grams);
        record.setCalories(nutrientByGrams(food.getCalories(), grams, 0));
        record.setProtein(nutrientByGrams(food.getProtein(), grams, 1));
        record.setFat(nutrientByGrams(food.getFat(), grams, 1));
        record.setCarbs(nutrientByGrams(food.getCarbs(), grams, 1));
        record.setRemark(StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : null);
        save(record);
        return toItemResponse(record);
    }

    @Override
    public DailyDietRecordResponse getDailyRecords(Long userId, LocalDate date) {
        LocalDate recordDate = date == null ? LocalDate.now() : date;
        List<DietRecord> records = list(new LambdaQueryWrapper<DietRecord>()
                .eq(DietRecord::getUserId, userId)
                .eq(DietRecord::getRecordDate, recordDate)
                .orderByAsc(DietRecord::getMealType)
                .orderByAsc(DietRecord::getCreateTime)
                .orderByAsc(DietRecord::getId));
        ProfileMetricsResponse metrics = getMetrics(userId);
        BigDecimal totalCalories = sum(records, DietRecord::getCalories, 0);
        BigDecimal totalProtein = sum(records, DietRecord::getProtein, 1);
        BigDecimal totalFat = sum(records, DietRecord::getFat, 1);
        BigDecimal totalCarbs = sum(records, DietRecord::getCarbs, 1);
        return new DailyDietRecordResponse(
                recordDate,
                totalCalories,
                totalProtein,
                totalFat,
                totalCarbs,
                metrics == null ? null : metrics.getRecommendedCalories(),
                metrics == null ? null : metrics.getProteinTarget(),
                metrics == null ? null : metrics.getFatTarget(),
                metrics == null ? null : metrics.getCarbsTarget(),
                rate(totalCalories, metrics == null ? null : metrics.getRecommendedCalories()),
                rate(totalProtein, metrics == null ? null : metrics.getProteinTarget()),
                rate(totalFat, metrics == null ? null : metrics.getFatTarget()),
                rate(totalCarbs, metrics == null ? null : metrics.getCarbsTarget()),
                records.stream().map(this::toItemResponse).toList()
        );
    }

    @Override
    public void deleteRecord(Long userId, Long id) {
        DietRecord record = getOne(new LambdaQueryWrapper<DietRecord>()
                .eq(DietRecord::getId, id)
                .eq(DietRecord::getUserId, userId)
                .last("LIMIT 1"));
        if (record == null) {
            throw new BusinessException("饮食记录不存在或无权删除");
        }
        removeById(id);
    }

    private String cleanMealType(String mealType) {
        String value = mealType == null ? "" : mealType.trim();
        if (!List.of("breakfast", "lunch", "dinner", "snack").contains(value)) {
            throw new BusinessException("餐次不正确");
        }
        return value;
    }

    private ProfileMetricsResponse getMetrics(Long userId) {
        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId)
                .last("LIMIT 1"));
        return profile == null ? null : nutritionCalculator.calculate(profile);
    }

    private BigDecimal nutrientByGrams(BigDecimal perHundredGrams, BigDecimal grams, int scale) {
        return perHundredGrams.multiply(grams)
                .divide(ONE_HUNDRED, scale + 2, RoundingMode.HALF_UP)
                .setScale(scale, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<DietRecord> records, NutrientGetter getter, int scale) {
        return records.stream()
                .map(getter::get)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(scale, RoundingMode.HALF_UP);
    }

    private Integer rate(BigDecimal value, BigDecimal target) {
        if (target == null || target.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return value.multiply(BigDecimal.valueOf(100))
                .divide(target, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private DietRecordItemResponse toItemResponse(DietRecord record) {
        return new DietRecordItemResponse(
                record.getId(),
                record.getFoodId(),
                record.getRecordDate(),
                record.getMealType(),
                mealName(record.getMealType()),
                record.getFoodName(),
                record.getFoodCategory(),
                record.getGrams(),
                record.getCalories(),
                record.getProtein(),
                record.getFat(),
                record.getCarbs(),
                record.getRemark(),
                record.getCreateTime()
        );
    }

    private String mealName(String mealType) {
        return switch (mealType) {
            case "breakfast" -> "早餐";
            case "lunch" -> "午餐";
            case "dinner" -> "晚餐";
            case "snack" -> "加餐";
            default -> "其他";
        };
    }

    @FunctionalInterface
    private interface NutrientGetter {
        BigDecimal get(DietRecord record);
    }
}
