package org.site.survey.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResponseDTO {
    private Integer id;
    private Integer questionId;
    private Integer userId;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer choiceId;
    
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private String choiceText;
} 