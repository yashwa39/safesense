package com.safesense.model;

import java.time.Instant;

/**
 * OOP: Encapsulation — one sample from a data source (simulated or hardware).
 */
public class Reading {

    private String sensorName;
    private double value;
    private Instant timestamp;

    public Reading() {
        // Jackson
    }

    public Reading(String sensorName, double value) {
        this(sensorName, value, Instant.now());
    }

    public Reading(String sensorName, double value, Instant timestamp) {
        setSensorName(sensorName);
        this.value = value;
        setTimestamp(timestamp);
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        if (sensorName == null || sensorName.isBlank()) {
            throw new IllegalArgumentException("Reading sensor name is required");
        }
        this.sensorName = sensorName.trim();
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp == null ? Instant.now() : timestamp;
    }
}
