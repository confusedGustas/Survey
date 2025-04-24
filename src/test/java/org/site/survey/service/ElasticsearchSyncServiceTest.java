package org.site.survey.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
import org.site.survey.repository.elasticsearch.AnswerElasticsearchRepository;
import org.site.survey.repository.elasticsearch.ChoiceElasticsearchRepository;
import org.site.survey.repository.elasticsearch.QuestionElasticsearchRepository;
import org.site.survey.repository.elasticsearch.SurveyElasticsearchRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ElasticsearchSyncServiceTest {

    @Mock
    private SurveyRepository surveyRepository;
    
    @Mock
    private QuestionRepository questionRepository;
    
    @Mock
    private ChoiceRepository choiceRepository;
    
    @Mock
    private AnswerRepository answerRepository;
    
    @Mock
    private SurveyElasticsearchRepository surveyElasticsearchRepository;
    
    @Mock
    private QuestionElasticsearchRepository questionElasticsearchRepository;
    
    @Mock
    private ChoiceElasticsearchRepository choiceElasticsearchRepository;
    
    @Mock
    private AnswerElasticsearchRepository answerElasticsearchRepository;
    
    @Mock
    private ElasticsearchMapper elasticsearchMapper;
    
    private ElasticsearchSyncService elasticsearchSyncService;

    @BeforeEach
    void setUp() {
        try {
            MockitoAnnotations.openMocks(this);
            
            elasticsearchSyncService = new ElasticsearchSyncService(
                    surveyRepository,
                    questionRepository,
                    choiceRepository,
                    answerRepository,
                    surveyElasticsearchRepository,
                    questionElasticsearchRepository,
                    choiceElasticsearchRepository,
                    answerElasticsearchRepository,
                    elasticsearchMapper
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize mocks", e);
        }
    }

    @Test
    void syncSurveys_WithData_SynchronizesSuccessfully() {
        Survey survey = new Survey();
        survey.setId(1);
        survey.setTitle("Test Survey");
        survey.setDescription("Test Description");
        survey.setCreatedAt(LocalDateTime.now());
        
        SurveyDocument surveyDocument = new SurveyDocument();
        surveyDocument.setId(1);
        surveyDocument.setTitle("Test Survey");
        surveyDocument.setDescription("Test Description");
        
        when(surveyRepository.findAll()).thenReturn(Flux.just(survey));
        when(elasticsearchMapper.mapToSurveyDocument(any(Survey.class), any(Integer.class)))
                .thenReturn(surveyDocument);
        when(surveyElasticsearchRepository.save(any(SurveyDocument.class)))
                .thenReturn(Mono.just(surveyDocument));
        
        StepVerifier.create(elasticsearchSyncService.syncSurveys())
                .verifyComplete();
        
        verify(surveyRepository).findAll();
        verify(elasticsearchMapper).mapToSurveyDocument(any(Survey.class), any(Integer.class));
        verify(surveyElasticsearchRepository).save(any(SurveyDocument.class));
    }

    @Test
    void syncQuestions_WithData_SynchronizesSuccessfully() {
        Question question = new Question();
        question.setId(1);
        question.setContent("Test Question");
        question.setSurveyId(1);
        
        QuestionDocument questionDocument = new QuestionDocument();
        questionDocument.setId(1);
        questionDocument.setContent("Test Question");
        questionDocument.setSurveyId(1);
        
        when(questionRepository.findAll()).thenReturn(Flux.just(question));
        when(elasticsearchMapper.mapToQuestionDocument(any(Question.class)))
                .thenReturn(questionDocument);
        when(questionElasticsearchRepository.save(any(QuestionDocument.class)))
                .thenReturn(Mono.just(questionDocument));
        
        StepVerifier.create(elasticsearchSyncService.syncQuestions())
                .verifyComplete();
        
        verify(questionRepository).findAll();
        verify(elasticsearchMapper).mapToQuestionDocument(any(Question.class));
        verify(questionElasticsearchRepository).save(any(QuestionDocument.class));
    }

    @Test
    void syncChoices_WithData_SynchronizesSuccessfully() {
        Choice choice = new Choice();
        choice.setId(1);
        choice.setQuestionId(1);
        
        ChoiceDocument choiceDocument = new ChoiceDocument();
        choiceDocument.setId(1);
        choiceDocument.setQuestionId(1);
        
        when(choiceRepository.findAll()).thenReturn(Flux.just(choice));
        when(elasticsearchMapper.mapToChoiceDocument(any(Choice.class)))
                .thenReturn(choiceDocument);
        when(choiceElasticsearchRepository.save(any(ChoiceDocument.class)))
                .thenReturn(Mono.just(choiceDocument));
        
        StepVerifier.create(elasticsearchSyncService.syncChoices())
                .verifyComplete();
        
        verify(choiceRepository).findAll();
        verify(elasticsearchMapper).mapToChoiceDocument(any(Choice.class));
        verify(choiceElasticsearchRepository).save(any(ChoiceDocument.class));
    }

    @Test
    void syncAnswers_WithData_SynchronizesSuccessfully() {
        Answer answer = new Answer();
        answer.setId(1);
        answer.setQuestionId(1);
        answer.setUserId(1);
        
        AnswerDocument answerDocument = new AnswerDocument();
        answerDocument.setId(1);
        answerDocument.setQuestionId(1);
        answerDocument.setUserId(1);
        
        when(answerRepository.findAll()).thenReturn(Flux.just(answer));
        when(elasticsearchMapper.mapToAnswerDocument(any(Answer.class)))
                .thenReturn(answerDocument);
        when(answerElasticsearchRepository.save(any(AnswerDocument.class)))
                .thenReturn(Mono.just(answerDocument));
        
        StepVerifier.create(elasticsearchSyncService.syncAnswers())
                .verifyComplete();
        
        verify(answerRepository).findAll();
        verify(elasticsearchMapper).mapToAnswerDocument(any(Answer.class));
        verify(answerElasticsearchRepository).save(any(AnswerDocument.class));
    }

    @Test
    void syncAllSurveys_Success() {
        Survey survey = Survey.builder()
                .id(1)
                .title("Test Survey")
                .description("Test Description")
                .build();
        
        SurveyDocument surveyDocument = SurveyDocument.builder()
                .id(1)
                .title("Test Survey")
                .description("Test Description")
                .build();
        
        when(surveyRepository.findAll()).thenReturn(Flux.just(survey));
        when(elasticsearchMapper.mapToSurveyDocument(survey, 0)).thenReturn(surveyDocument);
        when(surveyElasticsearchRepository.save(surveyDocument)).thenReturn(Mono.just(surveyDocument));
        
        Mono<Void> result = elasticsearchSyncService.syncSurveys();
        
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(surveyRepository).findAll();
        verify(elasticsearchMapper).mapToSurveyDocument(survey, 0);
        verify(surveyElasticsearchRepository).save(surveyDocument);
    }
}