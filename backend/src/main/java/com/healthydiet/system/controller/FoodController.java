package com.healthydiet.system.controller;

import com.healthydiet.system.common.PageResult;
import com.healthydiet.system.common.Result;
import com.healthydiet.system.common.BusinessException;
import com.healthydiet.system.dto.food.FoodImportResult;
import com.healthydiet.system.dto.food.FoodQueryRequest;
import com.healthydiet.system.dto.food.FoodRequest;
import com.healthydiet.system.entity.Food;
import com.healthydiet.system.service.FoodExcelImportService;
import com.healthydiet.system.service.FoodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;
    private final FoodExcelImportService foodExcelImportService;

    @GetMapping
    public Result<PageResult<Food>> pageFoods(FoodQueryRequest request) {
        return Result.success(foodService.pageFoods(request));
    }

    @PostMapping
    public Result<Food> createFood(@Valid @RequestBody FoodRequest request) {
        return Result.success(foodService.createFood(request));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<FoodImportResult> importFoods(@RequestParam("file") MultipartFile file) {
        validateExcelFile(file);
        try {
            return Result.success(foodExcelImportService.importFrom(file.getInputStream()));
        } catch (IOException exception) {
            throw new BusinessException("读取上传文件失败：" + exception.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Food> updateFood(@PathVariable Long id, @Valid @RequestBody FoodRequest request) {
        return Result.success(foodService.updateFood(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteFood(@PathVariable Long id) {
        foodService.deleteFood(id);
        return Result.success();
    }

    private void validateExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请上传食物 Excel 文件");
        }
        String filename = file.getOriginalFilename();
        String lowerFilename = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        if (!lowerFilename.endsWith(".xlsx") && !lowerFilename.endsWith(".xls")) {
            throw new BusinessException("仅支持 .xlsx 或 .xls 格式的 Excel 文件");
        }
    }
}
