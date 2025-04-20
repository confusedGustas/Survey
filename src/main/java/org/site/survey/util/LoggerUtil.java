package org.site.survey.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggerUtil {

    public static Logger getLogger(Class<?> clazz) {
        return LogManager.getLogger(clazz);
    }

    public static Logger getLogger(String name) {
        return LogManager.getLogger(name);
    }

    public static Logger getErrorLogger(Class<?> clazz) {
        return LogManager.getLogger("org.site.survey.error." + clazz.getSimpleName());
    }
} 
