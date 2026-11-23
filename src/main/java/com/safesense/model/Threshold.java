package com.safesense.model;

/**
 * OOP: Encapsulation — warning and critical limits with a single classify() rule.
 * Critical if value &gt;= criticalLimit, Warning if value &gt;= warningLimit, else Normal.
 */
public class Threshold {

    private double warningLimit;
    private double criticalLimit;

    public Threshold(double warningLimit, double criticalLimit) {
        setWarningLimit(warningLimit);
        setCriticalLimit(criticalLimit);
    }

    public double getWarningLimit() {
        return warningLimit;
    }

    public void setWarningLimit(double warningLimit) {
        if (Double.isNaN(warningLimit) || warningLimit < 0) {
            throw new IllegalArgumentException("Warning limit must be a non-negative number");
        }
        this.warningLimit = warningLimit;
    }

    public double getCriticalLimit() {
        return criticalLimit;
    }

    public void setCriticalLimit(double criticalLimit) {
        if (Double.isNaN(criticalLimit) || criticalLimit < 0) {
            throw new IllegalArgumentException("Critical limit must be a non-negative number");
        }
        this.criticalLimit = criticalLimit;
    }

    /**
     * Uses &gt;= so a value exactly on the limit is classified at that severity
     * (BUG-01: the first version used &gt; and mis-classified H2S at 50.0 ppm).
     */
    public Status classify(double value) {
        if (Double.isNaN(value)) {
            return Status.SENSOR_ERROR;
        }
        if (value >= criticalLimit) {
            return Status.CRITICAL;
        }
        if (value >= warningLimit) {
            return Status.WARNING;
        }
        return Status.NORMAL;
    }
}
