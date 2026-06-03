package com.healthydiet.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.healthydiet.system.common.BusinessException;
import com.healthydiet.system.dto.food.FoodImportResult;
import com.healthydiet.system.entity.Food;
import com.healthydiet.system.mapper.FoodMapper;
import com.healthydiet.system.service.FoodExcelImportService;
import com.healthydiet.system.service.FoodImportRuleEngine;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FoodExcelImportServiceImpl implements FoodExcelImportService {

    private static final String HEADER_NAME = "食物名称";
    private static final String HEADER_CALORIES = "能量";
    private static final String HEADER_PROTEIN = "蛋白质";
    private static final String HEADER_FAT = "脂肪";
    private static final String HEADER_CARBS = "碳水化合物";

    private final FoodMapper foodMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FoodImportResult importFrom(Path excelPath) {
        if (excelPath == null || !Files.exists(excelPath)) {
            throw new BusinessException("食物 Excel 文件不存在：" + excelPath);
        }

        try (InputStream inputStream = Files.newInputStream(excelPath)) {
            return importFrom(inputStream);
        } catch (IOException exception) {
            throw new BusinessException("打开食物 Excel 文件失败：" + exception.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FoodImportResult importFrom(InputStream inputStream) {
        if (inputStream == null) {
            throw new BusinessException("食物 Excel 文件不能为空");
        }

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Map<String, Integer> headerMap = readHeader(sheet, formatter, evaluator);
            validateHeaders(headerMap);
            return importRows(sheet, formatter, evaluator, headerMap);
        } catch (IOException exception) {
            throw new BusinessException("读取食物 Excel 文件失败：" + exception.getMessage());
        }
    }

    private FoodImportResult importRows(
            Sheet sheet,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            Map<String, Integer> headerMap
    ) {
        FoodImportResult result = new FoodImportResult();
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            result.setTotalRows(result.getTotalRows() + 1);
            Food food = buildFood(row, formatter, evaluator, headerMap);
            if (food == null) {
                result.setSkippedRows(result.getSkippedRows() + 1);
                continue;
            }
            result.getCategoryStats().merge(food.getCategory(), 1, Integer::sum);
            upsertFood(food, result);
        }
        return result;
    }

    private Food buildFood(
            Row row,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            Map<String, Integer> headerMap
    ) {
        if (row == null) {
            return null;
        }

        String name = FoodImportRuleEngine.normalizeName(readText(row, headerMap.get(HEADER_NAME), formatter, evaluator));
        BigDecimal calories = readNumber(row, headerMap.get(HEADER_CALORIES), formatter, evaluator);
        BigDecimal protein = readNumber(row, headerMap.get(HEADER_PROTEIN), formatter, evaluator);
        BigDecimal fat = readNumber(row, headerMap.get(HEADER_FAT), formatter, evaluator);
        BigDecimal carbs = readNumber(row, headerMap.get(HEADER_CARBS), formatter, evaluator);
        if (!StringUtils.hasText(name)) {
            return null;
        }
        calories = defaultZero(calories);
        protein = defaultZero(protein);
        fat = defaultZero(fat);
        carbs = defaultZero(carbs);

        String category = FoodImportRuleEngine.inferCategory(name);
        Food food = new Food();
        food.setName(name);
        food.setCategory(category);
        food.setCalories(calories);
        food.setProtein(protein);
        food.setFat(fat);
        food.setCarbs(carbs);
        food.setMealTags(FoodImportRuleEngine.inferMealTags(name, category));
        food.setHighSugar(FoodImportRuleEngine.inferHighSugar(name, category, carbs));
        food.setHighFat(FoodImportRuleEngine.inferHighFat(category, fat));
        food.setRemark(FoodImportRuleEngine.buildRemark());
        return food;
    }

    private void upsertFood(Food food, FoodImportResult result) {
        List<Food> existingFoods = foodMapper.selectList(new LambdaQueryWrapper<Food>()
                .eq(Food::getName, food.getName())
                .orderByAsc(Food::getId));

        if (existingFoods.isEmpty()) {
            foodMapper.insert(food);
            result.setInsertedRows(result.getInsertedRows() + 1);
            return;
        }

        Food existingFood = existingFoods.get(0);
        food.setId(existingFood.getId());
        foodMapper.updateById(food);
        result.setUpdatedRows(result.getUpdatedRows() + 1);

        for (int index = 1; index < existingFoods.size(); index++) {
            foodMapper.deleteById(existingFoods.get(index).getId());
            result.setDuplicateRemovedRows(result.getDuplicateRemovedRows() + 1);
        }
    }

    private Map<String, Integer> readHeader(Sheet sheet, DataFormatter formatter, FormulaEvaluator evaluator) {
        Row headerRow = sheet.getRow(0);
        Map<String, Integer> headerMap = new HashMap<>();
        if (headerRow == null) {
            return headerMap;
        }
        for (Cell cell : headerRow) {
            String header = formatter.formatCellValue(cell, evaluator).trim();
            if (StringUtils.hasText(header)) {
                headerMap.put(header, cell.getColumnIndex());
            }
        }
        return headerMap;
    }

    private void validateHeaders(Map<String, Integer> headerMap) {
        for (String header : List.of(HEADER_NAME, HEADER_CALORIES, HEADER_PROTEIN, HEADER_FAT, HEADER_CARBS)) {
            if (!headerMap.containsKey(header)) {
                throw new BusinessException("食物 Excel 缺少必要字段：" + header);
            }
        }
    }

    private String readText(Row row, Integer columnIndex, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (row == null || columnIndex == null) {
            return null;
        }
        Cell cell = row.getCell(columnIndex);
        return cell == null ? null : formatter.formatCellValue(cell, evaluator);
    }

    private BigDecimal readNumber(Row row, Integer columnIndex, DataFormatter formatter, FormulaEvaluator evaluator) {
        return FoodImportRuleEngine.parseFirstNumber(readText(row, columnIndex, formatter, evaluator));
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
