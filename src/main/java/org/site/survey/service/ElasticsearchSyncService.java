package org.site.survey.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.site.survey.mapper.ElasticsearchMapper;
import org.site.survey.repository.AnswerRepository;
import org.site.survey.repository.ChoiceRepository;
import org.site.survey.repository.QuestionRepository;
import org.site.survey.repository.SurveyRepository;
import org.site.survey.repository.elasticsearch.AnswerElasticsearchRepository;
import org.site.survey.repository.elasticsearch.ChoiceElasticsearchRepository;
import org.site.survey.repository.elasticsearch.QuestionElasticsearchRepository;
import org.site.survey.repository.elasticsearch.SurveyElasticsearchRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
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
    
    public Mono<Void> syncAllData() {
        log.info("Starting data synchronization with Elasticsearch");
        return syncSurveys()
                .then(syncQuestions())
                .then(syncChoices())
                .then(syncAnswers())
                .doOnSuccess(v -> log.info("All data synchronized with Elasticsearch"))
                .doOnError(e -> log.error("Error synchronizing data with Elasticsearch", e));
    }
    
    public Mono<Void> syncSurveys() {
        log.debug("Syncing surveys to Elasticsearch");
        return surveyRepository.findAll()
                .onErrorResume(e -> {
                    log.error("Error fetching surveys from database", e);
                    return Flux.empty();
                })
                .map(survey -> elasticsearchMapper.mapToSurveyDocument(survey, 0))
                .flatMap(doc -> surveyElasticsearchRepository.save(doc)
                        .onErrorResume(e -> {
                            log.error("Error saving survey to Elasticsearch: {}", doc.getId(), e);
                            return Mono.empty();
                        }))
                .then();
    }
    
    public Mono<Void> syncQuestions() {
        log.debug("Syncing questions to Elasticsearch");
        return questionRepository.findAll()
                .onErrorResume(e -> {
                    log.error("Error fetching questions from database", e);
                    return Flux.empty();
                })
                .map(elasticsearchMapper::mapToQuestionDocument)
                .flatMap(doc -> questionElasticsearchRepository.save(doc)
                        .onErrorResume(e -> {
                            log.error("Error saving question to Elasticsearch: {}", doc.getId(), e);
                            return Mono.empty();
                        }))
                .then();
    }
    
    public Mono<Void> syncChoices() {
        log.debug("Syncing choices to Elasticsearch");
        return choiceRepository.findAll()
                .onErrorResume(e -> {
                    log.error("Error fetching choices from database", e);
                    return Flux.empty();
                })
                .map(elasticsearchMapper::mapToChoiceDocument)
                .flatMap(doc -> choiceElasticsearchRepository.save(doc)
                        .onErrorResume(e -> {
                            log.error("Error saving choice to Elasticsearch: {}", doc.getId(), e);
                            return Mono.empty();
                        }))
                .then();
    }
    
    public Mono<Void> syncAnswers() {
        log.debug("Syncing answers to Elasticsearch");
        return answerRepository.findAll()
                .onErrorResume(e -> {
                    log.error("Error fetching answers from database", e);
                    return Flux.empty();
                })
                .map(elasticsearchMapper::mapToAnswerDocument)
                .flatMap(doc -> answerElasticsearchRepository.save(doc)
                        .onErrorResume(e -> {
                            log.error("Error saving answer to Elasticsearch: {}", doc.getId(), e);
                            return Mono.empty();
                        }))
                .then();
    }
} 