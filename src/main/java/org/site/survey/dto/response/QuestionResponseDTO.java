package org.site.survey.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.site.survey.type.QuestionType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponseDTO {
    private Integer id;
    private Integer surveyId;
    private String content;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private QuestionType questionType;
    
    private Integer questionSize;
    private LocalDateTime createdAt;
    private List<ChoiceResponseDTO> choices;
} 