package org.site.survey.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.site.survey.dto.StatisticsDTO;
import org.site.survey.dto.response.SearchResultDTO;
import org.site.survey.integrity.ElasticsearchDataIntegrity;
import org.site.survey.mapper.ElasticsearchMapper;
import org.site.survey.model.Answer;
import org.site.survey.model.Choice;
import org.site.survey.model.Question;
import org.site.survey.model.Survey;
import org.site.survey.model.elasticsearch.AnswerDocument;
import org.site.survey.model.elasticsearch.ChoiceDocument;
import org.site.survey.model.elasticsearch.QuestionDocument;
import org.site.survey.model.elasticsearch.SurveyDocument;
import org.site.survey.repository.AnswerRepository;
import org.site.survey.repository.ChoiceRepository;
import org.site.survey.repository.QuestionRepository;
import org.site.survey.repository.SurveyRepository;
import org.site.survey.repository.UserRepository;
import org.site.survey.repository.elasticsearch.AnswerElasticsearchRepository;
import org.site.survey.repository.elasticsearch.ChoiceElasticsearchRepository;
import org.site.survey.repository.elasticsearch.QuestionElasticsearchRepository;
import org.site.survey.repository.elasticsearch.SurveyElasticsearchRepository;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class AdminServiceTest {

    @Mock
    private SurveyRepository surveyRepository;
    
    @Mock
    private QuestionRepository questionRepository;
    
    @Mock
    private ChoiceRepository choiceRepository;
    
    @Mock
    private AnswerRepository answerRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ElasticsearchDataIntegrity elasticsearchDataIntegrity;
    
    @Mock
    private ElasticsearchMapper elasticsearchMapper;
    
    @Mock
    private SurveyElasticsearchRepository surveyElasticsearchRepository;
    
    @Mock
    private QuestionElasticsearchRepository questionElasticsearchRepository;
    
    @Mock
    private ChoiceElasticsearchRepository choiceElasticsearchRepository;
    
    @Mock
    private AnswerElasticsearchRepository answerElasticsearchRepository;
    
    @Mock
    private ReactiveElasticsearchOperations elasticsearchOperations;
    
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        try {
            MockitoAnnotations.openMocks(this);
            
            adminService = new AdminService(
                    surveyRepository,
                    questionRepository,
                    choiceRepository,
                    answerRepository,
                    userRepository,
                    elasticsearchDataIntegrity,
                    elasticsearchMapper
            );
            
            adminService.setElasticsearchDependencies(
                    elasticsearchOperations,
                    surveyElasticsearchRepository,
                    questionElasticsearchRepository,
                    choiceElasticsearchRepository,
                    answerElasticsearchRepository
            );
            
            when(surveyRepository.findAll()).thenReturn(Flux.empty());
            when(questionRepository.findAll()).thenReturn(Flux.empty());
            when(choiceRepository.findAll()).thenReturn(Flux.empty());
            when(answerRepository.findAll()).thenReturn(Flux.empty());
            when(userRepository.findAll()).thenReturn(Flux.empty());
                        
            when(questionRepository.findBySurveyId(anyInt())).thenReturn(Flux.empty());
            when(choiceRepository.findByQuestionId(anyInt())).thenReturn(Flux.empty());
            
            when(answerElasticsearchRepository.findByIsPublic(any())).thenReturn(Flux.empty());
            when(questionElasticsearchRepository.findByQuestionType(anyString())).thenReturn(Flux.empty());
            when(surveyElasticsearchRepository.findByTitleContainingOrDescriptionContaining(anyString(), anyString())).thenReturn(Flux.empty());
            when(questionElasticsearchRepository.findByContentContaining(anyString())).thenReturn(Flux.empty());
            when(choiceElasticsearchRepository.findByChoiceTextContaining(anyString())).thenReturn(Flux.empty());
            when(answerElasticsearchRepository.findByQuestionId(anyInt())).thenReturn(Flux.empty());
            when(answerElasticsearchRepository.findByUserId(anyInt())).thenReturn(Flux.empty());
            when(answerElasticsearchRepository.findByQuestionIdAndUserId(anyInt(), anyInt())).thenReturn(Flux.empty());
            when(questionElasticsearchRepository.findBySurveyId(anyInt())).thenReturn(Flux.empty());
            when(choiceElasticsearchRepository.findByQuestionId(anyInt())).thenReturn(Flux.empty());
            
            SurveyDocument surveyDoc = new SurveyDocument();
            surveyDoc.setId(1);
            surveyDoc.setTitle("Test Survey");
            
            QuestionDocument questionDoc = new QuestionDocument();
            questionDoc.setId(1);
            questionDoc.setContent("Test Question");
            
            ChoiceDocument choiceDoc = new ChoiceDocument();
            choiceDoc.setId(1);
            choiceDoc.setChoiceText("Test Choice");
            
            AnswerDocument answerDoc = new AnswerDocument();
            answerDoc.setId(1);
            
            when(surveyElasticsearchRepository.findByTitleContainingOrDescriptionContaining(anyString(), anyString()))
                    .thenReturn(Flux.just(surveyDoc));
            when(questionElasticsearchRepository.findByContentContaining(anyString()))
                    .thenReturn(Flux.just(questionDoc));
            when(choiceElasticsearchRepository.findByChoiceTextContaining(anyString()))
                    .thenReturn(Flux.just(choiceDoc));
            when(answerElasticsearchRepository.findByQuestionId(anyInt()))
                    .thenReturn(Flux.just(answerDoc));
            
            Survey survey = new Survey();
            survey.setId(1);
            survey.setTitle("Test Survey");
            
            Question question = new Question();
            question.setId(1);
            question.setContent("Test Question");
            
            Choice choice = new Choice();
            choice.setId(1);
            choice.setChoiceText("Test Choice");
            
            Answer answer = new Answer();
            answer.setId(1);
            
            when(elasticsearchMapper.mapToSurveyDocument(any(Survey.class), anyInt()))
                    .thenReturn(surveyDoc);
            when(elasticsearchMapper.mapToQuestionDocument(any(Question.class)))
                    .thenReturn(questionDoc);
            when(elasticsearchMapper.mapToChoiceDocument(any(Choice.class)))
                    .thenReturn(choiceDoc);
            when(elasticsearchMapper.mapToAnswerDocument(any(Answer.class)))
                    .thenReturn(answerDoc);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize mocks", e);
        }
    }

    @Test
    void testSearchAll() {
        Flux<SearchResultDTO> results = adminService.searchAll("test");
        
        StepVerifier.create(results)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void testGetStatistics() {
        when(surveyRepository.count()).thenReturn(Mono.just(10L));
        when(questionRepository.count()).thenReturn(Mono.just(20L));
        when(choiceRepository.count()).thenReturn(Mono.just(30L));
        when(answerRepository.count()).thenReturn(Mono.just(40L));
        when(userRepository.count()).thenReturn(Mono.just(5L));
        
        Question q1 = new Question();
        q1.setId(1);
        q1.setQuestionType("TEXT");
        
        Question q2 = new Question();
        q2.setId(2);
        q2.setQuestionType("SINGLE");
        
        Question q3 = new Question();
        q3.setId(3);
        q3.setQuestionType("MULTIPLE");
        
        when(questionRepository.findAll()).thenReturn(Flux.just(q1, q2, q3));
        
        Mono<StatisticsDTO> result = adminService.getStatistics();
        
        StepVerifier.create(result)
                .expectNextMatches(stats -> 
                    stats.getTotalSurveys() == 10L &&
                    stats.getTotalQuestions() == 20L &&
                    stats.getTotalChoices() == 30L &&
                    stats.getTotalAnswers() == 40L &&
                    stats.getTotalUsers() == 5L &&
                    stats.getQuestionTypeStats() != null
                )
                .verifyComplete();
    }

    @Test
    void testSyncAllToElasticsearch() {
        when(surveyRepository.findAll()).thenReturn(Flux.just(
                Survey.builder().id(1).title("Test Survey").build()));
        when(questionRepository.findAll()).thenReturn(Flux.just(
                Question.builder().id(1).content("Test Question").build()));
        when(choiceRepository.findAll()).thenReturn(Flux.just(
                Choice.builder().id(1).choiceText("Test Choice").build()));
        when(answerRepository.findAll()).thenReturn(Flux.just(
                Answer.builder().id(1).build()));
                
        when(surveyElasticsearchRepository.save(any())).thenReturn(Mono.just(SurveyDocument.builder().build()));
        when(questionElasticsearchRepository.save(any())).thenReturn(Mono.just(QuestionDocument.builder().build()));
        when(choiceElasticsearchRepository.save(any())).thenReturn(Mono.just(ChoiceDocument.builder().build()));
        when(answerElasticsearchRepository.save(any())).thenReturn(Mono.just(AnswerDocument.builder().build()));
        
        Flux<SearchResultDTO> results = adminService.searchAll("test");
        
        StepVerifier.create(results)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void testRecentActivity() {
        String query = "test";
        
        Flux<SurveyDocument> result = adminService.searchSurveys(query);
        
        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void searchAll_WithValidQuery_ReturnsResults() {
        String query = "test";
        
        SurveyDocument surveyDoc = new SurveyDocument();
        surveyDoc.setId(1);
        surveyDoc.setTitle("Test Survey");
        
        QuestionDocument questionDoc = new QuestionDocument();
        questionDoc.setId(2);
        questionDoc.setContent("Test Question");
        
        ChoiceDocument choiceDoc = new ChoiceDocument();
        choiceDoc.setId(3);
        choiceDoc.setChoiceText("Test Choice");
        
        doNothing().when(elasticsearchDataIntegrity).validateSearchQuery(anyString());
        when(surveyElasticsearchRepository.findByTitleContainingOrDescriptionContaining(anyString(), anyString()))
                .thenReturn(Flux.just(surveyDoc));
        when(questionElasticsearchRepository.findByContentContaining(anyString()))
                .thenReturn(Flux.just(questionDoc));
        when(choiceElasticsearchRepository.findByChoiceTextContaining(anyString()))
                .thenReturn(Flux.just(choiceDoc));
        
        Flux<SearchResultDTO> result = adminService.searchAll(query);
        
        StepVerifier.create(result)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void searchSurveys_UsingElasticsearch_ReturnsSurveys() {
        String query = "test";
        SurveyDocument surveyDoc = new SurveyDocument();
        surveyDoc.setId(1);
        surveyDoc.setTitle("Test Survey");
        
        doNothing().when(elasticsearchDataIntegrity).validateSearchQuery(anyString());
        when(surveyElasticsearchRepository.findByTitleContainingOrDescriptionContaining(anyString(), anyString()))
                .thenReturn(Flux.just(surveyDoc));
        
        Flux<SurveyDocument> result = adminService.searchSurveys(query);
        
        StepVerifier.create(result)
                .assertNext(doc -> {
                    assertEquals(1, doc.getId());
                    assertEquals("Test Survey", doc.getTitle());
                })
                .verifyComplete();
    }
    
    @Test
    void searchQuestions_UsingElasticsearch_ReturnsQuestions() {
        String query = "test";
        QuestionDocument questionDoc = new QuestionDocument();
        questionDoc.setId(1);
        questionDoc.setContent("Test Question");
        
        doNothing().when(elasticsearchDataIntegrity).validateSearchQuery(anyString());
        when(questionElasticsearchRepository.findByContentContaining(anyString()))
                .thenReturn(Flux.just(questionDoc));
        
        Flux<QuestionDocument> result = adminService.searchQuestions(query);
        
        StepVerifier.create(result)
                .assertNext(doc -> {
                    assertEquals(1, doc.getId());
                    assertEquals("Test Question", doc.getContent());
                })
                .verifyComplete();
    }
    
    @Test
    void searchChoices_UsingElasticsearch_ReturnsChoices() {
        String query = "test";
        ChoiceDocument choiceDoc = new ChoiceDocument();
        choiceDoc.setId(1);
        choiceDoc.setChoiceText("Test Choice");
        
        doNothing().when(elasticsearchDataIntegrity).validateSearchQuery(anyString());
        when(choiceElasticsearchRepository.findByChoiceTextContaining(anyString()))
                .thenReturn(Flux.just(choiceDoc));
        
        Flux<ChoiceDocument> result = adminService.searchChoices(query);
        
        StepVerifier.create(result)
                .assertNext(doc -> {
                    assertEquals(1, doc.getId());
                    assertEquals("Test Choice", doc.getChoiceText());
                })
                .verifyComplete();
    }
    
    @Test
    void searchQuestionsBySurveyId_UsingElasticsearch_ReturnsQuestions() {
        Integer surveyId = 1;
        QuestionDocument questionDoc = new QuestionDocument();
        questionDoc.setId(1);
        questionDoc.setContent("Test Question");
        questionDoc.setSurveyId(surveyId);
        
        doNothing().when(elasticsearchDataIntegrity).validateSurveyId(anyInt());
        when(questionElasticsearchRepository.findBySurveyId(eq(surveyId)))
                .thenReturn(Flux.just(questionDoc));
        
        Flux<QuestionDocument> result = adminService.searchQuestionsBySurveyId(surveyId);
        
        StepVerifier.create(result)
                .assertNext(doc -> {
                    assertEquals(1, doc.getId());
                    assertEquals("Test Question", doc.getContent());
                    assertEquals(surveyId, doc.getSurveyId());
                })
                .verifyComplete();
    }
    
    @Test
    void searchChoicesByQuestionId_UsingElasticsearch_ReturnsChoices() {
        Integer questionId = 1;
        ChoiceDocument choiceDoc = new ChoiceDocument();
        choiceDoc.setId(1);
        choiceDoc.setQuestionId(questionId);
        
        doNothing().when(elasticsearchDataIntegrity).validateQuestionId(anyInt());
        when(choiceElasticsearchRepository.findByQuestionId(eq(questionId)))
                .thenReturn(Flux.just(choiceDoc));
        
        Flux<ChoiceDocument> result = adminService.searchChoicesByQuestionId(questionId);
        
        StepVerifier.create(result)
                .assertNext(doc -> {
                    assertEquals(1, doc.getId());
                    assertEquals(questionId, doc.getQuestionId());
                })
                .verifyComplete();
    }
    
    @Test
    void searchAnswersByQuestionId_UsingElasticsearch_ReturnsAnswers() {
        Integer questionId = 1;
        AnswerDocument answerDoc = new AnswerDocument();
        answerDoc.setId(1);
        answerDoc.setQuestionId(questionId);
        
        doNothing().when(elasticsearchDataIntegrity).validateQuestionId(anyInt());
        when(answerElasticsearchRepository.findByQuestionId(eq(questionId)))
                .thenReturn(Flux.just(answerDoc));
        
        Flux<AnswerDocument> result = adminService.searchAnswersByQuestionId(questionId);
        
        StepVerifier.create(result)
                .assertNext(doc -> {
                    assertEquals(1, doc.getId());
                    assertEquals(questionId, doc.getQuestionId());
                })
                .verifyComplete();
    }
    
    @Test
    void searchAnswersByUserId_UsingElasticsearch_ReturnsAnswers() {
        Integer userId = 1;
        AnswerDocument answerDoc = new AnswerDocument();
        answerDoc.setId(1);
        answerDoc.setUserId(userId);
        
        doNothing().when(elasticsearchDataIntegrity).validateUserId(anyInt());
        when(answerElasticsearchRepository.findByUserId(eq(userId)))
                .thenReturn(Flux.just(answerDoc));
        
        Flux<AnswerDocument> result = adminService.searchAnswersByUserId(userId);
        
        StepVerifier.create(result)
                .assertNext(doc -> {
                    assertEquals(1, doc.getId());
                    assertEquals(userId, doc.getUserId());
                })
                .verifyComplete();
    }
    
    @Test
    void searchPublicAnswers_UsingElasticsearch_ReturnsPublicAnswers() {
        AnswerDocument answerDoc = new AnswerDocument();
        answerDoc.setId(1);
        answerDoc.setIsPublic(true);
        
        doNothing().when(elasticsearchDataIntegrity).validatePublicFlag(any());
        when(answerElasticsearchRepository.findByIsPublic(eq(true)))
                .thenReturn(Flux.just(answerDoc));
        
        Flux<AnswerDocument> result = adminService.searchPublicAnswers();
        
        StepVerifier.create(result)
                .assertNext(doc -> {
                    assertEquals(1, doc.getId());
                    assertEquals(true, doc.getIsPublic());
                })
                .verifyComplete();
    }
    
    @Test
    void searchAnswersByQuestionIdAndUserId_UsingElasticsearch_ReturnsAnswers() {
        Integer questionId = 1;
        Integer userId = 1;
        AnswerDocument answerDoc = new AnswerDocument();
        answerDoc.setId(1);
        answerDoc.setQuestionId(questionId);
        answerDoc.setUserId(userId);
        
        doNothing().when(elasticsearchDataIntegrity).validateQuestionId(anyInt());
        doNothing().when(elasticsearchDataIntegrity).validateUserId(anyInt());
        when(answerElasticsearchRepository.findByQuestionIdAndUserId(eq(questionId), eq(userId)))
                .thenReturn(Flux.just(answerDoc));
        
        Flux<AnswerDocument> result = adminService.searchAnswersByQuestionIdAndUserId(questionId, userId);
        
        StepVerifier.create(result)
                .assertNext(doc -> {
                    assertEquals(1, doc.getId());
                    assertEquals(questionId, doc.getQuestionId());
                    assertEquals(userId, doc.getUserId());
                })
                .verifyComplete();
    }
    
    @Test
    void getStatistics_ReturnsStatistics() {
        when(surveyRepository.count()).thenReturn(Mono.just(10L));
        when(questionRepository.count()).thenReturn(Mono.just(50L));
        when(choiceRepository.count()).thenReturn(Mono.just(200L));
        when(answerRepository.count()).thenReturn(Mono.just(500L));
        when(userRepository.count()).thenReturn(Mono.just(100L));
        
        Question q1 = new Question();
        q1.setId(1);
        q1.setQuestionType("MULTIPLE");
        
        Question q2 = new Question();
        q2.setId(2);
        q2.setQuestionType("SINGLE");
        
        when(questionRepository.findAll()).thenReturn(Flux.just(q1, q2));
        
        Mono<StatisticsDTO> result = adminService.getStatistics();
        
        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
    }
    
    @Test
    void getQuestionTypeStatistics_ReturnsStatistics() {
        Question q1 = new Question();
        q1.setId(1);
        q1.setQuestionType("MULTIPLE");
        
        Question q2 = new Question();
        q2.setId(2);
        q2.setQuestionType("MULTIPLE");
        
        Question q3 = new Question();
        q3.setId(3);
        q3.setQuestionType("SINGLE");
        
        Question q4 = new Question();
        q4.setId(4);
        q4.setQuestionType("TEXT");
        
        when(questionRepository.findAll()).thenReturn(Flux.just(q1, q2, q3, q4));
        
        Mono<Map<String, Long>> result = adminService.getQuestionTypeStatistics();
        
        StepVerifier.create(result)
                .assertNext(stats -> {
                    assertEquals(2L, stats.get("MULTIPLE"));
                    assertEquals(1L, stats.get("SINGLE"));
                    assertEquals(1L, stats.get("TEXT"));
                })
                .verifyComplete();
    }
}