package org.site.survey.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestionHasAnswersExceptionTest {

    @Test
    void getStatus_ShouldReturnBadRequest() {
        QuestionHasAnswersException exception = new QuestionHasAnswersException();
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void getErrorCode_ShouldReturnQuestionHasAnswers() {
        QuestionHasAnswersException exception = new QuestionHasAnswersException();
        assertEquals("QUESTION_HAS_ANSWERS", exception.getErrorCode());
    }

    @Test
    void getMessage_ShouldReturnErrorMessage() {
        QuestionHasAnswersException exception = new QuestionHasAnswersException();
        assertEquals("Cannot delete question with existing answers", exception.getMessage());
    }
} 