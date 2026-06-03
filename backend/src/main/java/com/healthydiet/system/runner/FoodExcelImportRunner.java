package com.healthydiet.system.runner;

import com.healthydiet.system.dto.food.FoodImportResult;
import com.healthydiet.system.service.FoodExcelImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.food-import", name = "enabled", havingValue = "true")
public class FoodExcelImportRunner implements ApplicationRunner {

    private final FoodExcelImportService foodExcelImportService;
    private final ConfigurableApplicationContext applicationContext;

    @Value("${app.food-import.file-path}")
    private String filePath;

    @Value("${app.food-import.exit-after-import:false}")
    private boolean exitAfterImport;

    @Override
    public void run(ApplicationArguments args) {
        Path excelPath = Paths.get(filePath).toAbsolutePath().normalize();
        FoodImportResult result = foodExcelImportService.importFrom(excelPath);
        log.info(
                "Food Excel import finished. file={}, totalRows={}, insertedRows={}, updatedRows={}, skippedRows={}, duplicateRemovedRows={}",
                excelPath,
                result.getTotalRows(),
                result.getInsertedRows(),
                result.getUpdatedRows(),
                result.getSkippedRows(),
                result.getDuplicateRemovedRows()
        );
        if (exitAfterImport) {
            applicationContext.close();
        }
    }
}
