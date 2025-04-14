package org.site.survey.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.site.survey.dto.request.QuestionRequestDTO;
import org.site.survey.dto.request.SurveyRequestDTO;
import org.site.survey.dto.response.ChoiceResponseDTO;
import org.site.survey.dto.response.QuestionResponseDTO;
import org.site.survey.dto.response.SurveyResponseDTO;
import org.site.survey.exception.SurveyHasAnswersException;
import org.site.survey.exception.SurveyNotFoundException;
import org.site.survey.exception.UnauthorizedSurveyAccessException;
import org.site.survey.integrity.SurveyDataIntegrity;
import org.site.survey.mapper.SurveyMapper;
import org.site.survey.model.Choice;
import org.site.survey.model.Question;
import org.site.survey.model.Survey;
import org.site.survey.repository.AnswerRepository;
import org.site.survey.repository.ChoiceRepository;
import org.site.survey.repository.QuestionRepository;
import org.site.survey.repository.SurveyRepository;
import org.site.survey.type.QuestionType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SurveyServiceTest {

    @Mock
    private SurveyRepository surveyRepository;
    
    @Mock
    private QuestionRepository questionRepository;
    
    @Mock
    private ChoiceRepository choiceRepository;
    
    @Mock
    private AnswerRepository answerRepository;
    
    @Mock
    private SurveyDataIntegrity surveyDataIntegrity;
    
    @Mock
    private SurveyMapper surveyMapper;
    
    private SurveyService surveyService;
    
    @BeforeEach
    void setUp() {
        try (AutoCloseable ignored = MockitoAnnotations.openMocks(this)) {
            surveyService = new SurveyService(
                    surveyRepository,
                    questionRepository,
                    choiceRepository,
                    answerRepository,
                    surveyDataIntegrity,
                    surveyMapper
            );
            
            ReflectionTestUtils.setField(surveyService, "surveyDataIntegrity", surveyDataIntegrity);
            ReflectionTestUtils.setField(surveyService, "surveyMapper", surveyMapper);

            doNothing().when(surveyDataIntegrity).validateSurveyRequest(any());
            doNothing().when(surveyDataIntegrity).validateUserId(anyInt());
            doNothing().when(surveyDataIntegrity).validateSurveyId(anyInt());

            Survey survey = Survey.builder()
                    .id(1)
                    .title("Test Survey")
                    .description("A test survey")
                    .createdBy(1)
                    .createdAt(LocalDateTime.now())
                    .build();
                    
            Question question = Question.builder()
                    .id(1)
                    .surveyId(1)
                    .content("Test Question")
                    .questionType("TEXT")
                    .questionSize(0)
                    .createdAt(LocalDateTime.now())
                    .build();
                    
            SurveyResponseDTO expectedResponse = surveyMapper.mapToSurveyResponse(survey, null);
                    
            when(surveyRepository.save(any(Survey.class))).thenReturn(Mono.just(survey));
            when(questionRepository.save(any(Question.class))).thenReturn(Mono.just(question));
            when(surveyMapper.mapToSurveyResponse(any(Survey.class), any())).thenReturn(expectedResponse);
            
            SurveyRequestDTO requestDTO = SurveyRequestDTO.builder()
                    .title("Test Survey")
                    .description("A test survey")
                    .questions(List.of())
                    .build();

            StepVerifier.create(surveyService.createSurvey(requestDTO, 1))
                    .verifyComplete();

            Survey survey1 = Survey.builder()
                    .id(1)
                    .title("Survey 1")
                    .description("First survey")
                    .createdBy(1)
                    .createdAt(LocalDateTime.now())
                    .build();
                    
            Survey survey2 = Survey.builder()
                    .id(2)
                    .title("Survey 2")
                    .description("Second survey")
                    .createdBy(1)
                    .createdAt(LocalDateTime.now())
                    .build();
                    
            Question question1 = Question.builder()
                    .id(1)
                    .surveyId(1)
                    .content("Question 1")
                    .questionType("TEXT")
                    .questionSize(0)
                    .createdAt(LocalDateTime.now())
                    .build();

            QuestionResponseDTO questionResponseDTO1 = QuestionResponseDTO.builder()
                    .id(1)
                    .content("Question 1")
                    .questionType(QuestionType.TEXT)
                    .choices(Collections.emptyList())
                    .build();
                    
            SurveyResponseDTO surveyResponseDTO1 = SurveyResponseDTO.builder()
                    .id(1)
                    .title("Survey 1")
                    .description("First survey")
                    .createdBy(1)
                    .createdAt(survey1.getCreatedAt())
                    .questions(List.of(questionResponseDTO1))
                    .build();
                    
            SurveyResponseDTO surveyResponseDTO2 = SurveyResponseDTO.builder()
                    .id(2)
                    .title("Survey 2")
                    .description("Second survey")
                    .createdBy(1)
                    .createdAt(survey2.getCreatedAt())
                    .questions(Collections.emptyList())
                    .build();

            when(surveyRepository.findByCreatedBy(1)).thenReturn(Flux.just(survey1, survey2));
            when(questionRepository.findBySurveyId(1)).thenReturn(Flux.just(question1));
            when(questionRepository.findBySurveyId(2)).thenReturn(Flux.empty());
            when(choiceRepository.findByQuestionId(1)).thenReturn(Flux.empty());
            when(surveyMapper.mapToQuestionResponse(eq(question1), anyList())).thenReturn(questionResponseDTO1);
            when(surveyMapper.mapToSurveyResponse(eq(survey1), anyList())).thenReturn(surveyResponseDTO1);
            when(surveyMapper.mapToSurveyResponse(eq(survey2), anyList())).thenReturn(surveyResponseDTO2);

            StepVerifier.create(surveyService.getAllSurveysByUser(1))
                    .expectNext(surveyResponseDTO1, surveyResponseDTO2)
                    .verifyComplete();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize mocks", e);
        }
    }
    
    @Test
    void createSurvey_WithoutQuestions_ReturnsSurveyResponseWithoutQuestions() {
        Integer userId = 1;
        SurveyRequestDTO requestDTO = new SurveyRequestDTO();
        requestDTO.setTitle("Test Survey");
        requestDTO.setDescription("Test Description");
        
        StepVerifier.create(surveyService.createSurvey(requestDTO, userId))
                .verifyComplete();
    }
    
    @Test
    void createSurvey_WithQuestions_ReturnsSurveyResponseWithQuestions() {
        Integer userId = 1;
        
        QuestionRequestDTO questionRequestDTO = new QuestionRequestDTO();
        questionRequestDTO.setContent("Test Question");
        questionRequestDTO.setQuestionType(QuestionType.SINGLE);
        questionRequestDTO.setChoices(List.of("Choice 1", "Choice 2"));
        
        SurveyRequestDTO requestDTO = new SurveyRequestDTO();
        requestDTO.setTitle("Test Survey");
        requestDTO.setDescription("Test Description");
        requestDTO.setQuestions(List.of(questionRequestDTO));
        
        Survey savedSurvey = Survey.builder()
                .id(1)
                .title("Test Survey")
                .description("Test Description")
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
                .build();
                
        Question savedQuestion = Question.builder()
                .id(1)
                .surveyId(savedSurvey.getId())
                .content("Test Question")
                .questionType("SINGLE")
                .questionSize(2)
                .createdAt(LocalDateTime.now())
                .build();
                
        Choice savedChoice1 = Choice.builder()
                .id(1)
                .questionId(savedQuestion.getId())
                .choiceText("Choice 1")
                .build();
                
        Choice savedChoice2 = Choice.builder()
                .id(2)
                .questionId(savedQuestion.getId())
                .choiceText("Choice 2")
                .build();
        
        ChoiceResponseDTO choiceResponseDTO1 = ChoiceResponseDTO.builder()
                .id(1)
                .questionId(1)
                .choiceText("Choice 1")
                .build();
        ChoiceResponseDTO choiceResponseDTO2 = ChoiceResponseDTO.builder()
                .id(2)
                .questionId(1)
                .choiceText("Choice 2")
                .build();
        
        List<ChoiceResponseDTO> choiceResponses = List.of(choiceResponseDTO1, choiceResponseDTO2);
        
        QuestionResponseDTO questionResponseDTO = QuestionResponseDTO.builder()
                .id(1)
                .content("Test Question")
                .questionType(QuestionType.SINGLE)
                .choices(choiceResponses)
                .build();
        
        SurveyResponseDTO expectedResponse = SurveyResponseDTO.builder()
                .id(1)
                .title("Test Survey")
                .description("Test Description")
                .createdBy(userId)
                .createdAt(savedSurvey.getCreatedAt())
                .questions(List.of(questionResponseDTO))
                .build();
        
        when(surveyRepository.save(any(Survey.class))).thenReturn(Mono.just(savedSurvey));
        when(questionRepository.save(any(Question.class))).thenReturn(Mono.just(savedQuestion));
        when(choiceRepository.save(any(Choice.class)))
                .thenReturn(Mono.just(savedChoice1))
                .thenReturn(Mono.just(savedChoice2));
        
        when(surveyMapper.mapToSurveyResponse(eq(savedSurvey), any())).thenReturn(expectedResponse);
        when(surveyMapper.mapToQuestionResponse(eq(savedQuestion), anyList())).thenReturn(questionResponseDTO);
        when(surveyMapper.mapToChoiceResponse(eq(savedChoice1))).thenReturn(choiceResponseDTO1);
        when(surveyMapper.mapToChoiceResponse(eq(savedChoice2))).thenReturn(choiceResponseDTO2);
        
        StepVerifier.create(surveyService.createSurvey(requestDTO, userId))
                .expectNext(expectedResponse)
                .verifyComplete();
    }
    
    @Test
    void getAllSurveysByUser_ReturnsUserSurveys() {
        Integer userId = 1;
        
        Survey survey = Survey.builder()
                .id(1)
                .title("Test Survey")
                .description("Test Description")
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
                .build();
                
        Question question = Question.builder()
                .id(1)
                .surveyId(survey.getId())
                .content("Test Question")
                .questionType("SINGLE")
                .questionSize(2)
                .createdAt(LocalDateTime.now())
                .build();
                
        Choice choice = Choice.builder()
                .id(1)
                .questionId(question.getId())
                .choiceText("Choice 1")
                .build();
        
        ChoiceResponseDTO choiceResponseDTO = ChoiceResponseDTO.builder()
                .id(1)
                .questionId(1)
                .choiceText("Choice 1")
                .build();
        
        QuestionResponseDTO questionResponseDTO = QuestionResponseDTO.builder()
                .id(1)
                .content("Test Question")
                .questionType(QuestionType.SINGLE)
                .choices(List.of(choiceResponseDTO))
                .build();
        
        SurveyResponseDTO surveyResponseDTO = SurveyResponseDTO.builder()
                .id(1)
                .title("Test Survey")
                .description("Test Description")
                .createdBy(userId)
                .createdAt(survey.getCreatedAt())
                .questions(List.of(questionResponseDTO))
                .build();
        
        when(surveyRepository.findByCreatedBy(userId)).thenReturn(Flux.just(survey));
        when(questionRepository.findBySurveyId(survey.getId())).thenReturn(Flux.just(question));
        when(choiceRepository.findByQuestionId(question.getId())).thenReturn(Flux.just(choice));
        
        when(surveyMapper.mapToChoiceResponse(choice)).thenReturn(choiceResponseDTO);
        when(surveyMapper.mapToQuestionResponse(eq(question), anyList())).thenReturn(questionResponseDTO);
        when(surveyMapper.mapToSurveyResponse(eq(survey), anyList())).thenReturn(surveyResponseDTO);
        
        StepVerifier.create(surveyService.getAllSurveysByUser(userId))
                .expectNext(surveyResponseDTO)
                .verifyComplete();
    }
    
    @Test
    void getAllSurveysByUser_UserHasNoSurveys_ReturnsEmptyList() {
        Integer userId = 1;
        
        when(surveyRepository.findByCreatedBy(userId)).thenReturn(Flux.empty());
        
        StepVerifier.create(surveyService.getAllSurveysByUser(userId))
                .verifyComplete();
    }
    
    @Test
    void deleteSurvey_SurveyExists_DeletesSuccessfully() {
        Integer surveyId = 1;
        Integer userId = 1;
        
        Survey survey = Survey.builder()
                .id(surveyId)
                .title("Test Survey")
                .description("Test Description")
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
                .build();
                
        Question question = Question.builder()
                .id(1)
                .surveyId(surveyId)
                .content("Test Question")
                .questionType("SINGLE")
                .questionSize(2)
                .createdAt(LocalDateTime.now())
                .build();
        
        when(surveyRepository.findById(surveyId)).thenReturn(Mono.just(survey));
        when(questionRepository.findBySurveyId(surveyId)).thenReturn(Flux.just(question));
        when(answerRepository.existsByQuestionIdIn(List.of(question.getId()))).thenReturn(Mono.just(false));
        when(choiceRepository.deleteByQuestionId(question.getId())).thenReturn(Mono.empty());
        when(questionRepository.deleteBySurveyId(surveyId)).thenReturn(Mono.empty());
        when(surveyRepository.delete(survey)).thenReturn(Mono.empty());
        
        StepVerifier.create(surveyService.deleteSurvey(surveyId, userId))
                .verifyComplete();
        
        verify(surveyRepository).delete(survey);
        verify(questionRepository).deleteBySurveyId(surveyId);
        verify(choiceRepository).deleteByQuestionId(question.getId());
    }
    
    @Test
    void deleteSurvey_SurveyNotFound_ThrowsException() {
        Integer surveyId = 999;
        Integer userId = 1;
        
        when(surveyRepository.findById(surveyId)).thenReturn(Mono.empty());
        
        StepVerifier.create(surveyService.deleteSurvey(surveyId, userId))
                .expectError(SurveyNotFoundException.class)
                .verify();
        
        verify(surveyRepository, never()).delete(any());
        verify(questionRepository, never()).deleteBySurveyId(anyInt());
        verify(choiceRepository, never()).deleteByQuestionId(anyInt());
    }
    
    @Test
    void deleteSurvey_UnauthorizedUser_ThrowsException() {
        Integer surveyId = 1;
        Integer userId = 1;
        Integer differentUserId = 2;
        
        Survey survey = Survey.builder()
                .id(surveyId)
                .title("Test Survey")
                .description("Test Description")
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
                .build();
        
        when(surveyRepository.findById(surveyId)).thenReturn(Mono.just(survey));
        
        StepVerifier.create(surveyService.deleteSurvey(surveyId, differentUserId))
                .expectError(UnauthorizedSurveyAccessException.class)
                .verify();
        
        verify(surveyRepository, never()).delete(any());
    }
    
    @Test
    void deleteSurvey_SurveyHasAnswers_ThrowsException() {
        Integer surveyId = 1;
        Integer userId = 1;
        
        Survey survey = Survey.builder()
                .id(surveyId)
                .title("Test Survey")
                .description("Test Description")
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
                .build();
                
        Question question = Question.builder()
                .id(1)
                .surveyId(surveyId)
                .content("Test Question")
                .questionType("SINGLE")
                .questionSize(2)
                .createdAt(LocalDateTime.now())
                .build();
        
        when(surveyRepository.findById(surveyId)).thenReturn(Mono.just(survey));
        when(questionRepository.findBySurveyId(surveyId)).thenReturn(Flux.just(question));
        when(answerRepository.existsByQuestionIdIn(List.of(question.getId()))).thenReturn(Mono.just(true));
        
        StepVerifier.create(surveyService.deleteSurvey(surveyId, userId))
                .expectError(SurveyHasAnswersException.class)
                .verify();
        
        verify(surveyRepository, never()).delete(any());
    }
} 
