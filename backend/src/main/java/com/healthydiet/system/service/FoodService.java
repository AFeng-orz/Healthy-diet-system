package com.healthydiet.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.healthydiet.system.common.PageResult;
import com.healthydiet.system.dto.food.FoodQueryRequest;
import com.healthydiet.system.dto.food.FoodRequest;
import com.healthydiet.system.entity.Food;

public interface FoodService extends IService<Food> {

    PageResult<Food> pageFoods(FoodQueryRequest request);

    Food createFood(FoodRequest request);

    Food updateFood(Long id, FoodRequest request);

    void deleteFood(Long id);
}