package org.site.survey.controller;

import lombok.RequiredArgsConstructor;
import org.site.survey.model.Answer;
import org.site.survey.model.Question;
import org.site.survey.service.AdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/search/questions")
    public Flux<Question> searchQuestions(@RequestParam String content) {
        return adminService.searchQuestions(content);
    }

    @GetMapping("/search/answers")
    public Flux<Answer> searchAnswers(@RequestParam String content) {
        return adminService.searchAnswers(content);
    }
}