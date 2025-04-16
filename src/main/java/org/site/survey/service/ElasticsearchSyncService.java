package org.site.survey.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.site.survey.model.*;
import org.site.survey.model.elasticsearch.*;
import org.site.survey.repository.*;
import org.site.survey.repository.elasticsearch.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
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
                .map(this::mapToSurveyDocument)
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
                .map(this::mapToQuestionDocument)
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
                .map(this::mapToChoiceDocument)
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
                .map(this::mapToAnswerDocument)
                .flatMap(doc -> answerElasticsearchRepository.save(doc)
                        .onErrorResume(e -> {
                            log.error("Error saving answer to Elasticsearch: {}", doc.getId(), e);
                            return Mono.empty();
                        }))
                .then();
    }
    
    private SurveyDocument mapToSurveyDocument(Survey survey) {
        // Calculate the question count for this survey
        // We'll set it to 0 for now since we need an async operation to count them
        // The AdminService will provide the correct counts when searching
        
        return SurveyDocument.builder()
                .id(survey.getId())
                .title(survey.getTitle())
                .description(survey.getDescription())
                .questionSize(0) // Default to 0 - this will be updated when searching via AdminService
                .createdBy(survey.getCreatedBy())
                .createdAt(survey.getCreatedAt())
                .build();
    }
    
    private QuestionDocument mapToQuestionDocument(Question question) {
        return QuestionDocument.builder()
                .id(question.getId())
                .surveyId(question.getSurveyId())
                .content(question.getContent())
                .questionType(question.getQuestionType())
                .createdAt(question.getCreatedAt())
                .build();
    }
    
    private ChoiceDocument mapToChoiceDocument(Choice choice) {
        return ChoiceDocument.builder()
                .id(choice.getId())
                .questionId(choice.getQuestionId())
                .choiceText(choice.getChoiceText())
                .build();
    }
    
    private AnswerDocument mapToAnswerDocument(Answer answer) {
        return AnswerDocument.builder()
                .id(answer.getId())
                .questionId(answer.getQuestionId())
                .userId(answer.getUserId())
                .choiceId(answer.getChoiceId())
                .isPublic(answer.getIsPublic())
                .createdAt(answer.getCreatedAt())
                .build();
    }
} 