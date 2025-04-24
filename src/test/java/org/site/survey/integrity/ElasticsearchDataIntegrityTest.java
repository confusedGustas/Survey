package org.site.survey.integrity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.site.survey.exception.ElasticsearchException;
import org.site.survey.exception.RequestValidationException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ElasticsearchDataIntegrityTest {

    private ElasticsearchDataIntegrity elasticsearchDataIntegrity;
    
    @BeforeEach
    void setUp() {
        elasticsearchDataIntegrity = new ElasticsearchDataIntegrity();
    }
    
    @Test
    void validateSearchQuery_ValidQuery_DoesNotThrow() {
        assertDoesNotThrow(() -> elasticsearchDataIntegrity.validateSearchQuery("valid query"));
    }
    
    @Test
    void validateSearchQuery_NullQuery_ThrowsRequestValidationException() {
        assertThrows(RequestValidationException.class, () -> elasticsearchDataIntegrity.validateSearchQuery(null));
    }
    
    @Test
    void validateSearchQuery_EmptyQuery_ThrowsRequestValidationException() {
        assertThrows(RequestValidationException.class, () -> elasticsearchDataIntegrity.validateSearchQuery(""));
    }
    
    @Test
    void validateSearchQuery_TooShortQuery_ThrowsElasticsearchException() {
        assertThrows(ElasticsearchException.class, () -> elasticsearchDataIntegrity.validateSearchQuery("a"));
    }
    
    @Test
    void validateSearchQuery_TooLongQuery_ThrowsElasticsearchException() {
        assertThrows(ElasticsearchException.class, () -> elasticsearchDataIntegrity.validateSearchQuery("a".repeat(501)));
    }
    
    @Test
    void validateSurveyId_ValidId_DoesNotThrow() {
        assertDoesNotThrow(() -> elasticsearchDataIntegrity.validateSurveyId(1));
    }
    
    @Test
    void validateSurveyId_NullId_ThrowsRequestValidationException() {
        assertThrows(RequestValidationException.class, () -> elasticsearchDataIntegrity.validateSurveyId(null));
    }
    
    @Test
    void validateSurveyId_ZeroId_ThrowsRequestValidationException() {
        assertThrows(RequestValidationException.class, () -> elasticsearchDataIntegrity.validateSurveyId(0));
    }
    
    @Test
    void validateSurveyId_NegativeId_ThrowsRequestValidationException() {
        assertThrows(RequestValidationException.class, () -> elasticsearchDataIntegrity.validateSurveyId(-1));
    }
    
    @Test
    void validateQuestionId_ValidId_DoesNotThrow() {
        assertDoesNotThrow(() -> elasticsearchDataIntegrity.validateQuestionId(1));
    }
    
    @Test
    void validateQuestionId_NullId_ThrowsRequestValidationException() {
        assertThrows(RequestValidationException.class, () -> elasticsearchDataIntegrity.validateQuestionId(null));
    }
    
    @Test
    void validateQuestionId_ZeroId_ThrowsRequestValidationException() {
        assertThrows(RequestValidationException.class, () -> elasticsearchDataIntegrity.validateQuestionId(0));
    }
    
    @Test
    void validateQuestionId_NegativeId_ThrowsRequestValidationException() {
        assertThrows(RequestValidationException.class, () -> elasticsearchDataIntegrity.validateQuestionId(-1));
    }
    
    @Test
    void validateUserId_ValidId_DoesNotThrow() {
        assertDoesNotThrow(() -> elasticsearchDataIntegrity.validateUserId(1));
    }
    
    @Test
    void validateUserId_NullId_ThrowsRequestValidationException() {
        assertThrows(RequestValidationException.class, () -> elasticsearchDataIntegrity.validateUserId(null));
    }
    
    @Test
    void validateUserId_ZeroId_ThrowsRequestValidationException() {
        assertThrows(RequestValidationException.class, () -> elasticsearchDataIntegrity.validateUserId(0));
    }
    
    @Test
    void validateUserId_NegativeId_ThrowsRequestValidationException() {
        assertThrows(RequestValidationException.class, () -> elasticsearchDataIntegrity.validateUserId(-1));
    }
    
    @Test
    void validateQuestionType_ValidType_DoesNotThrow() {
        assertDoesNotThrow(() -> elasticsearchDataIntegrity.validateQuestionType("MULTIPLE"));
    }
    
    @Test
    void validateQuestionType_NullType_ThrowsRequestValidationException() {
        assertThrows(RequestValidationException.class, () -> elasticsearchDataIntegrity.validateQuestionType(null));
    }
    
    @Test
    void validateQuestionType_EmptyType_ThrowsRequestValidationException() {
        assertThrows(RequestValidationException.class, () -> elasticsearchDataIntegrity.validateQuestionType(""));
    }
    
    @Test
    void validatePublicFlag_ValidFlag_DoesNotThrow() {
        assertDoesNotThrow(() -> elasticsearchDataIntegrity.validatePublicFlag(true));
    }
    
    @Test
    void validatePublicFlag_NullFlag_ThrowsRequestValidationException() {
        assertThrows(RequestValidationException.class, () -> elasticsearchDataIntegrity.validatePublicFlag(null));
    }
} 