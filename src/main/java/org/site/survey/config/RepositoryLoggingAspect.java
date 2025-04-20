package org.site.survey.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Arrays;

@Aspect
@Component
@Order(3) // A higher value than LoggingAspect to ensure proper order
public class RepositoryLoggingAspect {

    @Pointcut("within(org.site.survey.repository..*)")
    public void repositoryPointcut() {
    }

    @Around("repositoryPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger logger = LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());

        String methodName = joinPoint.getSignature().getName();
        String args = Arrays.toString(joinPoint.getArgs());

        if (logger.isDebugEnabled()) {
            logger.debug("Repository Call: {}() with argument[s] = {}", methodName, args);
        }

        Object result = joinPoint.proceed();

        if (result instanceof Mono) {
            return ((Mono<?>) result).doOnSuccess(value ->
                    logResult(logger, methodName, value))
                .doOnError(error ->
                    logger.error("Error in repository method {}(): {}", methodName, error.getMessage(), error));
        } else if (result instanceof Flux) {
            return ((Flux<?>) result).doOnComplete(() ->
                    logger.debug("Repository Call Completed: {}()", methodName))
                .doOnError(error ->
                    logger.error("Error in repository method {}(): {}", methodName, error.getMessage(), error));
        } else {
            logResult(logger, methodName, result);
            return result;
        }
    }

    private void logResult(Logger logger, String methodName, Object result) {
        if (logger.isDebugEnabled()) {
            String resultStr = result != null ? result.toString() : "null";
            if (resultStr.length() > 100) {
                resultStr = resultStr.substring(0, 100) + "... [truncated]";
            }
            logger.debug("Repository Result: {}() returned: {}", methodName, resultStr);
        }
    }
}