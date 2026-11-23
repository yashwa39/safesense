package com.safesense.model;

import java.time.Instant;
import java.util.UUID;

/**
 * OOP: Encapsulation — one alert created when a sensor status gets worse.
 */
public class Alert {

    private String id;
    private Instant timestamp;
    private String sensorName;
    private double value;
    private String unit;
    private Status status;
    private String message;

    public Alert() {
        // Jackson
    }

    public Alert(String sensorName, double value, String unit, Status status, String message) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        setSensorName(sensorName);
        this.value = value;
        this.unit = unit;
        setStatus(status);
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        if (sensorName == null || sensorName.isBlank()) {
            throw new IllegalArgumentException("Alert sensor name is required");
        }
        this.sensorName = sensorName;
    }

    public double getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (status == null) {
            throw new IllegalArgumentException("Alert status is required");
        }
        this.status = status;
    }

    public String getMessage() {
        return message;
    }
}
