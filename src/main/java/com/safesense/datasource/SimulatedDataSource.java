package com.safesense.datasource;

import com.safesense.model.Reading;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * OOP: Interface — SimulatedDataSource implements SensorDataSource.
 * AUTO drifts around safe values and occasionally spikes.
 */
@Component
public class SimulatedDataSource implements SensorDataSource {

    public static final String SOUND = "Sound level";
    public static final String CO = "Carbon monoxide";
    public static final String H2S = "Hydrogen sulfide";
    public static final String METHANE = "Methane";

    private volatile DemoMode mode = DemoMode.AUTO;
    private double sound = 67;
    private double co = 6;
    private double h2s = 1.9;
    private double methane = 183;

    public DemoMode getMode() {
        return mode;
    }

    public void setMode(DemoMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("Invalid mode");
        }
        this.mode = mode;
        applyFixedMode(mode);
    }

    @Override
    public List<Reading> readAll() {
        if (mode == DemoMode.AUTO) {
            drift();
        } else {
            applyFixedMode(mode);
        }
        return List.of(
                new Reading(SOUND, round(sound, 1)),
                new Reading(CO, round(co, 1)),
                new Reading(H2S, round(h2s, 1)),
                new Reading(METHANE, round(methane, 0))
        );
    }

    private void applyFixedMode(DemoMode next) {
        switch (next) {
            case NORMAL -> {
                sound = 67;
                co = 6;
                h2s = 1.9;
                methane = 183;
            }
            case WARNING -> {
                sound = 91;
                co = 42;
                h2s = 14.2;
                methane = 1450;
            }
            case CRITICAL -> {
                sound = 102;
                co = 59;
                h2s = 46.6;
                methane = 258;
            }
            case AUTO -> {
                // keep current drifting values
            }
        }
    }

    private void drift() {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        sound = clamp(sound + rng.nextDouble(-2.5, 2.5), 40, 95);
        co = clamp(co + rng.nextDouble(-1.2, 1.2), 1, 40);
        h2s = clamp(h2s + rng.nextDouble(-0.4, 0.4), 0.2, 12);
        methane = clamp(methane + rng.nextDouble(-40, 40), 50, 1400);
        // Occasional spike so Critical toasts can appear in a live demo.
        if (rng.nextDouble() < 0.08) {
            int pick = rng.nextInt(4);
            switch (pick) {
                case 0 -> sound = 102 + rng.nextDouble(0, 6);
                case 1 -> co = 210 + rng.nextDouble(0, 20);
                case 2 -> h2s = 62.0;
                default -> methane = 5200 + rng.nextDouble(0, 200);
            }
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double round(double value, int decimals) {
        double scale = Math.pow(10, decimals);
        return Math.round(value * scale) / scale;
    }
}
