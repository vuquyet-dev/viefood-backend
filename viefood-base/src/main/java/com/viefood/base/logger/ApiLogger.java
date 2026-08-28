package com.viefood.base.logger;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ApiLogger {
    public ApiLogger() {
    }

    public static Logger getLogger(Class<?> clazz){
        return LoggerFactory.getLogger("API."+clazz.getName());
    }
}
