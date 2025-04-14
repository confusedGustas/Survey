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
public class SurveyResponseDTO {
    private Integer id;
    private String title;
    private String description;
    private Integer createdBy;
    private LocalDateTime createdAt;
    private List<QuestionResponseDTO> questions;
} 