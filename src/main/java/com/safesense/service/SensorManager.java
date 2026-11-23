package com.safesense.service;

import com.safesense.datasource.SensorDataSource;
import com.safesense.model.GasSensor;
import com.safesense.model.Reading;
import com.safesense.model.Sensor;
import com.safesense.model.SoundSensor;
import com.safesense.model.Status;
import com.safesense.model.StatusResult;
import com.safesense.model.Threshold;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OOP: Object creation — builds the four zone sensors.
 * OOP: Polymorphism — stores them as List&lt;Sensor&gt; and calls checkSafetyStatus() on each.
 */
@Service
public class SensorManager {

    public static final String ZONE = "Workshop Line 2";
    public static final String PLANT = "Plant A";

    private final SensorDataSource dataSource;
    private final AlertService alertService;
    private final ZoneStatusService zoneStatusService;
    private final List<Sensor> sensors = new ArrayList<>();

    public SensorManager(SensorDataSource dataSource,
                         AlertService alertService,
                         ZoneStatusService zoneStatusService) {
        this.dataSource = dataSource;
        this.alertService = alertService;
        this.zoneStatusService = zoneStatusService;
        createZoneSensors();
    }

    /** Used by live tests to prove constructor injection of any SensorDataSource. */
    public SensorManager(SensorDataSource dataSource) {
        this(dataSource, new AlertService(), new ZoneStatusService());
    }

    private void createZoneSensors() {
        // OOP: Object creation
        sensors.add(new SoundSensor("Sound level", ZONE));
        sensors.add(new GasSensor("Carbon monoxide", ZONE, "Carbon monoxide", new Threshold(35, 200)));
        sensors.add(new GasSensor("Hydrogen sulfide", ZONE, "Hydrogen sulfide", new Threshold(10, 50)));
        sensors.add(new GasSensor("Methane", ZONE, "Methane", new Threshold(1000, 5000)));
    }

    public List<Sensor> getSensors() {
        return List.copyOf(sensors);
    }

    public ZoneSnapshot refresh() {
        List<Reading> readings = dataSource.readAll();
        Map<String, Double> byName = new LinkedHashMap<>();
        for (Reading reading : readings) {
            byName.put(reading.getSensorName(), reading.getValue());
        }
        applyReadings(byName);

        List<SensorSnapshot> snapshots = new ArrayList<>();
        List<StatusResult> results = new ArrayList<>();
        List<String> newCriticalMessages = new ArrayList<>();
        // OOP: Polymorphism — each child runs its own overridden checkSafetyStatus()
        for (Sensor sensor : sensors) {
            StatusResult result = sensor.checkSafetyStatus();
            results.add(result);
            var created = alertService.recordIfWorsened(sensor, result);
            if (created != null && created.getStatus() == Status.CRITICAL) {
                newCriticalMessages.add(result.getMessage());
            }
            snapshots.add(SensorSnapshot.from(sensor, result));
        }
        Status overall = zoneStatusService.overall(results);
        return new ZoneSnapshot(ZONE, PLANT, overall, Instant.now(), snapshots, newCriticalMessages);
    }

    private void applyReadings(Map<String, Double> byName) {
        for (Sensor sensor : sensors) {
            Double value = byName.get(sensor.getSensorName());
            if (value == null) {
                continue;
            }
            if (sensor instanceof GasSensor gasSensor) {
                gasSensor.setGasLevel(value);
            } else if (sensor instanceof SoundSensor soundSensor) {
                soundSensor.setDecibelLevel(value);
            }
        }
    }

    public record SensorSnapshot(
            String name,
            String type,
            Double value,
            String unit,
            Status status,
            String message,
            double warningLimit,
            double criticalLimit,
            boolean error
    ) {
        static SensorSnapshot from(Sensor sensor, StatusResult result) {
            boolean error = result.getStatus() == Status.SENSOR_ERROR;
            return new SensorSnapshot(
                    sensor.getSensorName(),
                    sensor.getSensorType(),
                    error ? null : sensor.getCurrentValue(),
                    sensor.getUnit(),
                    result.getStatus(),
                    result.getMessage(),
                    sensor.getThreshold().getWarningLimit(),
                    sensor.getThreshold().getCriticalLimit(),
                    error
            );
        }
    }

    public record ZoneSnapshot(
            String zone,
            String plant,
            Status overallStatus,
            Instant updatedAt,
            List<SensorSnapshot> sensors,
            List<String> newCriticalMessages
    ) {
    }
}
