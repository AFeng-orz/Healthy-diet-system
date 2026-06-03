package com.healthydiet.system.dto.health;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuestionOptionResponse {

    private String label;

    private Integer score;
}