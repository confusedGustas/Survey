package org.site.survey;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ReactiveThreadingVerificationTest {

    @LocalServerPort
    private int port;

    @Test
    public void testThreadUtilizationUnderLoad() throws Exception {
        WebClient client = WebClient.create("http://localhost:" + port);

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        int initialThreadCount = threadMXBean.getThreadCount();
        
        System.out.println("Initial thread count: " + initialThreadCount);

        List<String> initialThreadNames = getThreadNames();

        int concurrentRequests = 100;
        CountDownLatch latch = new CountDownLatch(concurrentRequests);

        Flux.range(1, concurrentRequests)
            .flatMap(i -> client.get()
                    .uri("/auth/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .doFinally(signal -> latch.countDown())
                    .onErrorResume(e -> {
                        e.printStackTrace();
                        return Mono.empty();
                    })
                    .subscribeOn(Schedulers.parallel()))
            .subscribe();

        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "Not all requests completed in time");

        int peakThreadCount = threadMXBean.getPeakThreadCount();
        System.out.println("Peak thread count: " + peakThreadCount);

        int endThreadCount = threadMXBean.getThreadCount();
        System.out.println("Final thread count: " + endThreadCount);

        List<String> endThreadNames = getThreadNames();
        List<String> newThreads = new ArrayList<>(endThreadNames);
        newThreads.removeAll(initialThreadNames);
        
        System.out.println("New threads created: " + newThreads.size());
        newThreads.forEach(thread -> System.out.println(" - " + thread));

        int newThreadsCreated = peakThreadCount - initialThreadCount;
        System.out.println("Total new threads during test: " + newThreadsCreated);

        assertTrue(newThreadsCreated < concurrentRequests, 
                "Created too many threads (" + newThreadsCreated + ") for " + concurrentRequests + " requests");
    }
    
    private List<String> getThreadNames() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds());
        
        return Arrays.stream(threadInfos)
                .filter(Objects::nonNull)
                .map(ThreadInfo::getThreadName)
                .collect(Collectors.toList());
    }
} 