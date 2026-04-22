package com.hrlee.transnaviserver.springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface LoggAble {

    public default Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass().getName());
    }

    default void logInfo(String content) { getLogger().info(" > " + Thread.currentThread().getName() + " " + content); }
    default void logUsedMemoryInfo() {
        logInfo(( (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) /1024/1024 ) + "MB CURRENTLY USED");
    }
}
