package com.dofusretro.pricetracker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Shutdown hook for graceful packet capture termination.
 *
 * This component ensures that packet capture is stopped cleanly
 * when the JVM is shutting down, even if the normal Spring shutdown
 * process is interrupted.
 *
 * The JVM shutdown hook is registered when this component is created
 * and will execute when the JVM begins its shutdown sequence.
 *
 * @author AGENT-NETWORK
 * @since 0.1.0
 */
@Component
@Slf4j
public class PacketCaptureShutdownHook {

    private final PacketCaptureService captureService;

    /**
     * Constructor that registers the shutdown hook.
     *
     * @param captureService the packet capture service to stop on shutdown
     */
    public PacketCaptureShutdownHook(PacketCaptureService captureService) {
        this.captureService = captureService;

        // Register JVM shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("JVM shutdown detected, stopping packet capture...");
            try {
                captureService.stopCapture();
                log.info("Packet capture stopped successfully during JVM shutdown");
            } catch (Exception e) {
                log.error("Error stopping packet capture during JVM shutdown", e);
            }
        }, "PacketCaptureShutdownHook"));

        log.debug("Packet capture shutdown hook registered");
    }
}
