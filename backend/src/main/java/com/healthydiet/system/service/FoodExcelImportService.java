package com.healthydiet.system.service;

import com.healthydiet.system.dto.food.FoodImportResult;

import java.io.InputStream;
import java.nio.file.Path;

public interface FoodExcelImportService {

    FoodImportResult importFrom(Path excelPath);

    FoodImportResult importFrom(InputStream inputStream);
}
