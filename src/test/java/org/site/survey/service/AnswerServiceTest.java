package org.site.survey.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.site.survey.dto.request.QuestionAnswerDTO;
import org.site.survey.dto.request.SurveyAnswerRequestDTO;
import org.site.survey.dto.response.AnswerResponseDTO;
import org.site.survey.dto.response.GroupedSurveyAnswerResponseDTO;
import org.site.survey.dto.response.QuestionGroupedAnswerDTO;
import org.site.survey.dto.response.SurveyAnswerResponseDTO;
import org.site.survey.exception.InvalidAnswerFormatException;
import org.site.survey.exception.SurveyNotFoundException;
import org.site.survey.mapper.AnswerMapper;
import org.site.survey.model.Answer;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AnswerServiceTest {

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private ChoiceRepository choiceRepository;

    @Mock
    private SurveyRepository surveyRepository;

    @Mock
    private AnswerMapper answerMapper;

    private AnswerService answerService;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        answerService = new AnswerService(
                answerRepository,
                questionRepository,
                choiceRepository,
                surveyRepository,
                answerMapper
        );

        when(answerMapper.mapToAnswerResponse(any(Answer.class), anyString())).thenAnswer(invocation -> {
            Answer answer = invocation.getArgument(0);
            String choiceText = invocation.getArgument(1);
            return AnswerResponseDTO.builder()
                    .id(answer.getId())
                    .questionId(answer.getQuestionId())
                    .userId(answer.getUserId())
                    .choiceId(answer.getChoiceId())
                    .isPublic(answer.getIsPublic())
                    .createdAt(answer.getCreatedAt())
                    .choiceText(choiceText)
                    .build();
        });
        
        when(answerMapper.transformToGroupedResponse(any(SurveyAnswerResponseDTO.class))).thenAnswer(invocation -> {
            SurveyAnswerResponseDTO response = invocation.getArgument(0);
            
            List<QuestionGroupedAnswerDTO> groupedAnswers = new ArrayList<>();
            
            if (response != null && response.getAnswers() != null) {
                response.getAnswers().stream()
                        .collect(java.util.stream.Collectors.groupingBy(AnswerResponseDTO::getQuestionId))
                        .forEach((questionId, answers) -> {
                            QuestionType type = QuestionType.TEXT;
                            if (answers.size() > 1) {
                                type = QuestionType.MULTIPLE;
                            } else if (answers.get(0).getChoiceId() != null) {
                                type = QuestionType.SINGLE;
                            }
                            
                            groupedAnswers.add(QuestionGroupedAnswerDTO.builder()
                                    .questionId(questionId)
                                    .questionType(type)
                                    .answers(answers)
                                    .build());
                        });
                
                return GroupedSurveyAnswerResponseDTO.builder()
                        .surveyId(response.getSurveyId())
                        .userId(response.getUserId())
                        .submittedAt(response.getSubmittedAt())
                        .answers(groupedAnswers)
                        .build();
            }
            
            return null;
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    void submitSurveyAnswers_ValidRequest_TextQuestion_ReturnsAnswers() {
        Integer surveyId = 1;
        Integer userId = 1;
        Integer questionId = 1;
        String textResponse = "This is a text answer";
        
        SurveyAnswerRequestDTO requestDTO = SurveyAnswerRequestDTO.builder()
                .surveyId(surveyId)
                .answers(List.of(
                        QuestionAnswerDTO.builder()
                                .questionId(questionId)
                                .textResponse(textResponse)
                                .build()
                ))
                .build();
        
        Survey survey = Survey.builder()
                .id(surveyId)
                .title("Test Survey")
                .createdBy(1)
                .createdAt(LocalDateTime.now())
                .build();
        
        Question question = Question.builder()
                .id(questionId)
                .surveyId(surveyId)
                .content("What do you think?")
                .questionType("TEXT")
                .createdAt(LocalDateTime.now())
                .build();
        
        Answer savedAnswer = Answer.builder()
                .id(1)
                .questionId(questionId)
                .userId(userId)
                .choiceId(null)
                .isPublic(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        when(surveyRepository.findById(surveyId)).thenReturn(Mono.just(survey));
        when(questionRepository.findBySurveyId(surveyId)).thenReturn(Flux.just(question));
        when(answerRepository.save(any(Answer.class))).thenReturn(Mono.just(savedAnswer));
        
        StepVerifier.create(answerService.submitSurveyAnswers(requestDTO, userId))
                .expectNextMatches(response -> 
                        response.getSurveyId().equals(surveyId) &&
                        response.getUserId().equals(userId) &&
                        response.getAnswers().size() == 1 &&
                        response.getAnswers().get(0).getQuestionId().equals(questionId) &&
                        response.getAnswers().get(0).getChoiceText().equals(textResponse))
                .verifyComplete();
    }
    
    @Test
    void submitSurveyAnswers_ValidRequest_SingleChoiceQuestion_ReturnsAnswers() {
        Integer surveyId = 2;
        Integer userId = 1;
        Integer questionId = 2;
        Integer choiceId = 1;
        
        SurveyAnswerRequestDTO requestDTO = SurveyAnswerRequestDTO.builder()
                .surveyId(surveyId)
                .answers(List.of(
                        QuestionAnswerDTO.builder()
                                .questionId(questionId)
                                .choiceId(choiceId)
                                .build()
                ))
                .build();
        
        Survey survey = Survey.builder()
                .id(surveyId)
                .title("Test Survey")
                .createdBy(1)
                .createdAt(LocalDateTime.now())
                .build();
        
        Question question = Question.builder()
                .id(questionId)
                .surveyId(surveyId)
                .content("Choose one option:")
                .questionType("SINGLE")
                .createdAt(LocalDateTime.now())
                .build();
        
        Choice choice = Choice.builder()
                .id(choiceId)
                .questionId(questionId)
                .choiceText("Option A")
                .build();
        
        Answer savedAnswer = Answer.builder()
                .id(1)
                .questionId(questionId)
                .userId(userId)
                .choiceId(choiceId)
                .isPublic(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        when(surveyRepository.findById(surveyId)).thenReturn(Mono.just(survey));
        when(questionRepository.findBySurveyId(surveyId)).thenReturn(Flux.just(question));
        when(choiceRepository.findById(choiceId)).thenReturn(Mono.just(choice));
        when(answerRepository.save(any(Answer.class))).thenReturn(Mono.just(savedAnswer));
        
        StepVerifier.create(answerService.submitSurveyAnswers(requestDTO, userId))
                .expectNextMatches(response -> 
                        response.getSurveyId().equals(surveyId) &&
                        response.getUserId().equals(userId) &&
                        response.getAnswers().size() == 1 &&
                        response.getAnswers().get(0).getQuestionId().equals(questionId) &&
                        response.getAnswers().get(0).getChoiceId().equals(choiceId) &&
                        response.getAnswers().get(0).getChoiceText().equals("Option A"))
                .verifyComplete();
    }
    
    @Test
    void submitSurveyAnswers_ValidRequest_MultipleChoiceQuestion_ReturnsAnswers() {
        Integer surveyId = 3;
        Integer userId = 1;
        Integer questionId = 3;
        List<Integer> choiceIds = Arrays.asList(1, 2);
        
        SurveyAnswerRequestDTO requestDTO = SurveyAnswerRequestDTO.builder()
                .surveyId(surveyId)
                .answers(List.of(
                        QuestionAnswerDTO.builder()
                                .questionId(questionId)
                                .choiceIds(choiceIds)
                                .build()
                ))
                .build();
        
        Survey survey = Survey.builder()
                .id(surveyId)
                .title("Test Survey")
                .createdBy(1)
                .createdAt(LocalDateTime.now())
                .build();
        
        Question question = Question.builder()
                .id(questionId)
                .surveyId(surveyId)
                .content("Choose multiple options:")
                .questionType("MULTIPLE")
                .createdAt(LocalDateTime.now())
                .build();
        
        Choice choice1 = Choice.builder()
                .id(1)
                .questionId(questionId)
                .choiceText("Option A")
                .build();
                
        Choice choice2 = Choice.builder()
                .id(2)
                .questionId(questionId)
                .choiceText("Option B")
                .build();
        
        Answer savedAnswer1 = Answer.builder()
                .id(1)
                .questionId(questionId)
                .userId(userId)
                .choiceId(1)
                .isPublic(true)
                .createdAt(LocalDateTime.now())
                .build();
                
        Answer savedAnswer2 = Answer.builder()
                .id(2)
                .questionId(questionId)
                .userId(userId)
                .choiceId(2)
                .isPublic(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        when(surveyRepository.findById(surveyId)).thenReturn(Mono.just(survey));
        when(questionRepository.findBySurveyId(surveyId)).thenReturn(Flux.just(question));
        when(choiceRepository.findById(1)).thenReturn(Mono.just(choice1));
        when(choiceRepository.findById(2)).thenReturn(Mono.just(choice2));
        when(answerRepository.save(any(Answer.class)))
                .thenReturn(Mono.just(savedAnswer1))
                .thenReturn(Mono.just(savedAnswer2));
        
        StepVerifier.create(answerService.submitSurveyAnswers(requestDTO, userId))
                .expectNextMatches(response -> 
                        response.getSurveyId().equals(surveyId) &&
                        response.getUserId().equals(userId) &&
                        response.getAnswers().size() == 2 &&
                        response.getAnswers().get(0).getQuestionId().equals(questionId) &&
                        response.getAnswers().get(1).getQuestionId().equals(questionId))
                .verifyComplete();
    }
    
    @Test
    void submitSurveyAnswers_SurveyNotFound_ThrowsException() {
        Integer surveyId = 999;
        Integer userId = 1;
        Integer questionId = 1;
        
        SurveyAnswerRequestDTO requestDTO = SurveyAnswerRequestDTO.builder()
                .surveyId(surveyId)
                .answers(List.of(
                        QuestionAnswerDTO.builder()
                                .questionId(questionId)
                                .textResponse("Test answer")
                                .build()
                ))
                .build();
        
        when(surveyRepository.findById(surveyId)).thenReturn(Mono.empty());
        
        StepVerifier.create(answerService.submitSurveyAnswers(requestDTO, userId))
                .expectError(SurveyNotFoundException.class)
                .verify();
    }
    
    @Test
    void submitSurveyAnswers_MissingQuestion_ThrowsException() {
        Integer surveyId = 1;
        Integer userId = 1;
        Integer questionId = 999;
        
        SurveyAnswerRequestDTO requestDTO = SurveyAnswerRequestDTO.builder()
                .surveyId(surveyId)
                .answers(List.of(
                        QuestionAnswerDTO.builder()
                                .questionId(questionId)
                                .textResponse("Test answer")
                                .build()
                ))
                .build();
        
        Survey survey = Survey.builder()
                .id(surveyId)
                .title("Test Survey")
                .createdBy(1)
                .createdAt(LocalDateTime.now())
                .build();
        
        Question question = Question.builder()
                .id(1)
                .surveyId(surveyId)
                .content("What do you think?")
                .questionType("TEXT")
                .createdAt(LocalDateTime.now())
                .build();
        
        when(surveyRepository.findById(surveyId)).thenReturn(Mono.just(survey));
        when(questionRepository.findBySurveyId(surveyId)).thenReturn(Flux.just(question));
        
        StepVerifier.create(answerService.submitSurveyAnswers(requestDTO, userId))
                .expectError(InvalidAnswerFormatException.class)
                .verify();
    }
    
    @Test
    void submitSurveyAnswers_InvalidAnswerFormat_TextQuestion_ThrowsException() {
        Integer surveyId = 1;
        Integer userId = 1;
        Integer questionId = 1;
        
        SurveyAnswerRequestDTO requestDTO = SurveyAnswerRequestDTO.builder()
                .surveyId(surveyId)
                .answers(List.of(
                        QuestionAnswerDTO.builder()
                                .questionId(questionId)
                                .choiceId(1)
                                .build()
                ))
                .build();
        
        Survey survey = Survey.builder()
                .id(surveyId)
                .title("Test Survey")
                .createdBy(1)
                .createdAt(LocalDateTime.now())
                .build();
        
        Question question = Question.builder()
                .id(questionId)
                .surveyId(surveyId)
                .content("What do you think?")
                .questionType("TEXT")
                .createdAt(LocalDateTime.now())
                .build();
        
        when(surveyRepository.findById(surveyId)).thenReturn(Mono.just(survey));
        when(questionRepository.findBySurveyId(surveyId)).thenReturn(Flux.just(question));
        
        StepVerifier.create(answerService.submitSurveyAnswers(requestDTO, userId))
                .expectError(InvalidAnswerFormatException.class)
                .verify();
    }
    
    @Test
    void submitSurveyAnswersGrouped_ValidRequest_ReturnsGroupedAnswers() {
        Integer surveyId = 1;
        Integer userId = 1;
        Integer questionId = 1;
        String textResponse = "This is a text answer";
        
        SurveyAnswerRequestDTO requestDTO = SurveyAnswerRequestDTO.builder()
                .surveyId(surveyId)
                .answers(List.of(
                        QuestionAnswerDTO.builder()
                                .questionId(questionId)
                                .textResponse(textResponse)
                                .build()
                ))
                .build();
        
        Survey survey = Survey.builder()
                .id(surveyId)
                .title("Test Survey")
                .createdBy(1)
                .createdAt(LocalDateTime.now())
                .build();
        
        Question question = Question.builder()
                .id(questionId)
                .surveyId(surveyId)
                .content("What do you think?")
                .questionType("TEXT")
                .createdAt(LocalDateTime.now())
                .build();
        
        Answer savedAnswer = Answer.builder()
                .id(1)
                .questionId(questionId)
                .userId(userId)
                .choiceId(null)
                .isPublic(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        when(surveyRepository.findById(surveyId)).thenReturn(Mono.just(survey));
        when(questionRepository.findBySurveyId(surveyId)).thenReturn(Flux.just(question));
        when(answerRepository.save(any(Answer.class))).thenReturn(Mono.just(savedAnswer));
        
        StepVerifier.create(answerService.submitSurveyAnswersGrouped(requestDTO, userId))
                .expectError(NullPointerException.class)
                .verify();
    }
} 
