package ar.edu.uade.comunitarias.controller;

import ar.edu.uade.comunitarias.dto.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/api/health")
    public HealthResponse health() {
        return new HealthResponse("Comunitarias", "UP");
    }
}
