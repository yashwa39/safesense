package com.safesense.controller;

import com.safesense.datasource.DemoMode;
import com.safesense.datasource.SimulatedDataSource;
import com.safesense.service.AlertService;
import com.safesense.service.SensorManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class DashboardController {

    private final SensorManager sensorManager;
    private final AlertService alertService;
    private final SimulatedDataSource simulatedDataSource;

    public DashboardController(SensorManager sensorManager,
                               AlertService alertService,
                               SimulatedDataSource simulatedDataSource) {
        this.sensorManager = sensorManager;
        this.alertService = alertService;
        this.simulatedDataSource = simulatedDataSource;
    }

    @GetMapping("/api/readings")
    public Map<String, Object> readings() {
        SensorManager.ZoneSnapshot snapshot = sensorManager.refresh();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("zone", snapshot.zone());
        body.put("plant", snapshot.plant());
        body.put("overallStatus", snapshot.overallStatus());
        body.put("updatedAt", snapshot.updatedAt());
        body.put("sensors", snapshot.sensors());
        body.put("newCriticalMessages", snapshot.newCriticalMessages());
        body.put("alertCount", alertService.size());
        body.put("demoMode", simulatedDataSource.getMode().name().toLowerCase());
        return body;
    }

    @GetMapping("/api/alerts")
    public Map<String, Object> alerts() {
        return Map.of(
                "alerts", alertService.latest(50),
                "count", alertService.size()
        );
    }

    @PostMapping("/api/demo-mode")
    public Map<String, String> demoMode(@RequestParam String mode) {
        simulatedDataSource.setMode(DemoMode.fromParam(mode));
        return Map.of("mode", simulatedDataSource.getMode().name().toLowerCase());
    }
}
