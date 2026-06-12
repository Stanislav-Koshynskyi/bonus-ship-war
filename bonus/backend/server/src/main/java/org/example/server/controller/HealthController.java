package org.example.server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("api")
public class HealthController {
    @GetMapping("/health")
    public String health() {
        return "Server is running!";
    }
}