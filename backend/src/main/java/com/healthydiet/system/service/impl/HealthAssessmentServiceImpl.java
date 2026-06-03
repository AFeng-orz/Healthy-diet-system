package com.healthydiet.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthydiet.system.common.BusinessException;
import com.healthydiet.system.dto.health.AnswerRequest;
import com.healthydiet.system.dto.health.AssessmentResponse;
import com.healthydiet.system.dto.health.AssessmentSubmitRequest;
import com.healthydiet.system.dto.health.QuestionResponse;
import com.healthydiet.system.entity.HealthAnswer;
import com.healthydiet.system.entity.HealthAssessment;
import com.healthydiet.system.mapper.HealthAssessmentMapper;
import com.healthydiet.system.service.HealthAnswerService;
import com.healthydiet.system.service.HealthAssessmentEvaluator;
import com.healthydiet.system.service.HealthAssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HealthAssessmentServiceImpl extends ServiceImpl<HealthAssessmentMapper, HealthAssessment> implements HealthAssessmentService {

    private final HealthAnswerService healthAnswerService;
    private final HealthAssessmentEvaluator evaluator;

    @Override
    public List<QuestionResponse> getQuestions() {
        return evaluator.getQuestions();
    }

    @Override
    @Transactional
    public AssessmentResponse submit(Long userId, AssessmentSubmitRequest request) {
        AssessmentResponse evaluated;
        try {
            evaluated = evaluator.evaluate(request.getAnswers());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ex.getMessage());
        }

        HealthAssessment assessment = new HealthAssessment();
        assessment.setUserId(userId);
        assessment.setTagCodes(String.join(",", evaluated.getTagCodes()));
        assessment.setTagNames(String.join(",", evaluated.getTagNames()));
        assessment.setSuggestions(String.join("\n", evaluated.getSuggestions()));
        assessment.setTotalScore(evaluated.getTotalScore());
        save(assessment);

        List<HealthAnswer> answers = request.getAnswers().stream()
                .map(answer -> buildAnswer(userId, assessment.getId(), answer))
                .toList();
        healthAnswerService.saveBatch(answers);

        return toResponse(assessment);
    }

    @Override
    public AssessmentResponse getLatest(Long userId) {
        HealthAssessment assessment = getOne(new LambdaQueryWrapper<HealthAssessment>()
                .eq(HealthAssessment::getUserId, userId)
                .orderByDesc(HealthAssessment::getCreateTime)
                .last("LIMIT 1"));
        return assessment == null ? null : toResponse(assessment);
    }

    private HealthAnswer buildAnswer(Long userId, Long assessmentId, AnswerRequest answer) {
        HealthAnswer entity = new HealthAnswer();
        entity.setAssessmentId(assessmentId);
        entity.setUserId(userId);
        entity.setQuestionKey(answer.getQuestionKey());
        entity.setDimension(evaluator.dimension(answer.getQuestionKey()));
        entity.setScore(answer.getScore());
        entity.setAnswerLabel(evaluator.answerLabel(answer.getQuestionKey(), answer.getScore()));
        return entity;
    }

    private AssessmentResponse toResponse(HealthAssessment assessment) {
        return new AssessmentResponse(
                assessment.getId(),
                split(assessment.getTagCodes()),
                split(assessment.getTagNames()),
                splitLines(assessment.getSuggestions()),
                assessment.getTotalScore(),
                assessment.getCreateTime()
        );
    }

    private List<String> split(String value) {
        return Arrays.stream(value.split(","))
                .filter(item -> !item.isBlank())
                .toList();
    }

    private List<String> splitLines(String value) {
        return Arrays.stream(value.split("\\n"))
                .filter(item -> !item.isBlank())
                .toList();
    }
}