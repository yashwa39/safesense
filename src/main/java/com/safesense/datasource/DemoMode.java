package com.safesense.datasource;

public enum DemoMode {
    AUTO,
    NORMAL,
    WARNING,
    CRITICAL;

    public static DemoMode fromParam(String mode) {
        if (mode == null || mode.isBlank()) {
            throw new IllegalArgumentException("Invalid mode");
        }
        try {
            return DemoMode.valueOf(mode.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid mode");
        }
    }
}
