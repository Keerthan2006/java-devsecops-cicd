package com.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return """
                <h1>Java CI/CD Demo</h1>
                <p> Implementing DevSecOps Pipeline for a Java Application </p>
                <p>Application is running successfully! after passing all security scans</p>
                """;
    }

    @GetMapping("/health")
    public String health() {
        return "Application is Healthy";
    }

}