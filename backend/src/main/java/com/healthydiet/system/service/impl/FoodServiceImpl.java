package com.healthydiet.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthydiet.system.common.BusinessException;
import com.healthydiet.system.common.PageResult;
import com.healthydiet.system.dto.food.FoodQueryRequest;
import com.healthydiet.system.dto.food.FoodRequest;
import com.healthydiet.system.entity.Food;
import com.healthydiet.system.mapper.FoodMapper;
import com.healthydiet.system.service.FoodService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class FoodServiceImpl extends ServiceImpl<FoodMapper, Food> implements FoodService {

    @Override
    public PageResult<Food> pageFoods(FoodQueryRequest request) {
        long pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
        long pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : Math.min(request.getPageSize(), 50);

        LambdaQueryWrapper<Food> wrapper = new LambdaQueryWrapper<Food>()
                .like(StringUtils.hasText(request.getKeyword()), Food::getName, clean(request.getKeyword()))
                .eq(StringUtils.hasText(request.getCategory()), Food::getCategory, clean(request.getCategory()))
                .like(StringUtils.hasText(request.getMealTag()), Food::getMealTags, clean(request.getMealTag()))
                .eq(request.getHighSugar() != null, Food::getHighSugar, boolToInt(request.getHighSugar()))
                .eq(request.getHighFat() != null, Food::getHighFat, boolToInt(request.getHighFat()))
                .orderByAsc(Food::getCategory)
                .orderByAsc(Food::getName)
                .orderByAsc(Food::getId);

        Page<Food> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.from(page);
    }

    @Override
    public Food createFood(FoodRequest request) {
        Food food = new Food();
        copyRequest(food, request);
        save(food);
        return food;
    }

    @Override
    public Food updateFood(Long id, FoodRequest request) {
        Food food = getById(id);
        if (food == null) {
            throw new BusinessException("食物不存在或已删除");
        }
        copyRequest(food, request);
        updateById(food);
        return food;
    }

    @Override
    public void deleteFood(Long id) {
        Food food = getById(id);
        if (food == null) {
            throw new BusinessException("食物不存在或已删除");
        }
        removeById(id);
    }

    private void copyRequest(Food food, FoodRequest request) {
        food.setName(clean(request.getName()));
        food.setCategory(clean(request.getCategory()));
        food.setCalories(request.getCalories());
        food.setProtein(request.getProtein());
        food.setFat(request.getFat());
        food.setCarbs(request.getCarbs());
        food.setMealTags(clean(request.getMealTags()));
        food.setHighSugar(boolToInt(request.getHighSugar()));
        food.setHighFat(boolToInt(request.getHighFat()));
        food.setRemark(clean(request.getRemark()));
    }

    private String clean(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private Integer boolToInt(Boolean value) {
        return Boolean.TRUE.equals(value) ? 1 : 0;
    }
}
