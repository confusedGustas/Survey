package org.site.survey.service;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.site.survey.mapper.ElasticsearchMapper;
import org.site.survey.repository.AnswerRepository;
import org.site.survey.repository.ChoiceRepository;
import org.site.survey.repository.QuestionRepository;
import org.site.survey.repository.SurveyRepository;
import org.site.survey.repository.elasticsearch.AnswerElasticsearchRepository;
import org.site.survey.repository.elasticsearch.ChoiceElasticsearchRepository;
import org.site.survey.repository.elasticsearch.QuestionElasticsearchRepository;
import org.site.survey.repository.elasticsearch.SurveyElasticsearchRepository;
import org.site.survey.util.LoggerUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true")
@ConditionalOnBean({SurveyElasticsearchRepository.class, QuestionElasticsearchRepository.class, 
                    ChoiceElasticsearchRepository.class, AnswerElasticsearchRepository.class})
public class ElasticsearchSyncService {

    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final ChoiceRepository choiceRepository;
    private final AnswerRepository answerRepository;
    
    private final SurveyElasticsearchRepository surveyElasticsearchRepository;
    private final QuestionElasticsearchRepository questionElasticsearchRepository;
    private final ChoiceElasticsearchRepository choiceElasticsearchRepository;
    private final AnswerElasticsearchRepository answerElasticsearchRepository;
    private final ElasticsearchMapper elasticsearchMapper;
    
    private static final Logger logger = LoggerUtil.getLogger(ElasticsearchSyncService.class);
    private static final Logger errorLogger = LoggerUtil.getErrorLogger(ElasticsearchSyncService.class);
    
    public Mono<Void> syncAllData() {
        logger.info("Starting data synchronization with Elasticsearch");
        return syncSurveys()
                .then(syncQuestions())
                .then(syncChoices())
                .then(syncAnswers())
                .doOnSuccess(v -> logger.info("All data synchronized with Elasticsearch"))
                .doOnError(e -> errorLogger.error("Error synchronizing data with Elasticsearch: {}", e.getMessage(), e));
    }
    
    public Mono<Void> syncSurveys() {
        logger.debug("Syncing surveys to Elasticsearch");
        return surveyRepository.findAll()
                .onErrorResume(e -> {
                    errorLogger.error("Error fetching surveys from database: {}", e.getMessage(), e);
                    return Flux.empty();
                })
                .map(survey -> elasticsearchMapper.mapToSurveyDocument(survey, 0))
                .flatMap(doc -> surveyElasticsearchRepository.save(doc)
                        .onErrorResume(e -> {
                            errorLogger.error("Error saving survey to Elasticsearch: {}", doc.getId(), e);
                            return Mono.empty();
                        }))
                .then()
                .doOnSuccess(v -> logger.info("Successfully synced surveys to Elasticsearch"));
    }
    
    public Mono<Void> syncQuestions() {
        logger.debug("Syncing questions to Elasticsearch");
        return questionRepository.findAll()
                .onErrorResume(e -> {
                    errorLogger.error("Error fetching questions from database: {}", e.getMessage(), e);
                    return Flux.empty();
                })
                .map(elasticsearchMapper::mapToQuestionDocument)
                .flatMap(doc -> questionElasticsearchRepository.save(doc)
                        .onErrorResume(e -> {
                            errorLogger.error("Error saving question to Elasticsearch: {}", doc.getId(), e);
                            return Mono.empty();
                        }))
                .then()
                .doOnSuccess(v -> logger.info("Successfully synced questions to Elasticsearch"));
    }
    
    public Mono<Void> syncChoices() {
        logger.debug("Syncing choices to Elasticsearch");
        return choiceRepository.findAll()
                .onErrorResume(e -> {
                    errorLogger.error("Error fetching choices from database: {}", e.getMessage(), e);
                    return Flux.empty();
                })
                .map(elasticsearchMapper::mapToChoiceDocument)
                .flatMap(doc -> choiceElasticsearchRepository.save(doc)
                        .onErrorResume(e -> {
                            errorLogger.error("Error saving choice to Elasticsearch: {}", doc.getId(), e);
                            return Mono.empty();
                        }))
                .then()
                .doOnSuccess(v -> logger.info("Successfully synced choices to Elasticsearch"));
    }
    
    public Mono<Void> syncAnswers() {
        logger.debug("Syncing answers to Elasticsearch");
        return answerRepository.findAll()
                .onErrorResume(e -> {
                    errorLogger.error("Error fetching answers from database: {}", e.getMessage(), e);
                    return Flux.empty();
                })
                .map(elasticsearchMapper::mapToAnswerDocument)
                .flatMap(doc -> answerElasticsearchRepository.save(doc)
                        .onErrorResume(e -> {
                            errorLogger.error("Error saving answer to Elasticsearch: {}", doc.getId(), e);
                            return Mono.empty();
                        }))
                .then()
                .doOnSuccess(v -> logger.info("Successfully synced answers to Elasticsearch"));
    }
} 