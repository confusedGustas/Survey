package org.site.survey.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private Integer questionType;
    private Integer questionSize;
    private LocalDateTime createdAt;
}