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
@Table("answers")
public class Answer {
    @Id
    private Integer id;
    private Integer questionId;
    private Integer userId;
    private Integer choiceId;
    private Boolean isPublic;
    private LocalDateTime createdAt;
} 