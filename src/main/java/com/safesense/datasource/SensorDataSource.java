package com.safesense.datasource;

import com.safesense.model.Reading;

import java.util.List;

/**
 * OOP: Interface — SensorManager depends on this type, not on SimulatedDataSource.
 * A HardwareDataSource (MQTT, serial or REST) can replace the simulator later
 * without changing any other class.
 */
public interface SensorDataSource {

    List<Reading> readAll();
}
