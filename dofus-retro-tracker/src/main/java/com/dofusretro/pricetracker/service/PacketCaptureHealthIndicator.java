package com.dofusretro.pricetracker.service;

import com.dofusretro.pricetracker.config.PacketCaptureConfig;
import lombok.extern.slf4j.Slf4j;
import org.pcap4j.core.PcapStat;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator for the packet capture service.
 *
 * This component integrates with Spring Boot Actuator to provide
 * health information about the packet capture service. It can be
 * accessed via the /actuator/health endpoint.
 *
 * Health status is determined by:
 * - Whether capture is running (if enabled)
 * - Queue utilization
 * - Packet drop statistics (if available)
 *
 * @author AGENT-NETWORK
 * @since 0.1.0
 */
@Component
@Slf4j
public class PacketCaptureHealthIndicator implements HealthIndicator {

    private final PacketCaptureService captureService;
    private final PacketCaptureConfig config;

    public PacketCaptureHealthIndicator(
            PacketCaptureService captureService,
            PacketCaptureConfig config) {
        this.captureService = captureService;
        this.config = config;
    }

    @Override
    public Health health() {
        // If packet capture is disabled, report as UP with disabled status
        if (!config.isEnabled()) {
            return Health.up()
                    .withDetail("status", "disabled")
                    .withDetail("message", "Packet capture is disabled in configuration")
                    .build();
        }

        // If enabled but not running, report as DOWN
        if (!captureService.isRunning()) {
            return Health.down()
                    .withDetail("status", "stopped")
                    .withDetail("message", "Packet capture service is not running")
                    .build();
        }

        // Service is running - check queue health
        int queueSize = captureService.getQueueSize();
        int queueCapacity = config.getQueueCapacity();
        double queueUtilization = (double) queueSize / queueCapacity * 100;

        Health.Builder healthBuilder = Health.up()
                .withDetail("status", "capturing")
                .withDetail("queueSize", queueSize)
                .withDetail("queueCapacity", queueCapacity)
                .withDetail("queueUtilization", String.format("%.1f%%", queueUtilization))
                .withDetail("dofusPort", config.getDofusPort());

        // Add network interface info
        if (config.getNetworkInterface() != null && !config.getNetworkInterface().isEmpty()) {
            healthBuilder.withDetail("networkInterface", config.getNetworkInterface());
        } else {
            healthBuilder.withDetail("networkInterface", "auto-detected");
        }

        // Add capture statistics if available
        try {
            PcapStat stats = captureService.getStatistics();
            if (stats != null) {
                healthBuilder
                        .withDetail("packetsReceived", stats.getNumPacketsReceived())
                        .withDetail("packetsDropped", stats.getNumPacketsDropped())
                        .withDetail("packetsDroppedByInterface", stats.getNumPacketsDroppedByIf());

                // Warn if packets are being dropped
                if (stats.getNumPacketsDropped() > 0) {
                    healthBuilder.withDetail("warning",
                            "Packets are being dropped. Consider increasing buffer size.");
                }
            }
        } catch (Exception e) {
            log.debug("Could not retrieve capture statistics: {}", e.getMessage());
        }

        // Determine overall health based on queue utilization
        if (queueUtilization >= 95) {
            return healthBuilder
                    .down()
                    .withDetail("issue", "Queue is critically full (>95%)")
                    .withDetail("recommendation", "Increase queue capacity or optimize packet processing")
                    .build();
        } else if (queueUtilization >= 80) {
            return healthBuilder
                    .status("WARNING")
                    .withDetail("warning", "Queue utilization is high (>80%)")
                    .build();
        }

        return healthBuilder.build();
    }
}
