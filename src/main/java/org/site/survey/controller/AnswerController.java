package org.site.survey.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Survey Answers", description = "APIs for managing survey answers")
public class AnswerController {
    
    private final AnswerService answerService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Submit survey answers",
        description = "Submits answers for a survey from the current authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Answers submitted successfully",
            content = @Content(schema = @Schema(implementation = GroupedSurveyAnswerResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Survey or question not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<GroupedSurveyAnswerResponseDTO> submitSurveyAnswers(
            @Parameter(description = "Survey answers", required = true)
            @Valid @RequestBody SurveyAnswerRequestDTO request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .cast(User.class)
                .flatMap(user -> answerService.submitSurveyAnswersGrouped(request, user.getId()));
    }
} 