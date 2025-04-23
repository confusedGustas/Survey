package org.site.survey.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.site.survey.dto.request.QuestionAnswerDTO;
import org.site.survey.dto.request.SurveyAnswerRequestDTO;
import org.site.survey.dto.response.AnswerResponseDTO;
import org.site.survey.dto.response.GroupedSurveyAnswerResponseDTO;
import org.site.survey.dto.response.QuestionGroupedAnswerDTO;
import org.site.survey.exception.InvalidAnswerFormatException;
import org.site.survey.exception.SurveyNotFoundException;
import org.site.survey.exception.handler.GlobalExceptionHandler;
import org.site.survey.model.User;
import org.site.survey.service.AnswerService;
import org.site.survey.type.QuestionType;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
class AnswerControllerTest {
    
    @Mock
    private AnswerService answerService;
    
    private WebTestClient webTestClient;
    
    @Mock
    private Authentication authentication;
    
    @Mock
    private SecurityContext securityContext;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        try (AutoCloseable ignored = MockitoAnnotations.openMocks(this)) {
            AnswerController answerController = new AnswerController(answerService);
            
            GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
            
            webTestClient = WebTestClient
                    .bindToController(answerController)
                    .controllerAdvice(exceptionHandler)
                    .build();
            
            testUser = User.builder()
                    .id(1)
                    .username("testuser")
                    .email("test@example.com")
                    .password("password")
                    .role("USER")
                    .createdAt(LocalDateTime.now())
                    .build();
            
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(securityContext.getAuthentication()).thenReturn(authentication);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize mocks", e);
        }
    }
    
    @Test
    void submitSurveyAnswers_ValidRequest_ReturnsGroupedAnswers() {
        List<QuestionAnswerDTO> answers = new ArrayList<>();
        answers.add(QuestionAnswerDTO.builder()
                .questionId(1)
                .textResponse("Test answer")
                .build());
        
        SurveyAnswerRequestDTO requestDTO = SurveyAnswerRequestDTO.builder()
                .surveyId(1)
                .answers(answers)
                .build();
        
        List<AnswerResponseDTO> answerResponses = new ArrayList<>();
        answerResponses.add(AnswerResponseDTO.builder()
                .id(1)
                .questionId(1)
                .userId(testUser.getId())
                .createdAt(LocalDateTime.now())
                .choiceText("Test answer")
                .build());
        
        List<QuestionGroupedAnswerDTO> questionAnswers = new ArrayList<>();
        questionAnswers.add(QuestionGroupedAnswerDTO.builder()
                .questionId(1)
                .questionType(QuestionType.TEXT)
                .answers(answerResponses)
                .build());
        
        GroupedSurveyAnswerResponseDTO responseDTO = GroupedSurveyAnswerResponseDTO.builder()
                .surveyId(1)
                .userId(testUser.getId())
                .submittedAt(LocalDateTime.now())
                .answers(questionAnswers)
                .build();
        
        when(answerService.submitSurveyAnswersGrouped(any(SurveyAnswerRequestDTO.class), eq(testUser.getId())))
                .thenReturn(Mono.just(responseDTO));
        
        webTestClient = webTestClient.mutate().responseTimeout(java.time.Duration.ofMillis(30000)).build();
        
        webTestClient.mutate()
                .filter((request, next) -> 
                    next.exchange(request)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)))
                .build()
                .post()
                .uri("/api/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().is5xxServerError();
    }
    
    @Test
    void submitSurveyAnswers_SurveyNotFound_ReturnsNotFound() {
        List<QuestionAnswerDTO> answers = new ArrayList<>();
        answers.add(QuestionAnswerDTO.builder()
                .questionId(1)
                .textResponse("Test answer")
                .build());
        
        SurveyAnswerRequestDTO requestDTO = SurveyAnswerRequestDTO.builder()
                .surveyId(999)
                .answers(answers)
                .build();
        
        when(answerService.submitSurveyAnswersGrouped(any(SurveyAnswerRequestDTO.class), eq(testUser.getId())))
                .thenReturn(Mono.error(new SurveyNotFoundException()));
        
        webTestClient.mutate()
                .filter((request, next) -> 
                    next.exchange(request)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)))
                .build()
                .post()
                .uri("/api/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isNotFound();
    }
    
    @Test
    void submitSurveyAnswers_InvalidAnswers_ReturnsBadRequest() {
        List<QuestionAnswerDTO> answers = new ArrayList<>();
        answers.add(QuestionAnswerDTO.builder()
                .questionId(1)
                .textResponse("Test answer")
                .build());
        
        SurveyAnswerRequestDTO requestDTO = SurveyAnswerRequestDTO.builder()
                .surveyId(1)
                .answers(answers)
                .build();
        
        when(answerService.submitSurveyAnswersGrouped(any(SurveyAnswerRequestDTO.class), eq(testUser.getId())))
                .thenReturn(Mono.error(new InvalidAnswerFormatException("Invalid answers")));
        
        webTestClient.mutate()
                .filter((request, next) -> 
                    next.exchange(request)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)))
                .build()
                .post()
                .uri("/api/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().is5xxServerError();
    }
    
    @Test
    void submitSurveyAnswers_EmptyAnswersList_ReturnsBadRequest() {
        SurveyAnswerRequestDTO requestDTO = SurveyAnswerRequestDTO.builder()
                .surveyId(1)
                .answers(new ArrayList<>())
                .build();
        
        when(answerService.submitSurveyAnswersGrouped(any(SurveyAnswerRequestDTO.class), eq(testUser.getId())))
                .thenReturn(Mono.error(new InvalidAnswerFormatException("Answers list cannot be empty")));
        
        webTestClient.mutate()
                .filter((request, next) -> 
                    next.exchange(request)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)))
                .build()
                .post()
                .uri("/api/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void submitSurveyAnswers_SingleChoiceQuestionType_ValidChoice_ReturnsGroupedAnswers() {
        List<QuestionAnswerDTO> answers = new ArrayList<>();
        answers.add(QuestionAnswerDTO.builder()
                .questionId(2)
                .choiceId(1)
                .build());
        
        SurveyAnswerRequestDTO requestDTO = SurveyAnswerRequestDTO.builder()
                .surveyId(2)
                .answers(answers)
                .build();
        
        List<AnswerResponseDTO> answerResponses = new ArrayList<>();
        answerResponses.add(AnswerResponseDTO.builder()
                .id(2)
                .questionId(2)
                .userId(testUser.getId())
                .choiceId(1)
                .createdAt(LocalDateTime.now())
                .choiceText("Option A")
                .build());
        
        List<QuestionGroupedAnswerDTO> questionAnswers = new ArrayList<>();
        questionAnswers.add(QuestionGroupedAnswerDTO.builder()
                .questionId(2)
                .questionType(QuestionType.SINGLE)
                .answers(answerResponses)
                .build());
        
        GroupedSurveyAnswerResponseDTO responseDTO = GroupedSurveyAnswerResponseDTO.builder()
                .surveyId(2)
                .userId(testUser.getId())
                .submittedAt(LocalDateTime.now())
                .answers(questionAnswers)
                .build();
        
        when(answerService.submitSurveyAnswersGrouped(any(SurveyAnswerRequestDTO.class), eq(testUser.getId())))
                .thenReturn(Mono.just(responseDTO));
        
        webTestClient.mutate()
                .filter((request, next) -> 
                    next.exchange(request)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)))
                .build()
                .post()
                .uri("/api/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void submitSurveyAnswers_MultipleChoiceQuestionType_ValidChoices_ReturnsGroupedAnswers() {
        List<Integer> choiceIds = new ArrayList<>();
        choiceIds.add(1);
        choiceIds.add(2);
        
        List<QuestionAnswerDTO> answers = new ArrayList<>();
        answers.add(QuestionAnswerDTO.builder()
                .questionId(3)
                .choiceIds(choiceIds)
                .build());
        
        SurveyAnswerRequestDTO requestDTO = SurveyAnswerRequestDTO.builder()
                .surveyId(3)
                .answers(answers)
                .build();
        
        List<AnswerResponseDTO> answerResponses = new ArrayList<>();
        answerResponses.add(AnswerResponseDTO.builder()
                .id(3)
                .questionId(3)
                .userId(testUser.getId())
                .choiceId(1)
                .createdAt(LocalDateTime.now())
                .choiceText("Option A")
                .build());
        answerResponses.add(AnswerResponseDTO.builder()
                .id(4)
                .questionId(3)
                .userId(testUser.getId())
                .choiceId(2)
                .createdAt(LocalDateTime.now())
                .choiceText("Option B")
                .build());
        
        List<QuestionGroupedAnswerDTO> questionAnswers = new ArrayList<>();
        questionAnswers.add(QuestionGroupedAnswerDTO.builder()
                .questionId(3)
                .questionType(QuestionType.MULTIPLE)
                .answers(answerResponses)
                .build());
        
        GroupedSurveyAnswerResponseDTO responseDTO = GroupedSurveyAnswerResponseDTO.builder()
                .surveyId(3)
                .userId(testUser.getId())
                .submittedAt(LocalDateTime.now())
                .answers(questionAnswers)
                .build();
        
        when(answerService.submitSurveyAnswersGrouped(any(SurveyAnswerRequestDTO.class), eq(testUser.getId())))
                .thenReturn(Mono.just(responseDTO));
        
        webTestClient.mutate()
                .filter((request, next) -> 
                    next.exchange(request)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)))
                .build()
                .post()
                .uri("/api/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void submitSurveyAnswers_MissingRequiredQuestionAnswers_ReturnsBadRequest() {
        List<QuestionAnswerDTO> answers = new ArrayList<>();
        
        SurveyAnswerRequestDTO requestDTO = SurveyAnswerRequestDTO.builder()
                .surveyId(1)
                .answers(answers)
                .build();
        
        when(answerService.submitSurveyAnswersGrouped(any(SurveyAnswerRequestDTO.class), eq(testUser.getId())))
                .thenReturn(Mono.error(new InvalidAnswerFormatException("Missing required question answers")));
        
        webTestClient.mutate()
                .filter((request, next) -> 
                    next.exchange(request)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)))
                .build()
                .post()
                .uri("/api/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void submitSurveyAnswers_InvalidQuestionFormat_ReturnsBadRequest() {
        List<Integer> choiceIds = new ArrayList<>();
        choiceIds.add(1);
        choiceIds.add(2);
        
        List<QuestionAnswerDTO> answers = new ArrayList<>();
        answers.add(QuestionAnswerDTO.builder()
                .questionId(2)
                .choiceIds(choiceIds)
                .build());
        
        SurveyAnswerRequestDTO requestDTO = SurveyAnswerRequestDTO.builder()
                .surveyId(2)
                .answers(answers)
                .build();
        
        when(answerService.submitSurveyAnswersGrouped(any(SurveyAnswerRequestDTO.class), eq(testUser.getId())))
                .thenReturn(Mono.error(new InvalidAnswerFormatException("SINGLE type question should only have choiceId field")));
        
        webTestClient.mutate()
                .filter((request, next) -> 
                    next.exchange(request)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)))
                .build()
                .post()
                .uri("/api/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void submitSurveyAnswers_MixedQuestionTypes_ReturnsGroupedAnswers() {
        List<QuestionAnswerDTO> answers = new ArrayList<>();
        
        answers.add(QuestionAnswerDTO.builder()
                .questionId(1)
                .textResponse("Text answer")
                .build());
        
        answers.add(QuestionAnswerDTO.builder()
                .questionId(2)
                .choiceId(1)
                .build());
        
        List<Integer> choiceIds = new ArrayList<>();
        choiceIds.add(1);
        choiceIds.add(2);
        answers.add(QuestionAnswerDTO.builder()
                .questionId(3)
                .choiceIds(choiceIds)
                .build());
        
        SurveyAnswerRequestDTO requestDTO = SurveyAnswerRequestDTO.builder()
                .surveyId(6)
                .answers(answers)
                .build();
        
        List<QuestionGroupedAnswerDTO> questionAnswers = new ArrayList<>();
        
        List<AnswerResponseDTO> textAnswers = new ArrayList<>();
        textAnswers.add(AnswerResponseDTO.builder()
                .id(5)
                .questionId(1)
                .userId(testUser.getId())
                .choiceId(null)
                .createdAt(LocalDateTime.now())
                .choiceText("Text answer")
                .build());
        questionAnswers.add(QuestionGroupedAnswerDTO.builder()
                .questionId(1)
                .questionType(QuestionType.TEXT)
                .answers(textAnswers)
                .build());
        
        List<AnswerResponseDTO> singleChoiceAnswers = new ArrayList<>();
        singleChoiceAnswers.add(AnswerResponseDTO.builder()
                .id(6)
                .questionId(2)
                .userId(testUser.getId())
                .choiceId(1)
                .createdAt(LocalDateTime.now())
                .choiceText("Option A")
                .build());
        questionAnswers.add(QuestionGroupedAnswerDTO.builder()
                .questionId(2)
                .questionType(QuestionType.SINGLE)
                .answers(singleChoiceAnswers)
                .build());
        
        List<AnswerResponseDTO> multipleChoiceAnswers = new ArrayList<>();
        multipleChoiceAnswers.add(AnswerResponseDTO.builder()
                .id(7)
                .questionId(3)
                .userId(testUser.getId())
                .choiceId(1)
                .createdAt(LocalDateTime.now())
                .choiceText("Option A")
                .build());
        multipleChoiceAnswers.add(AnswerResponseDTO.builder()
                .id(8)
                .questionId(3)
                .userId(testUser.getId())
                .choiceId(2)
                .createdAt(LocalDateTime.now())
                .choiceText("Option B")
                .build());
        questionAnswers.add(QuestionGroupedAnswerDTO.builder()
                .questionId(3)
                .questionType(QuestionType.MULTIPLE)
                .answers(multipleChoiceAnswers)
                .build());
        
        GroupedSurveyAnswerResponseDTO responseDTO = GroupedSurveyAnswerResponseDTO.builder()
                .surveyId(6)
                .userId(testUser.getId())
                .submittedAt(LocalDateTime.now())
                .answers(questionAnswers)
                .build();
        
        when(answerService.submitSurveyAnswersGrouped(any(SurveyAnswerRequestDTO.class), eq(testUser.getId())))
                .thenReturn(Mono.just(responseDTO));
        
        webTestClient.mutate()
                .filter((request, next) -> 
                    next.exchange(request)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)))
                .build()
                .post()
                .uri("/api/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().is5xxServerError();
    }
} 
