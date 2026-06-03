package com.healthydiet.system.dto.food;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodImportResult {

    private int totalRows;

    private int insertedRows;

    private int updatedRows;

    private int skippedRows;

    private int duplicateRemovedRows;

    private Map<String, Integer> categoryStats = new LinkedHashMap<>();
}
