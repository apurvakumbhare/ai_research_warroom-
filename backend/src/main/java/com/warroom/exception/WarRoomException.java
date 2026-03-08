package com.warroom.exception;

import lombok.Getter;

/**
 * Custom business exception for the AI Research War-Room system.
 * Allows passing specific error codes for structured frontend handling.
 */
@Getter
public class WarRoomException extends RuntimeException {

    private final String errorCode;

    public WarRoomException(String message) {
        super(message);
        this.errorCode = "INTERNAL_ERROR";
    }

    public WarRoomException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
