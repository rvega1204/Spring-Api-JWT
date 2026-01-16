package com.rvg.store.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

/**
 * Global exception handler for the application.
 * <p>
 * This class captures exceptions thrown by the controllers and service layer,
 * translating them into appropriate HTTP responses with meaningful error
 * messages.
 * It uses {@link RestControllerAdvice} to apply globally across all
 * controllers.
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles {@link BadCredentialsException} thrown during authentication.
     *
     * @param ex the exception instance containing details about the authentication
     *           failure
     * @return a {@link ResponseEntity} with HTTP 401 (Unauthorized) and an error
     *         message
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentials(BadCredentialsException ex) {
        // Return 401 Unauthorized for invalid credentials
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }

    /**
     * Handles {@link NoSuchElementException} when a requested resource is not
     * found.
     *
     * @param ex the exception instance
     * @return a {@link ResponseEntity} with HTTP 500 (Internal Server Error) and a
     *         "Resource not found" message
     *         (Note: Typically 404 Not Found might be expected, but current
     *         implementation returns 500)
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Resource not found");
    }

    /**
     * Handles generic {@link RuntimeException}s that are not caught by specific
     * handlers.
     *
     * @param ex the runtime exception instance
     * @return a {@link ResponseEntity} with HTTP 500 (Internal Server Error) and
     *         the exception message
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    /**
     * Catch-all handler for any other unhandled {@link Exception}.
     *
     * @param ex the exception instance
     * @return a {@link ResponseEntity} with HTTP 500 (Internal Server Error) and a
     *         generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        // Fallback for unexpected errors
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
    }
}