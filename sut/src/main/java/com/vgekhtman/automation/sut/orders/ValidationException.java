package com.vgekhtman.automation.sut.orders;

/** Thrown for invalid order input; mapped to HTTP 400 with a {@code {"error": ...}} body. */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
