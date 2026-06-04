package com.healthydiet.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.healthydiet.system.dto.record.DailyDietRecordResponse;
import com.healthydiet.system.dto.record.DietRecordItemResponse;
import com.healthydiet.system.dto.record.DietRecordRequest;
import com.healthydiet.system.entity.DietRecord;

import java.time.LocalDate;

public interface DietRecordService extends IService<DietRecord> {

    DietRecordItemResponse createRecord(Long userId, DietRecordRequest request);

    DailyDietRecordResponse getDailyRecords(Long userId, LocalDate date);

    void deleteRecord(Long userId, Long id);
}
