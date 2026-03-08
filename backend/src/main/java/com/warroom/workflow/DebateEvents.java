package com.warroom.workflow;

/**
 * Enumeration of all events that can trigger transitions between debate states.
 */
public enum DebateEvents {
    UPLOAD_FILES,
    START_PROCESSING,
    COMPLETE_ANALYSIS,
    GENERATE_RESULT,
    MARK_FAILED,
    RESET
}
