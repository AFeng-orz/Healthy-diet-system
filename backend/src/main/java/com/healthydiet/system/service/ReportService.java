package com.healthydiet.system.service;

import com.healthydiet.system.dto.report.WeeklyReportResponse;

public interface ReportService {

    WeeklyReportResponse getWeeklyReport(Long userId);
}
