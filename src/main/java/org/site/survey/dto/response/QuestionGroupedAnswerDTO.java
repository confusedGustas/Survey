package org.site.survey.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.site.survey.type.QuestionType;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionGroupedAnswerDTO {
    private Integer questionId;
    private QuestionType questionType;
    private List<AnswerResponseDTO> answers;
} 