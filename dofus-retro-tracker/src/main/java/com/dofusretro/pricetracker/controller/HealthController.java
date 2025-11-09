package com.dofusretro.pricetracker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for health check endpoints.
 * Provides API health status information.
 *
 * @author AGENT-API
 * @version 1.0
 * @since Wave 2
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "API health check endpoints")
public class HealthController {

    private final HealthEndpoint healthEndpoint;

    /**
     * Get API health status.
     *
     * @return health status information
     */
    @Operation(summary = "Get API health status",
            description = "Returns the health status of the API and its components")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API is healthy",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "503", description = "API is unhealthy")
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> getHealth() {
        log.debug("GET /api/v1/health");

        Health health = healthEndpoint.health();
        Map<String, Object> response = new HashMap<>();
        response.put("status", health.getStatus().getCode());
        response.put("components", health.getDetails());

        if (health.getStatus().getCode().equals("UP")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(503).body(response);
        }
    }
}
