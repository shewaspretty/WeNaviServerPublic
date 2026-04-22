package com.hrlee.transnaviserver.springboot.config;

import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Deprecated
//@Configuration
//@EnableAsync
//@NoArgsConstructor
public class AsyncConfig {

    @Bean(name = "osmDataThreadPoolTask")
    public Executor getOsmDataThreadPoolExecutor() {
        ThreadPoolTaskExecutor returnAble = new ThreadPoolTaskExecutor();
        returnAble.setCorePoolSize(16);
        returnAble.setMaxPoolSize(16);
        returnAble.setQueueCapacity(0);
        returnAble.setThreadNamePrefix("osm-");

        return returnAble;
    }
}
