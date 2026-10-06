package com.sachin.bloodbank.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/api/status")
    public Map<String, Object> home() {
        return Map.of("service", "Blood Bank Finder", "status", "UP", "version", "1.0.0");
    }
}
