package com.safesense.service;

import com.safesense.model.Status;
import com.safesense.model.StatusResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Overall zone status = the worst status among all sensors.
 */
@Service
public class ZoneStatusService {

    public Status overall(List<StatusResult> results) {
        Status worst = Status.NORMAL;
        for (StatusResult result : results) {
            Status status = result.getStatus();
            if (status == Status.SENSOR_ERROR) {
                continue;
            }
            if (rank(status) > rank(worst)) {
                worst = status;
            }
        }
        return worst;
    }

    private static int rank(Status status) {
        return switch (status) {
            case SENSOR_ERROR, NORMAL -> 0;
            case WARNING -> 1;
            case CRITICAL -> 2;
        };
    }
}
