package com.healthydiet.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.healthydiet.system.dto.diet.DietPlanResponse;
import com.healthydiet.system.entity.DietPlan;

public interface DietPlanService extends IService<DietPlan> {

    DietPlanResponse generate(Long userId);

    DietPlanResponse getLatest(Long userId);

    DietPlanResponse getPlan(Long userId, Long planId);
}
