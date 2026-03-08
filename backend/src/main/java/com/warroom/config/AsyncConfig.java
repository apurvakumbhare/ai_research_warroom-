package com.warroom.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Thread management for asynchronous agent processing.
 * Allows the orchestrator to handle multiple agent reasoning chains
 * concurrently.
 */
@Configuration
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Parallel agent reasoning capacity
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);

        // Queue size to prevent overflow during heavy project analysis
        executor.setQueueCapacity(100);

        executor.setThreadNamePrefix("WarRoom-Agent-");
        executor.initialize();
        return executor;
    }
}
