package org.site.survey.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SurveyHasAnswersExceptionTest {

    @Test
    void getStatus_ShouldReturnBadRequest() {
        SurveyHasAnswersException exception = new SurveyHasAnswersException();
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void getErrorCode_ShouldReturnSurveyHasAnswers() {
        SurveyHasAnswersException exception = new SurveyHasAnswersException();
        assertEquals("SURVEY_HAS_ANSWERS", exception.getErrorCode());
    }

    @Test
    void getMessage_ShouldReturnErrorMessage() {
        SurveyHasAnswersException exception = new SurveyHasAnswersException();
        assertEquals("Cannot delete survey with existing answers", exception.getMessage());
    }
} 
