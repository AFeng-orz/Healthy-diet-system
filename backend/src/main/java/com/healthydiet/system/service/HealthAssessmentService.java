package com.healthydiet.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.healthydiet.system.dto.health.AssessmentResponse;
import com.healthydiet.system.dto.health.AssessmentSubmitRequest;
import com.healthydiet.system.dto.health.QuestionResponse;
import com.healthydiet.system.entity.HealthAssessment;

import java.util.List;

public interface HealthAssessmentService extends IService<HealthAssessment> {

    List<QuestionResponse> getQuestions();

    AssessmentResponse submit(Long userId, AssessmentSubmitRequest request);

    AssessmentResponse getLatest(Long userId);
}