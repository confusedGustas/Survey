package org.site.survey.mapper;

import org.site.survey.dto.response.AnswerResponseDTO;
import org.site.survey.dto.response.GroupedSurveyAnswerResponseDTO;
import org.site.survey.dto.response.QuestionGroupedAnswerDTO;
import org.site.survey.dto.response.SurveyAnswerResponseDTO;
import org.site.survey.model.Answer;
import org.site.survey.type.QuestionType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AnswerMapper {

    public AnswerResponseDTO mapToAnswerResponse(Answer answer, String choiceText) {
        return AnswerResponseDTO.builder()
                .id(answer.getId())
                .questionId(answer.getQuestionId())
                .userId(answer.getUserId())
                .choiceId(answer.getChoiceId())
                .isPublic(answer.getIsPublic())
                .createdAt(answer.getCreatedAt())
                .choiceText(choiceText)
                .build();
    }
    
    public GroupedSurveyAnswerResponseDTO transformToGroupedResponse(SurveyAnswerResponseDTO response) {
        if (response == null) {
            return null;
        }
        
        Map<Integer, List<AnswerResponseDTO>> answersByQuestion = response.getAnswers().stream()
                .collect(Collectors.groupingBy(AnswerResponseDTO::getQuestionId));

        List<QuestionGroupedAnswerDTO> groupedAnswers = answersByQuestion.entrySet().stream()
                .map(entry -> {
                    Integer questionId = entry.getKey();
                    List<AnswerResponseDTO> answers = entry.getValue();

                    QuestionType questionType;
                    if (answers.size() > 1) {
                        questionType = QuestionType.MULTIPLE;
                    } else if (answers.get(0).getChoiceId() == null) {
                        questionType = QuestionType.TEXT;
                        answers.forEach(answer -> answer.setChoiceId(null));
                    } else {
                        questionType = QuestionType.SINGLE;
                    }
                    
                    return QuestionGroupedAnswerDTO.builder()
                            .questionId(questionId)
                            .questionType(questionType)
                            .answers(answers)
                            .build();
                })
                .collect(Collectors.toList());
        
        return GroupedSurveyAnswerResponseDTO.builder()
                .surveyId(response.getSurveyId())
                .userId(response.getUserId())
                .submittedAt(response.getSubmittedAt())
                .answers(groupedAnswers)
                .build();
    }
} 