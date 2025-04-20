package org.site.survey.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableAspectJAutoProxy
public class LoggingConfig {

    @PostConstruct
    public void init() {
        configureLog4j2();
    }
    
    private void configureLog4j2() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        org.apache.logging.log4j.core.config.Configuration config = context.getConfiguration();
        
        LoggerConfig loggerConfig = config.getLoggerConfig("org.site.survey");
        loggerConfig.setLevel(org.apache.logging.log4j.Level.DEBUG);
        
        context.updateLoggers();
    }
} 
