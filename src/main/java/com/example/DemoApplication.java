package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    private DemoApplication() {
        // Utility class containing only the application entry point
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}