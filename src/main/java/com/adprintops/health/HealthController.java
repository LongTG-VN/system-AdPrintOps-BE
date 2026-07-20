package com.adprintops.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<HealthResponse> checkHealth() {
        HealthResponse health = new HealthResponse(
                "UP",
                "AdPrintOps Core Backend",
                "0.0.1-SNAPSHOT",
                Instant.now()
        );
        return ResponseEntity.ok(health);
    }
}
