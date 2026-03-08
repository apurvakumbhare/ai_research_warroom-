package com.warroom.workflow;

/**
 * Enumeration of all possible states in the AI debate lifecycle.
 */
public enum DebateStates {
    CREATED,
    FILES_UPLOADED,
    PROCESSING,
    ANALYSIS_COMPLETE,
    RESULT_GENERATED,
    FAILED
}
