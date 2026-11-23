package com.safesense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SafeSense prototype (ET-201, Team 23).
 * Run with: mvn spring-boot:run
 * The server port is read from the PORT environment variable (default 8080).
 */
@SpringBootApplication
public class SafeSenseApplication {

    public static void main(String[] args) {
        SpringApplication.run(SafeSenseApplication.class, args);
    }
}
