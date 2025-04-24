package org.site.survey.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.site.survey.dto.StatisticsDTO;
import org.site.survey.dto.response.SearchResultDTO;
import org.site.survey.exception.handler.GlobalExceptionHandler;
import org.site.survey.model.elasticsearch.AnswerDocument;
import org.site.survey.model.elasticsearch.ChoiceDocument;
import org.site.survey.model.elasticsearch.QuestionDocument;
import org.site.survey.model.elasticsearch.SurveyDocument;
import org.site.survey.service.AdminService;
import org.site.survey.service.ElasticsearchSyncService;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @Mock
    private ElasticsearchSyncService elasticsearchSyncService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        try (AutoCloseable ignored = MockitoAnnotations.openMocks(this)) {
            AdminController adminController = new AdminController(adminService, elasticsearchSyncService);
            GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
            
            webTestClient = WebTestClient
                    .bindToController(adminController)
                    .controllerAdvice(exceptionHandler)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize mocks", e);
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchAll_WithValidQuery_ReturnsResults() {
                String query = "test";
        SearchResultDTO result = SearchResultDTO.builder()
                .index("surveys")
                .id(1)
                .type("survey")
                .content(new HashMap<String, Object>())
                .build();

        when(adminService.searchAll(anyString())).thenReturn(Flux.just(result));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/admin/search")
                        .queryParam("query", query)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data[0].index").isEqualTo("surveys");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchSurveys_WithValidQuery_ReturnsSurveys() {
                String query = "test";
        SurveyDocument survey = new SurveyDocument();
        survey.setId(1);
        survey.setTitle("Test Survey");
        survey.setDescription("Test Description");
        survey.setCreatedAt(LocalDateTime.now());

        when(adminService.searchSurveys(anyString())).thenReturn(Flux.just(survey));


        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/admin/search/surveys")
                        .queryParam("query", query)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data[0].id").isEqualTo(1)
                .jsonPath("$.data[0].title").isEqualTo("Test Survey");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchQuestions_WithValidQuery_ReturnsQuestions() {
                String query = "test";
        QuestionDocument question = new QuestionDocument();
        question.setId(1);
        question.setContent("Test Question");
        question.setSurveyId(1);

        when(adminService.searchQuestions(anyString())).thenReturn(Flux.just(question));


        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/admin/search/questions")
                        .queryParam("query", query)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data[0].id").isEqualTo(1)
                .jsonPath("$.data[0].content").isEqualTo("Test Question");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchQuestionsBySurveyId_WithValidId_ReturnsQuestions() {
                Integer surveyId = 1;
        QuestionDocument question = new QuestionDocument();
        question.setId(1);
        question.setContent("Test Question");
        question.setSurveyId(surveyId);

        when(adminService.searchQuestionsBySurveyId(eq(surveyId))).thenReturn(Flux.just(question));


        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/admin/search/questions/survey")
                        .queryParam("surveyId", surveyId)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data[0].id").isEqualTo(1)
                .jsonPath("$.data[0].surveyId").isEqualTo(surveyId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchQuestionsByType_WithValidType_ReturnsQuestions() {
                String type = "MULTIPLE";
        QuestionDocument question = new QuestionDocument();
        question.setId(1);
        question.setContent("Test Question");
        question.setSurveyId(1);

        when(adminService.searchQuestionsByType(eq(type))).thenReturn(Flux.just(question));


        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/admin/search/questions/type")
                        .queryParam("type", type)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data[0].id").isEqualTo(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchChoices_WithValidQuery_ReturnsChoices() {
                String query = "test";
        ChoiceDocument choice = new ChoiceDocument();
        choice.setId(1);
        choice.setQuestionId(1);

        when(adminService.searchChoices(anyString())).thenReturn(Flux.just(choice));


        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/admin/search/choices")
                        .queryParam("query", query)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data[0].id").isEqualTo(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchChoicesByQuestionId_WithValidId_ReturnsChoices() {
                Integer questionId = 1;
        ChoiceDocument choice = new ChoiceDocument();
        choice.setId(1);
        choice.setQuestionId(questionId);

        when(adminService.searchChoicesByQuestionId(eq(questionId))).thenReturn(Flux.just(choice));


        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/admin/search/choices/question")
                        .queryParam("questionId", questionId)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data[0].id").isEqualTo(1)
                .jsonPath("$.data[0].questionId").isEqualTo(questionId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchAnswersByQuestionId_WithValidId_ReturnsAnswers() {
                Integer questionId = 1;
        AnswerDocument answer = new AnswerDocument();
        answer.setId(1);
        answer.setQuestionId(questionId);
        answer.setUserId(1);

        when(adminService.searchAnswersByQuestionId(eq(questionId))).thenReturn(Flux.just(answer));


        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/admin/search/answers/question")
                        .queryParam("questionId", questionId)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data[0].id").isEqualTo(1)
                .jsonPath("$.data[0].questionId").isEqualTo(questionId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchAnswersByUserId_WithValidId_ReturnsAnswers() {
                Integer userId = 1;
        AnswerDocument answer = new AnswerDocument();
        answer.setId(1);
        answer.setQuestionId(1);
        answer.setUserId(userId);

        when(adminService.searchAnswersByUserId(eq(userId))).thenReturn(Flux.just(answer));


        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/admin/search/answers/user")
                        .queryParam("userId", userId)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data[0].id").isEqualTo(1)
                .jsonPath("$.data[0].userId").isEqualTo(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchPublicAnswers_ReturnsPublicAnswers() {
                AnswerDocument answer = new AnswerDocument();
        answer.setId(1);
        answer.setQuestionId(1);
        answer.setUserId(1);

        when(adminService.searchPublicAnswers()).thenReturn(Flux.just(answer));


        webTestClient.get()
                .uri("/api/admin/search/answers/public")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data[0].id").isEqualTo(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchAnswersByQuestionIdAndUserId_WithValidIds_ReturnsAnswers() {
                Integer questionId = 1;
        Integer userId = 1;
        AnswerDocument answer = new AnswerDocument();
        answer.setId(1);
        answer.setQuestionId(questionId);
        answer.setUserId(userId);

        when(adminService.searchAnswersByQuestionIdAndUserId(eq(questionId), eq(userId)))
                .thenReturn(Flux.just(answer));


        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/admin/search/answers/question-user")
                        .queryParam("questionId", questionId)
                        .queryParam("userId", userId)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data[0].id").isEqualTo(1)
                .jsonPath("$.data[0].questionId").isEqualTo(questionId)
                .jsonPath("$.data[0].userId").isEqualTo(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void syncElasticsearch_WhenElasticsearchEnabled_CompletesSuccessfully() {
                when(elasticsearchSyncService.syncAllData()).thenReturn(Mono.empty());


        webTestClient.post()
                .uri("/api/admin/elasticsearch/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").isEqualTo("Elasticsearch synchronization completed successfully");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getStatistics_ReturnsStatistics() {
        when(adminService.getStatistics()).thenReturn(Mono.just(new StatisticsDTO()));


        webTestClient.get()
                .uri("/api/admin/statistics")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getQuestionTypeStatistics_ReturnsStatistics() {
                Map<String, Long> statistics = Map.of(
                "MULTIPLE", 10L,
                "SINGLE", 20L,
                "TEXT", 30L
        );

        when(adminService.getQuestionTypeStatistics()).thenReturn(Mono.just(statistics));


        webTestClient.get()
                .uri("/api/admin/statistics/question-types")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data.MULTIPLE").isEqualTo(10)
                .jsonPath("$.data.SINGLE").isEqualTo(20)
                .jsonPath("$.data.TEXT").isEqualTo(30);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserParticipationStatistics_ReturnsStatistics() {
                Map<String, Object> userStat = new HashMap<>();
        userStat.put("userId", 1);
        userStat.put("username", "user1");
        userStat.put("answerCount", 10L);

        when(adminService.getUserParticipationStatistics()).thenReturn(Flux.just(userStat));


        webTestClient.get()
                .uri("/api/admin/statistics/user-participation")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data[0].userId").isEqualTo(1)
                .jsonPath("$.data[0].username").isEqualTo("user1")
                .jsonPath("$.data[0].answerCount").isEqualTo(10);
    }
} 