package com.healthydiet.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.healthydiet.system.dto.profile.ProfileMetricsResponse;
import com.healthydiet.system.dto.report.DailyNutritionTrendResponse;
import com.healthydiet.system.dto.report.WeeklyReportResponse;
import com.healthydiet.system.entity.DietRecord;
import com.healthydiet.system.entity.UserProfile;
import com.healthydiet.system.mapper.DietRecordMapper;
import com.healthydiet.system.mapper.UserProfileMapper;
import com.healthydiet.system.service.NutritionCalculator;
import com.healthydiet.system.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final DietRecordMapper dietRecordMapper;
    private final UserProfileMapper userProfileMapper;
    private final NutritionCalculator nutritionCalculator;

    @Override
    public WeeklyReportResponse getWeeklyReport(Long userId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        ProfileMetricsResponse metrics = getMetrics(userId);
        List<DietRecord> records = dietRecordMapper.selectList(new LambdaQueryWrapper<DietRecord>()
                .eq(DietRecord::getUserId, userId)
                .between(DietRecord::getRecordDate, startDate, endDate)
                .orderByAsc(DietRecord::getRecordDate)
                .orderByAsc(DietRecord::getCreateTime));
        Map<LocalDate, List<DietRecord>> recordsByDate = records.stream()
                .collect(Collectors.groupingBy(DietRecord::getRecordDate));

        List<DailyNutritionTrendResponse> trends = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<DietRecord> dayRecords = recordsByDate.getOrDefault(date, List.of());
            BigDecimal calories = sum(dayRecords, DietRecord::getCalories, 0);
            trends.add(new DailyNutritionTrendResponse(
                    date,
                    calories,
                    sum(dayRecords, DietRecord::getProtein, 1),
                    sum(dayRecords, DietRecord::getFat, 1),
                    sum(dayRecords, DietRecord::getCarbs, 1),
                    rate(calories, metrics == null ? null : metrics.getRecommendedCalories())
            ));
        }

        DailyNutritionTrendResponse today = trends.get(trends.size() - 1);
        BigDecimal averageCalories = trends.stream()
                .map(DailyNutritionTrendResponse::getCalories)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(trends.size()), 0, RoundingMode.HALF_UP);
        int recordedDays = (int) trends.stream()
                .filter(day -> day.getCalories().compareTo(BigDecimal.ZERO) > 0)
                .count();

        return new WeeklyReportResponse(
                startDate,
                endDate,
                metrics == null ? null : metrics.getRecommendedCalories(),
                metrics == null ? null : metrics.getProteinTarget(),
                metrics == null ? null : metrics.getFatTarget(),
                metrics == null ? null : metrics.getCarbsTarget(),
                today.getCalories(),
                today.getProtein(),
                today.getFat(),
                today.getCarbs(),
                today.getCaloriesRate(),
                rate(today.getProtein(), metrics == null ? null : metrics.getProteinTarget()),
                rate(today.getFat(), metrics == null ? null : metrics.getFatTarget()),
                rate(today.getCarbs(), metrics == null ? null : metrics.getCarbsTarget()),
                averageCalories,
                recordedDays,
                buildSummary(metrics, today, recordedDays),
                trends
        );
    }

    private ProfileMetricsResponse getMetrics(Long userId) {
        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId)
                .last("LIMIT 1"));
        return profile == null ? null : nutritionCalculator.calculate(profile);
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

    private String buildSummary(ProfileMetricsResponse metrics, DailyNutritionTrendResponse today, int recordedDays) {
        if (metrics == null) {
            return "先完善健康档案后，系统会结合推荐热量和营养目标生成报告。";
        }
        if (recordedDays == 0) {
            return "最近 7 天还没有饮食记录。先从记录今天的一餐开始，报告会自动形成趋势。";
        }
        Integer rate = today.getCaloriesRate();
        if (rate == null || rate == 0) {
            return "今天还没有记录饮食。记录实际摄入后，可以查看今日完成度和最近 7 天趋势。";
        }
        if (rate < 70) {
            return "今日热量摄入偏低，注意不要长期低于目标过多，优先保证蛋白质和基础能量。";
        }
        if (rate <= 110) {
            return "今日热量接近目标区间，继续保持稳定记录和均衡搭配。";
        }
        return "今日热量已超过目标，晚些时候可以选择清淡低脂食物，并关注明天的摄入节奏。";
    }

    @FunctionalInterface
    private interface NutrientGetter {
        BigDecimal get(DietRecord record);
    }
}
