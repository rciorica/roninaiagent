package com.ronin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core pool size: how many async tasks can run in parallel
        executor.setCorePoolSize(4);

        // Max pool size: upper limit when tasks spike
        executor.setMaxPoolSize(8);

        // Queue capacity: how many tasks can wait before rejection
        executor.setQueueCapacity(100);

        executor.setThreadNamePrefix("ronin-worker-");
        executor.initialize();

        return executor;
    }
}
