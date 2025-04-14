package org.site.survey.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.site.survey.type.QuestionType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("questions")
public class Question {
    @Id
    private Integer id;
    private Integer surveyId;
    private String content;
    private String questionType;
    private Integer questionSize;
    private LocalDateTime createdAt;

    public QuestionType getQuestionTypeEnum() {
        if (questionType == null) {
            return null;
        }
        return QuestionType.valueOf(questionType);
    }
} 