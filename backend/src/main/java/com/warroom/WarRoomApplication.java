package com.warroom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main entrance for the AI Research War-Room.
 * This class initializes the Spring Boot context and enables asynchronous
 * execution
 * required for parallel multi-agent debate orchestration.
 */
@SpringBootApplication
@EnableAsync
public class WarRoomApplication {

    public static void main(String[] args) {
        SpringApplication.run(WarRoomApplication.class, args);
    }
}
