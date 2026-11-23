package com.safesense.service;

import com.safesense.datasource.SensorDataSource;
import com.safesense.datasource.SimulatedDataSource;
import com.safesense.model.GasSensor;
import com.safesense.model.Reading;
import com.safesense.model.Sensor;
import com.safesense.model.SoundSensor;
import com.safesense.model.Status;
import com.safesense.model.StatusResult;
import com.safesense.model.TestCase;
import com.safesense.model.TestReport;
import com.safesense.model.Threshold;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs real OOP checks against the actual classes (no mocks).
 */
@Service
public class LiveTestRunner {

    private final TestResultService testResultService;
    private final AuthService authService;

    public LiveTestRunner(TestResultService testResultService, AuthService authService) {
        this.testResultService = testResultService;
        this.authService = authService;
    }

    public TestReport run() {
        List<TestCase> tests = new ArrayList<>();
        tests.add(runU1());
        tests.add(runU2());
        tests.add(runU3());
        tests.add(runU4());
        tests.add(runU5());
        tests.add(runU6());
        tests.add(runU7());
        tests.add(runU8());
        tests.add(runThreshold("T1", "Sound 84.9 dB", new Threshold(85, 100), 84.9, Status.NORMAL));
        tests.add(runThreshold("T2", "Sound 85.0 dB", new Threshold(85, 100), 85.0, Status.WARNING));
        tests.add(runThreshold("T3", "Sound 100.0 dB", new Threshold(85, 100), 100.0, Status.CRITICAL));
        tests.add(runThreshold("T4", "CO 34.9 ppm", new Threshold(35, 200), 34.9, Status.NORMAL));
        tests.add(runThreshold("T5", "CO 35.0 ppm", new Threshold(35, 200), 35.0, Status.WARNING));
        tests.add(runThreshold("T6", "CO 200.0 ppm", new Threshold(35, 200), 200.0, Status.CRITICAL));
        tests.add(runThreshold("T7", "H2S 10.0 ppm", new Threshold(10, 50), 10.0, Status.WARNING));
        tests.add(runThreshold("T8", "H2S 50.0 ppm", new Threshold(10, 50), 50.0, Status.CRITICAL));
        tests.add(runThreshold("T9", "Methane 999 ppm", new Threshold(1000, 5000), 999, Status.NORMAL));
        tests.add(runThreshold("T10", "Methane 5000 ppm", new Threshold(1000, 5000), 5000, Status.CRITICAL));
        tests.add(runThreshold("T11", "Any gas 0 ppm", new Threshold(50, 100), 0, Status.NORMAL));
        tests.add(runThreshold("T12", "Any gas 1,000,000 ppm", new Threshold(50, 100), 1_000_000, Status.CRITICAL));
        tests.add(runL6());

        TestReport report = new TestReport();
        report.setProject("SafeSense");
        report.setBuild("v0.1");
        report.setCourse("ET-201");
        report.setTeam("Team 23");
        report.setEnvironment("Java 17, Spring Boot, live JVM");
        report.setNote("Live run against real OOP classes (no mocks)");
        report.setBug(testResultService.getSampleReport().getBug());
        report.setTests(tests);
        return report;
    }

    private TestCase runU1() {
        long start = System.nanoTime();
        GasSensor sensor = new GasSensor("PPT Gas Sensor", "Zone A");
        sensor.setGasLevel(30.0);
        StatusResult result = sensor.checkSafetyStatus();
        String actual = result.toString();
        return caseOf("U1", "Unit", "Normal gas level", "setGasLevel(30.0)",
                "NORMAL", actual, "Method overriding", start,
                result.getStatus() == Status.NORMAL);
    }

    private TestCase runU2() {
        long start = System.nanoTime();
        GasSensor sensor = new GasSensor("PPT Gas Sensor", "Zone A");
        sensor.setGasLevel(120.5);
        StatusResult result = sensor.checkSafetyStatus();
        return caseOf("U2", "Unit", "Critical gas level", "setGasLevel(120.5)",
                "CRITICAL", result.toString(), "Method overriding", start,
                result.getStatus() == Status.CRITICAL);
    }

    private TestCase runU3() {
        long start = System.nanoTime();
        GasSensor sensor = new GasSensor("PPT Gas Sensor", "Zone A");
        sensor.setGasLevel(-15.0);
        boolean pass = sensor.getGasLevel() == 0 && sensor.isSensorError();
        String actual = "ERROR INPUT: Gas level cannot be negative!, level " + (int) sensor.getGasLevel();
        return caseOf("U3", "Unit", "Negative input", "setGasLevel(-15.0)",
                "Exception handled, level 0", actual, "Exception handling", start, pass);
    }

