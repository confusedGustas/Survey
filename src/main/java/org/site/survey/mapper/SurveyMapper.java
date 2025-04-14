package org.site.survey.mapper;

import org.site.survey.dto.response.ChoiceResponseDTO;
import org.site.survey.dto.response.QuestionResponseDTO;
import org.site.survey.dto.response.SurveyResponseDTO;
import org.site.survey.model.Choice;
import org.site.survey.model.Question;
import org.site.survey.model.Survey;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SurveyMapper {

    public ChoiceResponseDTO mapToChoiceResponse(Choice choice) {
        return ChoiceResponseDTO.builder()
                .id(choice.getId())
                .questionId(choice.getQuestionId())
                .choiceText(choice.getChoiceText())
                .build();
    }
    
    public QuestionResponseDTO mapToQuestionResponse(Question question, List<ChoiceResponseDTO> choices) {
        return QuestionResponseDTO.builder()
                .id(question.getId())
                .surveyId(question.getSurveyId())
                .content(question.getContent())
                .questionType(question.getQuestionTypeEnum())
                .questionSize(question.getQuestionSize())
                .createdAt(question.getCreatedAt())
                .choices(choices != null ? choices : new ArrayList<>())
                .build();
    }
    
    public SurveyResponseDTO mapToSurveyResponse(Survey survey, List<QuestionResponseDTO> questions) {
        return SurveyResponseDTO.builder()
                .id(survey.getId())
                .title(survey.getTitle())
                .description(survey.getDescription())
                .createdBy(survey.getCreatedBy())
                .createdAt(survey.getCreatedAt())
                .questions(questions != null ? questions : new ArrayList<>())
                .build();
    }
} 