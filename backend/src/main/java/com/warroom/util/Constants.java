package com.warroom.util;

/**
 * Application-wide constants for the AI Research War-Room system.
 */
public final class Constants {

    private Constants() {
        // Prevent instantiation
    }

    public static final String DEFAULT_PROJECT_STATUS = "CREATED";

    public static final String DEBATE_STATUS_RUNNING = "RUNNING";
    public static final String DEBATE_STATUS_COMPLETED = "COMPLETED";

    public static final String AI_MODEL_NAME = "gpt-4o-mini";

    public static final int MAX_DEBATE_ROUNDS = 3;
}
