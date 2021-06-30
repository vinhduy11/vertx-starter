package vn.com.mservice.factories;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.logging.SLF4JLogDelegateFactory;

import java.io.File;

public class LoggingFactory {
    private static Logger instance = null;
    public LoggingFactory(){
        File logbackFile = new File("etc", "log4j2.xml");
        //System.setProperty("log4j.configuration", logbackFile.getAbsolutePath());
        System.setProperty("java.util.logging.config.file", logbackFile.getAbsolutePath());
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
    }

    public static Logger getInstance(String clazz){
        if (instance == null){
            instance = LoggerFactory.getLogger(clazz);
        }
        return instance;
    }
}
