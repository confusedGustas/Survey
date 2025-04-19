package org.site.survey.integrity;

import org.site.survey.exception.ElasticsearchException;
import org.site.survey.exception.RequestValidationException;
import org.springframework.stereotype.Component;

@Component
public class ElasticsearchDataIntegrity {
    
    private static final int MAX_QUERY_LENGTH = 500;
    private static final int MIN_QUERY_LENGTH = 2;
    
    public void validateSearchQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new RequestValidationException("Search query cannot be empty");
        }
        
        if (query.length() < MIN_QUERY_LENGTH) {
            throw new ElasticsearchException("Search query too short - minimum length is " + MIN_QUERY_LENGTH);
        }
        
        if (query.length() > MAX_QUERY_LENGTH) {
            throw new ElasticsearchException("Search query too long - maximum length is " + MAX_QUERY_LENGTH);
        }
    }
    
    public void validateSurveyId(Integer surveyId) {
        if (surveyId == null || surveyId <= 0) {
            throw new RequestValidationException("Survey ID must be a positive integer");
        }
    }
    
    public void validateQuestionId(Integer questionId) {
        if (questionId == null || questionId <= 0) {
            throw new RequestValidationException("Question ID must be a positive integer");
        }
    }
    
    public void validateUserId(Integer userId) {
        if (userId == null || userId <= 0) {
            throw new RequestValidationException("User ID must be a positive integer");
        }
    }
    
    public void validateQuestionType(String questionType) {
        if (questionType == null || questionType.trim().isEmpty()) {
            throw new RequestValidationException("Question type cannot be empty");
        }
    }
    
    public void validatePublicFlag(Boolean isPublic) {
        if (isPublic == null) {
            throw new RequestValidationException("Public flag must be specified");
        }
    }
} 