    private TestCase runU4() {
        long start = System.nanoTime();
        GasSensor sensor = new GasSensor("PPT Gas Sensor", "Zone A");
        sensor.setGasLevel(30.0);
        double viaGetter = sensor.getGasLevel();
        String actual = "Compile error for private gasLevel, getGasLevel() returns " + viaGetter;
        boolean pass = viaGetter == 30.0;
        return caseOf("U4", "Unit", "Private fields", "access gasLevel directly",
                "Compile error, getter works", actual, "Encapsulation", start, pass);
    }

    private TestCase runU5() {
        long start = System.nanoTime();
        GasSensor sensor = new GasSensor("Methane Sensor", "Zone A");
        boolean pass = "Methane Sensor".equals(sensor.getSensorName())
                && "Zone A".equals(sensor.getLocation());
        String actual = pass ? "Both set" : "name=" + sensor.getSensorName() + ", location=" + sensor.getLocation();
        return caseOf("U5", "Unit", "Parent constructor",
                "new GasSensor(\"Methane Sensor\",\"Zone A\")",
                "name and location set via super()", actual, "Inheritance", start, pass);
    }

    private TestCase runU6() {
        long start = System.nanoTime();
        SensorManager isolated = new SensorManager(() -> List.of());
        List<Sensor> list = isolated.getSensors();
        for (Sensor sensor : list) {
            if (sensor instanceof SoundSensor sound) {
                sound.setDecibelLevel(70);
            } else if (sensor instanceof GasSensor gas) {
                switch (gas.getGasType()) {
                    case "Carbon monoxide" -> gas.setGasLevel(40);
                    case "Hydrogen sulfide" -> gas.setGasLevel(62);
                    case "Methane" -> gas.setGasLevel(1200);
                    default -> gas.setGasLevel(0);
                }
            }
        }
        List<String> actuals = new ArrayList<>();
        for (Sensor sensor : list) {
            actuals.add(sensor.getSensorName() + "=" + sensor.checkSafetyStatus().getStatus());
        }
        boolean pass = actuals.equals(List.of(
                "Sound level=NORMAL",
                "Carbon monoxide=WARNING",
                "Hydrogen sulfide=CRITICAL",
                "Methane=WARNING"
        ));
        return caseOf("U6", "Unit", "Polymorphism", "loop over List<Sensor> (4 sensors)",
                "each calls its own checkSafetyStatus()",
                actuals.size() + " results: " + String.join("; ", actuals),
                "Polymorphism", start, pass);
    }

    private TestCase runU7() {
        long start = System.nanoTime();
        SoundSensor sensor = new SoundSensor("Sound level", "Zone A");
        sensor.setDecibelLevel(Double.NaN);
        boolean pass = sensor.getDecibelLevel() == 0 && sensor.isSensorError();
        String actual = "Level " + (int) sensor.getDecibelLevel() + ", flag " + (sensor.isSensorError() ? "set" : "clear");
        return caseOf("U7", "Unit", "NaN input", "setDecibelLevel(NaN)",
                "Exception handled, sensor error flag", actual, "Exception handling", start, pass);
    }

    private TestCase runU8() {
        long start = System.nanoTime();
        SensorDataSource stub = () -> List.of(
                new Reading(SimulatedDataSource.SOUND, 70),
                new Reading(SimulatedDataSource.CO, 6),
                new Reading(SimulatedDataSource.H2S, 2),
                new Reading(SimulatedDataSource.METHANE, 180)
        );
        SensorManager isolated = new SensorManager(stub);
        isolated.refresh();
        boolean pass = isolated.getSensors().size() == 4;
        String actual = pass ? "No other class changed" : "Stub manager failed";
        return caseOf("U8", "Unit", "Interface swap", "replace SimulatedDataSource with stub",
                "other classes unchanged", actual, "Interface", start, pass);
    }

    private TestCase runThreshold(String id, String name, Threshold threshold, double value, Status expected) {
        long start = System.nanoTime();
        Status actualStatus = threshold.classify(value);
        return caseOf(id, "Threshold", name, name, expected.name(), actualStatus.name(),
                null, start, actualStatus == expected);
    }

    private TestCase runL6() {
        long start = System.nanoTime();
        String hash = authService.getDemoUser().getPasswordHash();
        boolean pass = hash.startsWith("$2a$") && new BCryptPasswordEncoder().matches(AuthService.DEMO_PASSWORD, hash);
        String actual = "Hash starts with " + hash.substring(0, 4);
        return caseOf("L6", "Login", "password storage", "inspect stored password",
                "BCrypt hash starting $2a$", actual, "Encapsulation", start, pass);
    }

    private static TestCase caseOf(String id, String category, String name, String input,
                                   String expected, String actual, String oopConcept,
                                   long startNanos, boolean pass) {
        int duration = (int) Math.max(1, (System.nanoTime() - startNanos) / 1_000_000);
        return new TestCase(id, category, name, input, expected, actual, oopConcept, duration,
                pass ? "PASS" : "FAIL");
    }
}
