package org.site.survey.controller;

import org.site.survey.dto.StatisticsDTO;
import org.site.survey.dto.response.SearchResultDTO;
import org.site.survey.model.elasticsearch.AnswerDocument;
import org.site.survey.model.elasticsearch.ChoiceDocument;
import org.site.survey.model.elasticsearch.QuestionDocument;
import org.site.survey.model.elasticsearch.SurveyDocument;
import org.site.survey.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<SearchResultDTO> searchAll(@RequestParam String query) {
        return adminService.searchAll(query);
    }

    @GetMapping("/search/surveys")
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<SurveyDocument> searchSurveys(@RequestParam String query) {
        return adminService.searchSurveys(query);
    }

    @GetMapping("/search/questions")
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<QuestionDocument> searchQuestions(@RequestParam String query) {
        return adminService.searchQuestions(query);
    }

    @GetMapping("/search/choices")
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<ChoiceDocument> searchChoices(@RequestParam String query) {
        return adminService.searchChoices(query);
    }

    @GetMapping("/search/answers")
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<AnswerDocument> searchAnswersByQuestionId(@RequestParam Integer questionId) {
        return adminService.searchAnswersByQuestionId(questionId);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<StatisticsDTO> getStatistics() {
        return adminService.getStatistics();
    }

    @GetMapping("/statistics/question-types")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Map<String, Long>> getQuestionTypeStatistics() {
        return adminService.getQuestionTypeStatistics();
    }

    @GetMapping("/statistics/user-participation")
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<Map<String, Object>> getUserParticipationStatistics() {
        return adminService.getUserParticipationStatistics();
    }
} 