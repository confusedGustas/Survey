package org.site.survey.integrity;

import org.site.survey.dto.request.QuestionRequestDTO;
import org.site.survey.dto.request.SurveyRequestDTO;
import org.site.survey.exception.RequestValidationException;
import org.site.survey.type.QuestionType;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class SurveyDataIntegrity {
    
    private static final int MAX_TITLE_LENGTH = 255;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;
    private static final int MAX_QUESTION_CONTENT_LENGTH = 2000;
    private static final int MIN_CHOICES_FOR_SINGLE_OR_MULTIPLE = 2;
    
    public void validateSurveyId(Integer id) {
        if (id == null || id <= 0) {
            throw new RequestValidationException();
        }
    }
    
    public void validateSurveyTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new RequestValidationException();
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new RequestValidationException();
        }
    }
    
    public void validateSurveyDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new RequestValidationException();
        }
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new RequestValidationException();
        }
    }
    
    public void validateQuestionContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new RequestValidationException();
        }
        if (content.length() > MAX_QUESTION_CONTENT_LENGTH) {
            throw new RequestValidationException();
        }
    }
    
    public void validateQuestions(List<QuestionRequestDTO> questions) {
        if (questions == null || questions.isEmpty()) {
            throw new RequestValidationException();
        }
        
        for (QuestionRequestDTO question : questions) {
            validateQuestionDTO(question);
        }
    }
    
    public void validateQuestionDTO(QuestionRequestDTO question) {
        if (question == null) {
            throw new RequestValidationException();
        }
        
        validateQuestionContent(question.getContent());
        
        if (question.getQuestionType() == null) {
            throw new RequestValidationException();
        }
        
        if (question.getQuestionType() == QuestionType.SINGLE || 
            question.getQuestionType() == QuestionType.MULTIPLE) {
            validateChoices(question.getChoices());
        }
    }
    
    public void validateChoices(List<String> choices) {
        if (choices == null || choices.size() < MIN_CHOICES_FOR_SINGLE_OR_MULTIPLE) {
            throw new RequestValidationException();
        }
        
        for (String choice : choices) {
            if (choice == null || choice.trim().isEmpty()) {
                throw new RequestValidationException();
            }
        }
    }
    
    public void validateSurveyRequest(SurveyRequestDTO request) {
        if (request == null) {
            throw new RequestValidationException();
        }
        
        validateSurveyTitle(request.getTitle());
        validateSurveyDescription(request.getDescription());
        validateQuestions(request.getQuestions());
    }
    
    public void validateUserId(Integer userId) {
        if (userId == null || userId <= 0) {
            throw new RequestValidationException();
        }
    }
} 