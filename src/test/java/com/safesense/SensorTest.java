package com.safesense;

import com.safesense.model.GasSensor;
import com.safesense.model.SoundSensor;
import com.safesense.model.Status;
import com.safesense.model.Threshold;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PPT cases plus threshold edges: exactly 50, exactly 100, 0, very large values.
 */
class SensorTest {

    @Test
    void pptNormalGasLevel() {
        GasSensor sensor = new GasSensor("PPT Gas Sensor", "Zone A");
        sensor.setGasLevel(30.0);
        assertEquals(Status.NORMAL, sensor.checkSafetyStatus().getStatus());
    }

    @Test
    void pptCriticalGasLevel() {
        GasSensor sensor = new GasSensor("PPT Gas Sensor", "Zone A");
        sensor.setGasLevel(120.5);
        assertEquals(Status.CRITICAL, sensor.checkSafetyStatus().getStatus());
    }

    @Test
    void pptNegativeInputHandled() {
        GasSensor sensor = new GasSensor("PPT Gas Sensor", "Zone A");
        sensor.setGasLevel(-15.0);
        assertEquals(0.0, sensor.getGasLevel());
        assertTrue(sensor.isSensorError());
        assertEquals(Status.SENSOR_ERROR, sensor.checkSafetyStatus().getStatus());
    }

    @Test
    void exactlyFiftyIsCriticalForPptSensor() {
        Threshold threshold = new Threshold(50, 100);
        assertEquals(Status.CRITICAL, threshold.classify(50));
        assertEquals(Status.CRITICAL, threshold.classify(100));
    }

    @Test
    void zeroIsNormalAndHugeValueIsCritical() {
        Threshold threshold = new Threshold(50, 100);
        assertEquals(Status.NORMAL, threshold.classify(0));
        assertEquals(Status.CRITICAL, threshold.classify(1_000_000));
    }

    @Test
    void hydrogenSulfideBoundaryUsesGreaterOrEqual() {
        Threshold h2s = new Threshold(10, 50);
        assertEquals(Status.WARNING, h2s.classify(10.0));
        assertEquals(Status.CRITICAL, h2s.classify(50.0));
    }

    @Test
    void soundNaNMarksSensorError() {
        SoundSensor sensor = new SoundSensor("Sound level", "Zone A");
        sensor.setDecibelLevel(Double.NaN);
        assertEquals(0.0, sensor.getDecibelLevel());
        assertTrue(sensor.isSensorError());
    }

    @Test
    void validReadingClearsSensorError() {
        GasSensor sensor = new GasSensor("PPT Gas Sensor", "Zone A");
        sensor.setGasLevel(-1);
        assertTrue(sensor.isSensorError());
        sensor.setGasLevel(10);
        assertFalse(sensor.isSensorError());
        assertEquals(Status.NORMAL, sensor.checkSafetyStatus().getStatus());
    }
}
