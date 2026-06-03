package com.healthydiet.system.dto.health;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QuestionResponse {

    private String key;

    private String dimension;

    private String title;

    private String helpText;

    private List<QuestionOptionResponse> options;
}