package org.site.survey.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAnswerResponseDTO {
    private Integer surveyId;
    private Integer userId;
    private LocalDateTime submittedAt;
    private List<AnswerResponseDTO> answers;
} 