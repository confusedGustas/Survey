package org.site.survey.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.site.survey.dto.request.SurveyAnswerRequestDTO;
import org.site.survey.dto.response.GroupedSurveyAnswerResponseDTO;
import org.site.survey.model.User;
import org.site.survey.service.AnswerService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
public class AnswerController {
    
    private final AnswerService answerService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<GroupedSurveyAnswerResponseDTO> submitSurveyAnswers(@Valid @RequestBody SurveyAnswerRequestDTO request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .cast(User.class)
                .flatMap(user -> answerService.submitSurveyAnswersGrouped(request, user.getId()));
    }
} 