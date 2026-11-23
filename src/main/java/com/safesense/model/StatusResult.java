package com.safesense.model;

/**
 * OOP: Encapsulation — immutable result of {@link Sensor#checkSafetyStatus()}.
 */
public class StatusResult {

    private final Status status;
    private final String message;

    public StatusResult(Status status, String message) {
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
        this.status = status;
        this.message = message == null ? "" : message;
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return status + ": " + message;
    }
}
