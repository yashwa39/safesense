package com.safesense.model;

/**
 * OOP: Inheritance — SoundSensor is a Sensor with a reading in decibels.
 */
public class SoundSensor extends Sensor {

    private double decibelLevel;
    private Threshold threshold;

    public SoundSensor(String sensorName, String location) {
        this(sensorName, location, new Threshold(85, 100));
    }

    public SoundSensor(String sensorName, String location, Threshold threshold) {
        super(sensorName, location);
        setThreshold(threshold);
        this.decibelLevel = 0;
    }

    public double getDecibelLevel() {
        return decibelLevel;
    }

    /**
     * OOP: Exception handling — negative or NaN values are rejected, then reset to 0.
     */
    public void setDecibelLevel(double decibelLevel) {
        try {
            if (Double.isNaN(decibelLevel)) {
                throw new IllegalArgumentException("Decibel level cannot be NaN!");
            }
            if (decibelLevel < 0) {
                throw new IllegalArgumentException("Decibel level cannot be negative!");
            }
            this.decibelLevel = decibelLevel;
            setSensorError(false);
        } catch (IllegalArgumentException ex) {
            System.out.println("ERROR INPUT: " + ex.getMessage());
            this.decibelLevel = 0;
            setSensorError(true);
        }
    }

    @Override
    public Threshold getThreshold() {
        return threshold;
    }

    public void setThreshold(Threshold threshold) {
        if (threshold == null) {
            throw new IllegalArgumentException("Threshold is required");
        }
        this.threshold = threshold;
    }

    @Override
    public double getCurrentValue() {
        return decibelLevel;
    }

    @Override
    public String getUnit() {
        return "dB";
    }

    @Override
    public String getSensorType() {
        return "SOUND";
    }

    /**
     * OOP: Method overriding — sound-specific safety message.
     */
    @Override
    public StatusResult checkSafetyStatus() {
        if (isSensorError()) {
            return new StatusResult(Status.SENSOR_ERROR, "Sensor error");
        }
        Status status = threshold.classify(decibelLevel);
        String valueText = GasSensor.formatNumber(decibelLevel);
        String warn = GasSensor.formatNumber(threshold.getWarningLimit());
        String crit = GasSensor.formatNumber(threshold.getCriticalLimit());
        return switch (status) {
            case CRITICAL -> new StatusResult(status,
                    "Sound level is " + valueText + " dB, above the critical limit of " + crit + " dB.");
            case WARNING -> new StatusResult(status,
                    "Sound level is " + valueText + " dB, above the warning limit of " + warn + " dB.");
            default -> new StatusResult(Status.NORMAL,
                    "Sound level is " + valueText + " dB, within safe limits.");
        };
    }
}
