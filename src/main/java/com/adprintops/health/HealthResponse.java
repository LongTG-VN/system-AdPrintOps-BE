package com.adprintops.health;

import java.time.Instant;

/**
 * Modern Java Record representing API Health Status.
 * Package-by-Feature: Located inside com.adprintops.health
 */
public record HealthResponse(
        String status,
        String service,
        String version,
        Instant timestamp
) {}
