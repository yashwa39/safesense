package com.safesense.datasource;

import com.safesense.model.Reading;

import java.util.List;

/**
 * OOP: Interface — semester-4 stub. Point SensorManager at this class instead of
 * SimulatedDataSource (constructor injection only). No other class needs to change.
 *
 * Suggested adapters:
 * - MQTT subscriber (gas / sound topics)
 * - serial / USB lab module
 * - REST poll of a plant PLC gateway
 */
public class HardwareDataSource implements SensorDataSource {

    @Override
    public List<Reading> readAll() {
        throw new UnsupportedOperationException(
                "Semester 4: connect MQTT, serial or REST hardware here.");
    }
}
