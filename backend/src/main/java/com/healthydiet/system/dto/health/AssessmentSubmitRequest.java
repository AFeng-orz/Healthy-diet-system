package com.healthydiet.system.dto.health;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AssessmentSubmitRequest {

    @Valid
    @NotEmpty(message = "请先完成问卷")
    private List<AnswerRequest> answers;
}