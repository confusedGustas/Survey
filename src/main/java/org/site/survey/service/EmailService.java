package org.site.survey.service;


import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final ReactiveKafkaProducerTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "emails";

    public Mono<SenderResult<Void>> sendEmail(String emailAddress, String subject, String content) {
        String emailJson = String.format("{\"to\":\"%s\",\"subject\":\"%s\",\"content\":\"%s\"}",
                emailAddress, subject, content);
        return kafkaTemplate.send(TOPIC, emailAddress, emailJson);
    }
}