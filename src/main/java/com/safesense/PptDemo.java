package com.safesense;

import com.safesense.model.GasSensor;
import com.safesense.model.StatusResult;

/**
 * OOP: Object creation — PPT walkthrough for U1–U3.
 * Run: mvn -q exec:java -Dexec.mainClass=com.safesense.PptDemo
 * (or run this class from the IDE).
 */
public class PptDemo {

    public static void main(String[] args) {
        GasSensor sensor = new GasSensor("PPT Gas Sensor", "Zone A");

        sensor.setGasLevel(30.0);
        print(sensor);

        sensor.setGasLevel(120.5);
        print(sensor);

        sensor.setGasLevel(-15.0);
        print(sensor);
    }

    private static void print(GasSensor sensor) {
        StatusResult result = sensor.checkSafetyStatus();
        System.out.println("Location: " + sensor.getLocation());
        System.out.println("Status: " + result.getStatus() + " - " + result.getMessage()
                + (result.getMessage().endsWith(".") || result.getMessage().endsWith("!") ? "" : "."));
        System.out.println("---------------------------------");
    }
}
