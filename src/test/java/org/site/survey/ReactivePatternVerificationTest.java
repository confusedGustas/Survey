package org.site.survey;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ReactivePatternVerificationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void verifyNoBlockingMethodsInServices() {
        String[] serviceNames = applicationContext.getBeanNamesForAnnotation(org.springframework.stereotype.Service.class);
        
        for (String serviceName : serviceNames) {
            Object serviceBean = applicationContext.getBean(serviceName);
            Class<?> serviceClass = serviceBean.getClass();

            List<Method> methods = Arrays.asList(serviceClass.getMethods());

            List<Method> blockingMethods = methods.stream()
                    .filter(method -> {
                        String name = method.getName();
                        Class<?> returnType = method.getReturnType();

                        boolean nonReactive = !reactor.core.publisher.Mono.class.isAssignableFrom(returnType) && 
                                             !reactor.core.publisher.Flux.class.isAssignableFrom(returnType);

                        boolean suspiciousName = name.contains("blocking") || 
                                               name.startsWith("get") || 
                                               name.equals("findAll");
                        
                        return nonReactive && suspiciousName;
                    })
                    .toList();

            if (!blockingMethods.isEmpty()) {
                System.out.println("Potential blocking methods in " + serviceClass.getName() + ":");
                blockingMethods.forEach(method -> 
                    System.out.println(" - " + method.getName() + 
                            " returns " + method.getReturnType().getSimpleName()));
            }
        }
    }
    
    @Test
    public void verifyWebClientIsUsedInsteadOfRestTemplate() {
        String[] restTemplateBeans = applicationContext.getBeanNamesForType(org.springframework.web.client.RestTemplate.class);
        assertEquals(0, restTemplateBeans.length, "RestTemplate beans found. Use WebClient instead");

        String[] webClientBeans = applicationContext.getBeanNamesForType(WebClient.class);
        assertTrue(webClientBeans.length > 0 || 
                  applicationContext.getBeanNamesForType(WebClient.Builder.class).length > 0, 
                  "No WebClient beans found");
    }
    
    @Test
    public void verifyFunctionalRoutes() {
        String[] routerFunctionBeans = applicationContext.getBeanNamesForType(RouterFunction.class);

        if (routerFunctionBeans.length == 0) {
            System.out.println("No functional routes (@RouterFunction) found. " +
                              "This is not an issue, but functional routes are more efficient than @RestController");
        } else {
            System.out.println("Found " + routerFunctionBeans.length + " functional route beans");
        }
    }
} 