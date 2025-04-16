package org.site.survey.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDTO {
    private Long totalSurveys;
    private Long totalQuestions;
    private Long totalChoices;
    private Long totalAnswers;
    private Long totalUsers;
    private Map<String, Long> questionTypeStats;
} 