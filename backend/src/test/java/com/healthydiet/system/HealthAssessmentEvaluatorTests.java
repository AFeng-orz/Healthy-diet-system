package com.healthydiet.system;

import com.healthydiet.system.dto.health.AnswerRequest;
import com.healthydiet.system.dto.health.AssessmentResponse;
import com.healthydiet.system.service.HealthAssessmentEvaluator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HealthAssessmentEvaluatorTests {

    private final HealthAssessmentEvaluator evaluator = new HealthAssessmentEvaluator();

    @Test
    void shouldGenerateLifestyleTagsFromRiskAnswers() {
        List<AnswerRequest> answers = evaluator.getQuestions().stream()
                .map(question -> answer(question.getKey(), 2))
                .toList();

        AssessmentResponse response = evaluator.evaluate(answers);

        assertTrue(response.getTagNames().contains("饮食不均衡型"));
        assertTrue(response.getTagNames().contains("运动不足型"));
        assertTrue(response.getTagNames().contains("作息压力型"));
        assertTrue(response.getSuggestions().size() >= 3);
    }

    private AnswerRequest answer(String key, Integer score) {
        AnswerRequest request = new AnswerRequest();
        request.setQuestionKey(key);
        request.setScore(score);
        return request;
    }
}