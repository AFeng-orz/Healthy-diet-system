package com.healthydiet.system.dto.health;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class AssessmentResponse {

    private Long id;

    private List<String> tagCodes;

    private List<String> tagNames;

    private List<String> suggestions;

    private Integer totalScore;

    private LocalDateTime createTime;
}