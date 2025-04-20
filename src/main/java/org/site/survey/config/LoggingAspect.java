package org.site.survey.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    @Pointcut("within(org.site.survey.controller..*)")
    public void controllerPointcut() {
    }

    @Pointcut("within(org.site.survey.service..*)")
    public void servicePointcut() {
    }

    @AfterThrowing(pointcut = "controllerPointcut() || servicePointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        Logger logger = LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());
        logger.error(
                "Exception in {}() with cause = '{}' and exception = '{}'",
                joinPoint.getSignature().getName(),
                e.getCause() != null ? e.getCause() : "NULL",
                e.getMessage(),
                e
        );
    }

    @Around("controllerPointcut() || servicePointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger logger = LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());
        if (logger.isDebugEnabled()) {
            logger.debug("Enter: {}() with argument[s] = {}", 
                    joinPoint.getSignature().getName(), 
                    Arrays.toString(joinPoint.getArgs()));
        }
        try {
            Object result = joinPoint.proceed();
            if (logger.isDebugEnabled()) {
                logger.debug("Exit: {}() with result = {}", 
                        joinPoint.getSignature().getName(), 
                        result);
            }
            return result;
        } catch (IllegalArgumentException e) {
            logger.error("Illegal argument: {} in {}()", 
                    Arrays.toString(joinPoint.getArgs()), 
                    joinPoint.getSignature().getName());
            throw e;
        }
    }
} 