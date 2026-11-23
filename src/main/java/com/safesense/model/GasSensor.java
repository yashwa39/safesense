package com.safesense.model;

/**
 * OOP: Inheritance — GasSensor is a Sensor with a gas reading in ppm.
 * Three instances are used: carbon monoxide, hydrogen sulfide, methane.
 */
public class GasSensor extends Sensor {

    private double gasLevel;
    private String gasType;
    private String unit;
    private Threshold threshold;

    /** PPT demo sensor: warning 50 ppm, critical 100 ppm. */
    public GasSensor(String sensorName, String location) {
        this(sensorName, location, "Toxic gas", "ppm", new Threshold(50, 100));
    }

    public GasSensor(String sensorName, String location, String gasType, Threshold threshold) {
        this(sensorName, location, gasType, "ppm", threshold);
    }

    public GasSensor(String sensorName, String location, String gasType, String unit, Threshold threshold) {
        super(sensorName, location);
        setGasType(gasType);
        setUnit(unit);
        setThreshold(threshold);
        this.gasLevel = 0;
    }

    public double getGasLevel() {
        return gasLevel;
    }

    /**
     * OOP: Exception handling — invalid values throw IllegalArgumentException,
     * which is caught here so the UI can show a grey "Sensor error" chip.
     */
    public void setGasLevel(double gasLevel) {
        try {
            if (Double.isNaN(gasLevel)) {
                throw new IllegalArgumentException("Gas level cannot be NaN!");
            }
            if (gasLevel < 0) {
                throw new IllegalArgumentException("Gas level cannot be negative!");
            }
            this.gasLevel = gasLevel;
            setSensorError(false);
        } catch (IllegalArgumentException ex) {
            System.out.println("ERROR INPUT: " + ex.getMessage());
            this.gasLevel = 0;
            setSensorError(true);
        }
    }

    public String getGasType() {
        return gasType;
    }

    public void setGasType(String gasType) {
        if (gasType == null || gasType.isBlank()) {
            throw new IllegalArgumentException("Gas type is required");
        }
        this.gasType = gasType.trim();
    }

    @Override
    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("Unit is required");
        }
        this.unit = unit.trim();
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
        return gasLevel;
    }

    @Override
    public String getSensorType() {
        return "GAS";
    }

    /**
     * OOP: Method overriding — gas-specific safety message.
     */
    @Override
    public StatusResult checkSafetyStatus() {
        if (isSensorError()) {
            return new StatusResult(Status.SENSOR_ERROR, "Sensor error");
        }
        Status status = threshold.classify(gasLevel);
        if (isGenericPptSensor()) {
            return switch (status) {
                case CRITICAL -> new StatusResult(status, "Toxic gas detected");
                case WARNING -> new StatusResult(status, "Gas level is elevated");
                default -> new StatusResult(Status.NORMAL, "Air is safe");
            };
        }
        String valueText = formatNumber(gasLevel);
        String warn = formatNumber(threshold.getWarningLimit());
        String crit = formatNumber(threshold.getCriticalLimit());
        return switch (status) {
            case CRITICAL -> new StatusResult(status,
                    gasType + " is " + valueText + " " + unit
                            + ", above the critical limit of " + crit + " " + unit + ".");
            case WARNING -> new StatusResult(status,
                    gasType + " is " + valueText + " " + unit
                            + ", above the warning limit of " + warn + " " + unit + ".");
            default -> new StatusResult(Status.NORMAL,
                    gasType + " is " + valueText + " " + unit + ", within safe limits.");
        };
    }

    private boolean isGenericPptSensor() {
        return "Toxic gas".equalsIgnoreCase(gasType);
    }

    static String formatNumber(double value) {
        if (value == Math.rint(value) && Math.abs(value) < 1_000_000) {
            return String.format("%.1f", value);
        }
        if (Math.abs(value) >= 1000) {
            return String.format("%.0f", value);
        }
        return String.format("%.1f", value);
    }
}
