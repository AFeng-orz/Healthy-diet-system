package com.healthydiet.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthydiet.system.common.BusinessException;
import com.healthydiet.system.dto.diet.DietPlanItemResponse;
import com.healthydiet.system.dto.diet.DietPlanResponse;
import com.healthydiet.system.dto.health.AssessmentResponse;
import com.healthydiet.system.dto.profile.ProfileMetricsResponse;
import com.healthydiet.system.entity.DietPlan;
import com.healthydiet.system.entity.DietPlanItem;
import com.healthydiet.system.entity.Food;
import com.healthydiet.system.entity.UserProfile;
import com.healthydiet.system.mapper.DietPlanMapper;
import com.healthydiet.system.mapper.UserProfileMapper;
import com.healthydiet.system.service.DietPlanItemService;
import com.healthydiet.system.service.DietPlanService;
import com.healthydiet.system.service.FoodService;
import com.healthydiet.system.service.HealthAssessmentService;
import com.healthydiet.system.service.NutritionCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DietPlanServiceImpl extends ServiceImpl<DietPlanMapper, DietPlan> implements DietPlanService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal MIN_GRAMS = BigDecimal.valueOf(50);
    private static final BigDecimal MAX_GRAMS = BigDecimal.valueOf(250);

    private final DietPlanItemService dietPlanItemService;
    private final UserProfileMapper userProfileMapper;
    private final FoodService foodService;
    private final HealthAssessmentService healthAssessmentService;
    private final NutritionCalculator nutritionCalculator;

    @Override
    @Transactional
    public DietPlanResponse generate(Long userId) {
        UserProfile profile = getProfile(userId);
        ProfileMetricsResponse metrics = nutritionCalculator.calculate(profile);
        AssessmentResponse assessment = healthAssessmentService.getLatest(userId);
        List<Food> foods = usableFoods(profile);

        List<DietPlanItem> items = new ArrayList<>();
        Set<Long> usedFoodIds = new HashSet<>();
        createMealItems("breakfast", metrics.getRecommendedCalories(), BigDecimal.valueOf(0.25), foods, profile, assessment, usedFoodIds, items);
        createMealItems("lunch", metrics.getRecommendedCalories(), BigDecimal.valueOf(0.40), foods, profile, assessment, usedFoodIds, items);
        createMealItems("dinner", metrics.getRecommendedCalories(), BigDecimal.valueOf(0.35), foods, profile, assessment, usedFoodIds, items);

        if (items.isEmpty()) {
            throw new BusinessException("食物库中暂无可用于生成计划的食物");
        }

        DietPlan plan = new DietPlan();
        plan.setUserId(userId);
        plan.setPlanDate(LocalDate.now());
        plan.setTargetCalories(metrics.getRecommendedCalories());
        plan.setSummary(buildSummary(profile, assessment));
        fillPlanTotals(plan, items);
        save(plan);

        items.forEach(item -> item.setPlanId(plan.getId()));
        dietPlanItemService.saveBatch(items);
        return toResponse(plan, items);
    }

    @Override
    public DietPlanResponse getLatest(Long userId) {
        DietPlan plan = getOne(new LambdaQueryWrapper<DietPlan>()
                .eq(DietPlan::getUserId, userId)
                .orderByDesc(DietPlan::getCreateTime)
                .last("LIMIT 1"));
        return plan == null ? null : toResponse(plan, getItems(plan.getId()));
    }

    @Override
    public DietPlanResponse getPlan(Long userId, Long planId) {
        DietPlan plan = getOne(new LambdaQueryWrapper<DietPlan>()
                .eq(DietPlan::getId, planId)
                .eq(DietPlan::getUserId, userId)
                .last("LIMIT 1"));
        if (plan == null) {
            throw new BusinessException("饮食计划不存在或无权查看");
        }
        return toResponse(plan, getItems(plan.getId()));
    }

    private UserProfile getProfile(Long userId) {
        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId)
                .last("LIMIT 1"));
        if (profile == null) {
            throw new BusinessException("请先完善健康档案，再生成饮食计划");
        }
        return profile;
    }

    private List<Food> usableFoods(UserProfile profile) {
        List<String> blockedKeywords = splitKeywords(profile.getAvoidFoods(), profile.getAllergies());
        List<Food> foods = foodService.list(new LambdaQueryWrapper<Food>()
                .gt(Food::getCalories, BigDecimal.ZERO)
                .orderByAsc(Food::getCategory)
                .orderByAsc(Food::getCalories)
                .orderByAsc(Food::getId));
        return foods.stream()
                .filter(food -> !containsBlockedKeyword(food.getName(), blockedKeywords))
                .filter(food -> !Boolean.TRUE.equals(food.getHighSugar() != null && food.getHighSugar() == 1))
                .toList();
    }

    private List<String> splitKeywords(String... values) {
        List<String> keywords = new ArrayList<>();
        for (String value : values) {
            if (!StringUtils.hasText(value)) {
                continue;
            }
            String[] parts = value.split("[,，、\\s]+");
            for (String part : parts) {
                if (StringUtils.hasText(part)) {
                    keywords.add(part.trim());
                }
            }
        }
        return keywords;
    }

    private boolean containsBlockedKeyword(String foodName, List<String> blockedKeywords) {
        if (!StringUtils.hasText(foodName)) {
            return false;
        }
        return blockedKeywords.stream().anyMatch(foodName::contains);
    }

    private void createMealItems(
            String mealType,
            BigDecimal targetCalories,
            BigDecimal ratio,
            List<Food> foods,
            UserProfile profile,
            AssessmentResponse assessment,
            Set<Long> usedFoodIds,
            List<DietPlanItem> result
    ) {
        BigDecimal mealCalories = targetCalories.multiply(ratio);
        List<MealSlot> slots = switch (mealType) {
            case "breakfast" -> List.of(
                    new MealSlot("主食", BigDecimal.valueOf(0.45)),
                    new MealSlot("蛋白质", BigDecimal.valueOf(0.35)),
                    new MealSlot("水果", BigDecimal.valueOf(0.20))
            );
            case "lunch" -> List.of(
                    new MealSlot("主食", BigDecimal.valueOf(0.45)),
                    new MealSlot("蛋白质", BigDecimal.valueOf(0.35)),
                    new MealSlot("蔬菜", BigDecimal.valueOf(0.20))
            );
            default -> List.of(
                    new MealSlot("主食", BigDecimal.valueOf(0.35)),
                    new MealSlot("蛋白质", BigDecimal.valueOf(0.40)),
                    new MealSlot("蔬菜", BigDecimal.valueOf(0.25))
            );
        };

        int baseOrder = switch (mealType) {
            case "breakfast" -> 10;
            case "lunch" -> 20;
            default -> 30;
        };
        for (int i = 0; i < slots.size(); i++) {
            MealSlot slot = slots.get(i);
            Food food = chooseFood(foods, slot.category(), mealType, usedFoodIds);
            if (food == null) {
                continue;
            }
            usedFoodIds.add(food.getId());
            BigDecimal foodTargetCalories = mealCalories.multiply(slot.ratio());
            result.add(buildItem(mealType, food, foodTargetCalories, profile, assessment, baseOrder + i));
        }
    }

    private Food chooseFood(List<Food> foods, String category, String mealType, Set<Long> usedFoodIds) {
        return foods.stream()
                .filter(food -> category.equals(food.getCategory()))
                .filter(food -> !usedFoodIds.contains(food.getId()))
                .filter(food -> mealMatch(food, mealType))
                .min(Comparator.comparing(Food::getHighFat)
                        .thenComparing(Food::getCalories)
                        .thenComparing(Food::getId))
                .orElseGet(() -> foods.stream()
                        .filter(food -> category.equals(food.getCategory()))
                        .filter(food -> !usedFoodIds.contains(food.getId()))
                        .min(Comparator.comparing(Food::getHighFat)
                                .thenComparing(Food::getCalories)
                                .thenComparing(Food::getId))
                        .orElse(null));
    }

    private boolean mealMatch(Food food, String mealType) {
        if (!StringUtils.hasText(food.getMealTags())) {
            return true;
        }
        return food.getMealTags().contains(mealName(mealType));
    }

    private DietPlanItem buildItem(
            String mealType,
            Food food,
            BigDecimal targetCalories,
            UserProfile profile,
            AssessmentResponse assessment,
            Integer sortOrder
    ) {
        BigDecimal grams = targetCalories
                .divide(food.getCalories(), 4, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED);
        grams = clamp(grams, MIN_GRAMS, MAX_GRAMS).setScale(0, RoundingMode.HALF_UP);

        DietPlanItem item = new DietPlanItem();
        item.setFoodId(food.getId());
        item.setMealType(mealType);
        item.setFoodName(food.getName());
        item.setFoodCategory(food.getCategory());
        item.setGrams(grams);
        item.setCalories(nutrientByGrams(food.getCalories(), grams, 0));
        item.setProtein(nutrientByGrams(food.getProtein(), grams, 1));
        item.setFat(nutrientByGrams(food.getFat(), grams, 1));
        item.setCarbs(nutrientByGrams(food.getCarbs(), grams, 1));
        item.setReason(reason(food, profile, assessment));
        item.setSortOrder(sortOrder);
        return item;
    }

    private BigDecimal clamp(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value.compareTo(min) < 0) {
            return min;
        }
        if (value.compareTo(max) > 0) {
            return max;
        }
        return value;
    }

    private BigDecimal nutrientByGrams(BigDecimal perHundredGrams, BigDecimal grams, int scale) {
        return perHundredGrams.multiply(grams)
                .divide(ONE_HUNDRED, scale + 2, RoundingMode.HALF_UP)
                .setScale(scale, RoundingMode.HALF_UP);
    }

    private String reason(Food food, UserProfile profile, AssessmentResponse assessment) {
        List<String> parts = new ArrayList<>();
        parts.add(switch (food.getCategory()) {
            case "主食" -> "提供稳定碳水，帮助维持饱腹感";
            case "蛋白质" -> "补充优质蛋白，支持肌肉维持和恢复";
            case "蔬菜" -> "增加膳食纤维和微量营养素";
            case "水果" -> "作为轻量加餐来源，补充天然风味";
            default -> "补充本餐营养结构";
        });
        if ("fat_loss".equals(profile.getDietGoal())) {
            parts.add("当前目标为减脂，已按推荐热量控制份量");
        } else if ("muscle_gain".equals(profile.getDietGoal())) {
            parts.add("当前目标为增肌，优先保证蛋白质和总能量");
        }
        if (assessment != null && assessment.getTagNames() != null && !assessment.getTagNames().isEmpty()) {
            parts.add("结合最近健康画像：" + String.join("、", assessment.getTagNames()));
        }
        return String.join("；", parts);
    }

    private String buildSummary(UserProfile profile, AssessmentResponse assessment) {
        String goalText = switch (profile.getDietGoal()) {
            case "fat_loss" -> "减脂";
            case "muscle_gain" -> "增肌";
            default -> "维持";
        };
        if (assessment == null || assessment.getTagNames() == null || assessment.getTagNames().isEmpty()) {
            return "根据健康档案生成一日三餐计划，当前目标为" + goalText + "。";
        }
        return "根据健康档案和最近健康画像生成计划，当前目标为" + goalText + "，重点关注："
                + String.join("、", assessment.getTagNames()) + "。";
    }

    private void fillPlanTotals(DietPlan plan, List<DietPlanItem> items) {
        plan.setTotalCalories(sum(items, DietPlanItem::getCalories, 0));
        plan.setTotalProtein(sum(items, DietPlanItem::getProtein, 1));
        plan.setTotalFat(sum(items, DietPlanItem::getFat, 1));
        plan.setTotalCarbs(sum(items, DietPlanItem::getCarbs, 1));
    }

    private BigDecimal sum(List<DietPlanItem> items, NutrientGetter getter, int scale) {
        return items.stream()
                .map(getter::get)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(scale, RoundingMode.HALF_UP);
    }

    private List<DietPlanItem> getItems(Long planId) {
        return dietPlanItemService.list(new LambdaQueryWrapper<DietPlanItem>()
                .eq(DietPlanItem::getPlanId, planId)
                .orderByAsc(DietPlanItem::getSortOrder)
                .orderByAsc(DietPlanItem::getId));
    }

    private DietPlanResponse toResponse(DietPlan plan, List<DietPlanItem> items) {
        return new DietPlanResponse(
                plan.getId(),
                plan.getPlanDate(),
                plan.getTargetCalories(),
                plan.getTotalCalories(),
                plan.getTotalProtein(),
                plan.getTotalFat(),
                plan.getTotalCarbs(),
                plan.getSummary(),
                items.stream().map(this::toItemResponse).toList(),
                plan.getCreateTime()
        );
    }

    private DietPlanItemResponse toItemResponse(DietPlanItem item) {
        return new DietPlanItemResponse(
                item.getId(),
                item.getFoodId(),
                item.getMealType(),
                mealName(item.getMealType()),
                item.getFoodName(),
                item.getFoodCategory(),
                item.getGrams(),
                item.getCalories(),
                item.getProtein(),
                item.getFat(),
                item.getCarbs(),
                item.getReason(),
                item.getSortOrder()
        );
    }

    private String mealName(String mealType) {
        return switch (mealType) {
            case "breakfast" -> "早餐";
            case "lunch" -> "午餐";
            case "dinner" -> "晚餐";
            default -> "其他";
        };
    }

    private record MealSlot(String category, BigDecimal ratio) {
    }

    @FunctionalInterface
    private interface NutrientGetter {
        BigDecimal get(DietPlanItem item);
    }
}
