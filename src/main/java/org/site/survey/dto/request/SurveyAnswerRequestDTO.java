package org.site.survey.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAnswerRequestDTO {
    @NotNull(message = "Survey ID is required")
    private Integer surveyId;
    
    @NotEmpty(message = "Answers are required")
    @Valid
    private List<QuestionAnswerDTO> answers;
} 