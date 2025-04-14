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
import org.site.survey.dto.request.SurveyRequestDTO;
import org.site.survey.dto.response.SurveyResponseDTO;
import org.site.survey.model.User;
import org.site.survey.service.SurveyService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
@Tag(name = "Survey Management", description = "APIs for managing surveys")
public class SurveyController {
    
    private final SurveyService surveyService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create new survey",
        description = "Creates a new survey with the provided details"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Survey created successfully",
            content = @Content(schema = @Schema(implementation = SurveyResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<SurveyResponseDTO> createSurvey(
            @Parameter(description = "Survey details", required = true)
            @Valid @RequestBody SurveyRequestDTO request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .cast(User.class)
                .flatMap(user -> surveyService.createSurvey(request, user.getId()));
    }

    @GetMapping("/user")
    @Operation(
        summary = "Get all user surveys",
        description = "Retrieves all surveys created by the current authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Surveys retrieved successfully",
            content = @Content(schema = @Schema(implementation = SurveyResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Flux<SurveyResponseDTO> getAllUserSurveys() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .cast(User.class)
                .flatMapMany(user -> surveyService.getAllSurveysByUser(user.getId()));
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Delete survey",
        description = "Deletes a survey by its ID if the current user is the creator"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Survey deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid survey ID"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - user is not the survey creator"),
        @ApiResponse(responseCode = "404", description = "Survey not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<Void> deleteSurvey(
            @Parameter(description = "ID of the survey to delete", required = true)
            @PathVariable Integer id) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .cast(User.class)
                .flatMap(user -> surveyService.deleteSurvey(id, user.getId()));
    }
} 