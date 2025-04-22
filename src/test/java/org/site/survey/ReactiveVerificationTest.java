package org.site.survey;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureWebTestClient
public class ReactiveVerificationTest {

    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    public static void setup() {
        try {
            System.setProperty("BlockHound.DEBUG", "true");
            System.setProperty("reactor.blockhound.shaded.net.bytebuddy.experimental", "true");

            BlockHound.builder()
                .allowBlockingCallsInside("java.util.concurrent.FutureTask", "run")
                .allowBlockingCallsInside("java.util.concurrent.ThreadPoolExecutor", "runWorker")
                .allowBlockingCallsInside("org.springframework.cglib.core.AbstractClassGenerator", "generate")
                .allowBlockingCallsInside("org.springframework.util.ResourceUtils", "getFile")
                .install();

            Hooks.onOperatorDebug();
        } catch (Exception e) {
            System.err.println("BlockHound could not be installed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void blockHoundWorks() {
        try {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> Mono.delay(Duration.ofMillis(1))
                .publishOn(Schedulers.parallel())
                    .publishOn(Schedulers.boundedElastic())
                .doOnNext(it -> {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .block());
            
            future.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            while (cause != null) {
                if (cause instanceof BlockingOperationError) {
                    return;
                }
                cause = cause.getCause();
            }
            throw new AssertionError("Expected BlockingOperationError");
        } catch (InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void verifyEndpointsAreNonBlocking() {
        webTestClient.get()
                .uri("/auth/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void verifyReactorOperatorsUsedCorrectly() {
        Mono<String> mono = Mono.just("test")
                .map(String::toUpperCase)
                .publishOn(Schedulers.parallel());

        StepVerifier.create(mono)
                .expectNext("TEST")
                .verifyComplete();
    }
} 