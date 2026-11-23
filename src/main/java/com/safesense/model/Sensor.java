package com.safesense.model;

/**
 * OOP: Inheritance — parent of GasSensor and SoundSensor.
 * OOP: Encapsulation — name, location and error flag are private.
 */
public abstract class Sensor {

    private String sensorName;
    private String location;
    private boolean sensorError;

    public Sensor(String sensorName, String location) {
        setSensorName(sensorName);
        setLocation(location);
        this.sensorError = false;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        if (sensorName == null || sensorName.isBlank()) {
            throw new IllegalArgumentException("Sensor name is required");
        }
        this.sensorName = sensorName.trim();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Location is required");
        }
        this.location = location.trim();
    }

    public boolean isSensorError() {
        return sensorError;
    }

    protected void setSensorError(boolean sensorError) {
        this.sensorError = sensorError;
    }

    /**
     * OOP: Method overriding — children replace this base check with gas/sound rules.
     */
    public StatusResult checkSafetyStatus() {
        if (sensorError) {
            return new StatusResult(Status.SENSOR_ERROR, "Sensor error");
        }
        return new StatusResult(Status.NORMAL, "No specific check implemented.");
    }

    public abstract double getCurrentValue();

    public abstract String getUnit();

    public abstract Threshold getThreshold();

    public abstract String getSensorType();
}
