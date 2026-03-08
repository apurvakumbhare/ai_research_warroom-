package com.warroom.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Centralized exception handler for all REST controllers.
 * Provides structured error responses and ensures internal details are not
 * leaked.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles custom business exceptions.
     */
    @ExceptionHandler(WarRoomException.class)
    public ResponseEntity<ErrorResponse> handleWarRoomException(WarRoomException ex, HttpServletRequest request) {
        log.error("Business Error [{}]: {}", ex.getErrorCode(), ex.getMessage());
        return createResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    /**
     * Handles Jakarta Bean Validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        log.error("Validation Error: {}", errors);
        return createResponse(HttpStatus.BAD_REQUEST, "Validation failed: " + errors, request.getRequestURI());
    }

    /**
     * Handles cases where a requested entity (Project, Session) does not exist or
     * invalid argument.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(IllegalArgumentException ex,
            HttpServletRequest request) {
        log.error("Invalid Argument / Not Found: {}", ex.getMessage());
        return createResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    /**
     * Generic fallback for unhandled runtime exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected System Error", ex);
        return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.",
                request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> createResponse(HttpStatus status, String message, String path) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();
        return new ResponseEntity<>(response, status);
    }

    /**
     * Internal structure for normalized error representation.
     */
    @Data
    @Builder
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
    }
}
