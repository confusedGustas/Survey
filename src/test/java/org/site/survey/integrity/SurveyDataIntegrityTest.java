package org.site.survey.integrity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.site.survey.dto.request.QuestionRequestDTO;
import org.site.survey.dto.request.SurveyRequestDTO;
import org.site.survey.exception.RequestValidationException;
import org.site.survey.type.QuestionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SurveyDataIntegrityTest {

    private SurveyDataIntegrity surveyDataIntegrity;

    @BeforeEach
    void setUp() {
        surveyDataIntegrity = new SurveyDataIntegrity();
    }

    @Test
    void validateSurveyId_ValidId_DoesNotThrowException() {
        assertDoesNotThrow(() -> surveyDataIntegrity.validateSurveyId(1));
    }

    @Test
    void validateSurveyId_InvalidId_ThrowsException() {
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateSurveyId(null));
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateSurveyId(0));
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateSurveyId(-1));
    }

    @Test
    void validateSurveyTitle_ValidTitle_DoesNotThrowException() {
        assertDoesNotThrow(() -> surveyDataIntegrity.validateSurveyTitle("Valid Title"));
    }

    @Test
    void validateSurveyTitle_InvalidTitle_ThrowsException() {
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateSurveyTitle(null));
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateSurveyTitle(""));
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateSurveyTitle("   "));
        
        String longTitle = "a".repeat(256);
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateSurveyTitle(longTitle));
    }

    @Test
    void validateSurveyTitle_TooLong_ThrowsException() {
        StringBuilder longTitle = new StringBuilder();
        longTitle.append("a".repeat(300));

        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateSurveyTitle(longTitle.toString()));
    }

    @Test
    void validateSurveyDescription_ValidDescription_DoesNotThrowException() {
        assertDoesNotThrow(() -> surveyDataIntegrity.validateSurveyDescription("Valid Description"));
    }

    @Test
    void validateSurveyDescription_InvalidDescription_ThrowsException() {
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateSurveyDescription(null));
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateSurveyDescription(""));
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateSurveyDescription("   "));
        
        String longDescription = "a".repeat(2001);
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateSurveyDescription(longDescription));
    }

    @Test
    void validateSurveyDescription_TooLong_ThrowsException() {
        StringBuilder longDescription = new StringBuilder();
        longDescription.append("a".repeat(2500));

        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateSurveyDescription(longDescription.toString()));
    }

    @Test
    void validateQuestionContent_ValidContent_DoesNotThrowException() {
        assertDoesNotThrow(() -> surveyDataIntegrity.validateQuestionContent("Valid question content?"));
    }

    @Test
    void validateQuestionContent_InvalidContent_ThrowsException() {
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateQuestionContent(null));
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateQuestionContent(""));
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateQuestionContent("   "));
        
        String longContent = "a".repeat(2001);
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateQuestionContent(longContent));
    }

    @Test
    void validateQuestionContent_TooLong_ThrowsException() {
        StringBuilder longContent = new StringBuilder();
        longContent.append("a".repeat(2500));

        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateQuestionContent(longContent.toString()));
    }

    @Test
    void validateChoices_ValidChoices_DoesNotThrowException() {
        List<String> validChoices = Arrays.asList("Choice 1", "Choice 2");
        assertDoesNotThrow(() -> surveyDataIntegrity.validateChoices(validChoices));
    }

    @Test
    void validateChoices_InvalidChoices_ThrowsException() {
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateChoices(null));
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateChoices(Collections.emptyList()));
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateChoices(Collections.singletonList("Only one choice")));
        
        List<String> choicesWithEmptyString = Arrays.asList("Choice 1", "");
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateChoices(choicesWithEmptyString));
        
        List<String> choicesWithNull = Arrays.asList("Choice 1", null);
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateChoices(choicesWithNull));
    }

    @Test
    void validateQuestionDTO_ValidQuestion_DoesNotThrowException() {
        QuestionRequestDTO validTextQuestion = QuestionRequestDTO.builder()
                .content("Valid text question?")
                .questionType(QuestionType.TEXT)
                .build();
        
        assertDoesNotThrow(() -> surveyDataIntegrity.validateQuestionDTO(validTextQuestion));
        
        QuestionRequestDTO validSingleQuestion = QuestionRequestDTO.builder()
                .content("Valid single choice question?")
                .questionType(QuestionType.SINGLE)
                .choices(Arrays.asList("Option 1", "Option 2"))
                .build();
        
        assertDoesNotThrow(() -> surveyDataIntegrity.validateQuestionDTO(validSingleQuestion));
    }

    @Test
    void validateQuestionDTO_InvalidQuestion_ThrowsException() {
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateQuestionDTO(null));
        
        QuestionRequestDTO noContent = QuestionRequestDTO.builder()
                .questionType(QuestionType.TEXT)
                .build();
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateQuestionDTO(noContent));
        
        QuestionRequestDTO noType = QuestionRequestDTO.builder()
                .content("Question with no type")
                .build();
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateQuestionDTO(noType));
        
        QuestionRequestDTO noChoices = QuestionRequestDTO.builder()
                .content("Single choice with no options")
                .questionType(QuestionType.SINGLE)
                .build();
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateQuestionDTO(noChoices));
    }

    @Test
    void validateQuestions_ValidQuestions_DoesNotThrowException() {
        List<QuestionRequestDTO> validQuestions = new ArrayList<>();
        
        QuestionRequestDTO question1 = QuestionRequestDTO.builder()
                .content("Question 1")
                .questionType(QuestionType.TEXT)
                .build();
        validQuestions.add(question1);
        
        QuestionRequestDTO question2 = QuestionRequestDTO.builder()
                .content("Question 2")
                .questionType(QuestionType.SINGLE)
                .choices(Arrays.asList("Option 1", "Option 2"))
                .build();
        validQuestions.add(question2);
        
        assertDoesNotThrow(() -> surveyDataIntegrity.validateQuestions(validQuestions));
    }

    @Test
    void validateQuestions_InvalidQuestions_ThrowsException() {
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateQuestions(null));
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateQuestions(Collections.emptyList()));
        
        List<QuestionRequestDTO> invalidQuestions = new ArrayList<>();
        invalidQuestions.add(null);
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateQuestions(invalidQuestions));
    }

    @Test
    void validateSurveyRequest_ValidRequest_DoesNotThrowException() {
        List<QuestionRequestDTO> questions = new ArrayList<>();
        QuestionRequestDTO question = QuestionRequestDTO.builder()
                .content("Valid question?")
                .questionType(QuestionType.TEXT)
                .build();
        questions.add(question);
        
        SurveyRequestDTO validRequest = SurveyRequestDTO.builder()
                .title("Valid Survey")
                .description("This is a valid survey description")
                .questions(questions)
                .build();
        
        assertDoesNotThrow(() -> surveyDataIntegrity.validateSurveyRequest(validRequest));
    }

    @Test
    void validateSurveyRequest_InvalidRequest_ThrowsException() {
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateSurveyRequest(null));
        
        SurveyRequestDTO noTitle = SurveyRequestDTO.builder()
                .description("Description")
                .questions(Collections.emptyList())
                .build();
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateSurveyRequest(noTitle));
        
        SurveyRequestDTO noDescription = SurveyRequestDTO.builder()
                .title("Title")
                .questions(Collections.emptyList())
                .build();
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateSurveyRequest(noDescription));
        
        SurveyRequestDTO noQuestions = SurveyRequestDTO.builder()
                .title("Title")
                .description("Description")
                .build();
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateSurveyRequest(noQuestions));
    }

    @Test
    void validateUserId_ValidId_DoesNotThrowException() {
        assertDoesNotThrow(() -> surveyDataIntegrity.validateUserId(1));
    }

    @Test
    void validateUserId_InvalidId_ThrowsException() {
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateUserId(null));
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateUserId(0));
        assertThrows(RequestValidationException.class, () -> surveyDataIntegrity.validateUserId(-1));
    }
} 
