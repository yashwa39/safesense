package com.safesense.service;

import com.safesense.model.Alert;
import com.safesense.model.Sensor;
import com.safesense.model.Status;
import com.safesense.model.StatusResult;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates an Alert when a sensor's status gets worse. Keeps the latest 50.
 */
@Service
public class AlertService {

    private static final int MAX_ALERTS = 50;

    private final Deque<Alert> alerts = new ArrayDeque<>();
    private final Map<String, Status> lastStatus = new HashMap<>();

    public synchronized Alert recordIfWorsened(Sensor sensor, StatusResult result) {
        Status next = result.getStatus();
        if (next == Status.SENSOR_ERROR || next == Status.NORMAL) {
            lastStatus.put(sensor.getSensorName(), next);
            return null;
        }
        Status previous = lastStatus.getOrDefault(sensor.getSensorName(), Status.NORMAL);
        lastStatus.put(sensor.getSensorName(), next);
        if (!isWorse(previous, next)) {
            return null;
        }
        Alert alert = new Alert(
                sensor.getSensorName(),
                sensor.getCurrentValue(),
                sensor.getUnit(),
                next,
                result.getMessage()
        );
        alerts.addFirst(alert);
        while (alerts.size() > MAX_ALERTS) {
            alerts.removeLast();
        }
        return alert;
    }

    public synchronized List<Alert> latest(int limit) {
        List<Alert> copy = new ArrayList<>();
        int i = 0;
        for (Alert alert : alerts) {
            if (i++ >= limit) {
                break;
            }
            copy.add(alert);
        }
        return copy;
    }

    public synchronized int size() {
        return alerts.size();
    }

    public synchronized void clear() {
        alerts.clear();
        lastStatus.clear();
    }

    private static boolean isWorse(Status previous, Status next) {
        return rank(next) > rank(previous);
    }

    private static int rank(Status status) {
        return switch (status) {
            case SENSOR_ERROR, NORMAL -> 0;
            case WARNING -> 1;
            case CRITICAL -> 2;
        };
    }
}
