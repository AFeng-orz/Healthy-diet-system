package com.healthydiet.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthydiet.system.entity.DietPlanItem;
import com.healthydiet.system.mapper.DietPlanItemMapper;
import com.healthydiet.system.service.DietPlanItemService;
import org.springframework.stereotype.Service;

@Service
public class DietPlanItemServiceImpl extends ServiceImpl<DietPlanItemMapper, DietPlanItem> implements DietPlanItemService {
}
