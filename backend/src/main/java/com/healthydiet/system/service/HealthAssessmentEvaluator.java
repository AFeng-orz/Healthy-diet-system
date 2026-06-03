package com.healthydiet.system.service;

import com.healthydiet.system.dto.health.AnswerRequest;
import com.healthydiet.system.dto.health.AssessmentResponse;
import com.healthydiet.system.dto.health.QuestionOptionResponse;
import com.healthydiet.system.dto.health.QuestionResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class HealthAssessmentEvaluator {

    private static final List<QuestionResponse> QUESTIONS = List.of(
            question("staple_ratio", "diet", "你最近一周主食、蔬菜和蛋白质搭配是否均衡？", "关注餐盘结构，而不是单纯少吃。",
                    option("比较均衡，每餐都有蔬菜蛋白质", 0),
                    option("偶尔不均衡，但能注意搭配", 1),
                    option("经常随便吃，主食或油脂占比高", 2)),
            question("protein_intake", "diet", "你每天是否有稳定的优质蛋白来源？", "例如鸡蛋、鱼虾、瘦肉、奶制品、豆制品。",
                    option("基本每天都有", 0),
                    option("有时有，有时忽略", 1),
                    option("很少专门关注蛋白质", 2)),
            question("weekly_exercise", "exercise", "你最近一周运动或主动步行的频率如何？", "运动不足会影响能量消耗和计划执行。",
                    option("每周3次以上", 0),
                    option("每周1-2次", 1),
                    option("几乎没有", 2)),
            question("daily_sitting", "exercise", "你每天久坐时间大概有多长？", "久坐可以通过短时活动打断。",
                    option("少于6小时", 0),
                    option("6-9小时", 1),
                    option("9小时以上", 2)),
            question("sleep_stress", "sleep", "你最近一周睡眠和压力状态如何？", "睡眠和压力会影响食欲与执行力。",
                    option("睡眠规律，压力可控", 0),
                    option("偶尔熬夜或压力较大", 1),
                    option("经常熬夜，压力明显", 2)),
            question("water_intake", "hydration", "你每天饮水量大概是多少？", "这里指白水或无糖饮品。",
                    option("约1500ml以上", 0),
                    option("约800-1500ml", 1),
                    option("少于800ml", 2)),
            question("snack_drink", "calorie", "你摄入甜饮、零食、油炸食品的频率如何？", "高热量偏好会影响热量缺口。",
                    option("很少", 0),
                    option("每周几次", 1),
                    option("几乎每天", 2)),
            question("eating_speed", "habit", "你吃饭速度和饱腹感控制如何？", "慢一点吃更容易感知饱腹。",
                    option("吃得较慢，七八分饱会停", 0),
                    option("有时吃太快或吃撑", 1),
                    option("经常吃很快，容易吃撑", 2))
    );

    public List<QuestionResponse> getQuestions() {
        return QUESTIONS;
    }

    public AssessmentResponse evaluate(List<AnswerRequest> answers) {
        Map<String, QuestionResponse> questionMap = QUESTIONS.stream()
                .collect(Collectors.toMap(QuestionResponse::getKey, question -> question));
        Set<String> answerKeys = answers.stream().map(AnswerRequest::getQuestionKey).collect(Collectors.toSet());
        if (!answerKeys.containsAll(questionMap.keySet()) || answers.size() != QUESTIONS.size()) {
            throw new IllegalArgumentException("请完成全部问卷题目");
        }

        Map<String, Integer> dimensionScores = new LinkedHashMap<>();
        int totalScore = 0;
        for (AnswerRequest answer : answers) {
            QuestionResponse question = questionMap.get(answer.getQuestionKey());
            if (question == null) {
                throw new IllegalArgumentException("问卷题目不存在");
            }
            int score = answer.getScore();
            totalScore += score;
            dimensionScores.merge(question.getDimension(), score, Integer::sum);
        }

        List<String> tagCodes = new ArrayList<>();
        List<String> tagNames = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        addIfRisk(dimensionScores, "diet", 3, tagCodes, tagNames, suggestions,
                "diet_unbalanced", "饮食不均衡型", "优先保证每餐有蔬菜和优质蛋白，再控制主食和油脂比例。");
        addIfRisk(dimensionScores, "exercise", 3, tagCodes, tagNames, suggestions,
                "low_activity", "运动不足型", "先从每天多走10分钟开始，再逐步增加力量训练或有氧运动。");
        addIfRisk(dimensionScores, "sleep", 2, tagCodes, tagNames, suggestions,
                "sleep_stress", "作息压力型", "尽量固定睡眠时间，晚间减少高热量零食和刺激性饮品。");
        addIfRisk(dimensionScores, "hydration", 2, tagCodes, tagNames, suggestions,
                "low_hydration", "饮水不足型", "把饮水拆到上午、下午和运动后，不必一次喝很多。");
        addIfRisk(dimensionScores, "calorie", 2, tagCodes, tagNames, suggestions,
                "high_calorie_preference", "高热量偏好型", "减少甜饮和油炸食品频率，用水果、酸奶或坚果小份替代部分零食。");
        addIfRisk(dimensionScores, "habit", 2, tagCodes, tagNames, suggestions,
                "fast_eating", "进食节奏偏快型", "每餐先吃蔬菜和蛋白质，放慢速度，给饱腹感留出时间。");

        if (tagCodes.isEmpty()) {
            tagCodes.add("balanced_lifestyle");
            tagNames.add("基础习惯良好型");
            suggestions.add("当前生活方式基础较好，后续重点是保持稳定记录，并让饮食计划更贴近你的偏好。");
        }

        return new AssessmentResponse(null, tagCodes, tagNames, suggestions, totalScore, LocalDateTime.now());
    }

    public String answerLabel(String questionKey, Integer score) {
        return QUESTIONS.stream()
                .filter(question -> question.getKey().equals(questionKey))
                .flatMap(question -> question.getOptions().stream())
                .filter(option -> option.getScore().equals(score))
                .map(QuestionOptionResponse::getLabel)
                .findFirst()
                .orElse("");
    }

    public String dimension(String questionKey) {
        return QUESTIONS.stream()
                .filter(question -> question.getKey().equals(questionKey))
                .map(QuestionResponse::getDimension)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("问卷题目不存在"));
    }

    private static QuestionResponse question(String key, String dimension, String title, String helpText, QuestionOptionResponse... options) {
        return new QuestionResponse(key, dimension, title, helpText, List.of(options));
    }

    private static QuestionOptionResponse option(String label, Integer score) {
        return new QuestionOptionResponse(label, score);
    }

    private void addIfRisk(Map<String, Integer> scores, String dimension, int threshold, List<String> tagCodes,
                           List<String> tagNames, List<String> suggestions, String code, String name, String suggestion) {
        if (scores.getOrDefault(dimension, 0) >= threshold) {
            tagCodes.add(code);
            tagNames.add(name);
            suggestions.add(suggestion);
        }
    }
}