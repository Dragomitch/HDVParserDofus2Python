package com.dofusretro.pricetracker.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for packet capture service.
 *
 * This class externalizes all packet capture settings, making them
 * configurable via application.yml or environment variables.
 *
 * @author AGENT-NETWORK
 * @since 0.1.0
 */
@Configuration
@ConfigurationProperties(prefix = "packet.capture")
@Data
@Validated
public class PacketCaptureConfig {

    /**
     * Enable or disable packet capture.
     * When disabled, the PacketCaptureService will not start.
     * Default: true
     */
    private boolean enabled = true;

    /**
     * Dofus Retro server port to monitor.
     * Default: 5555 (standard Dofus Retro port)
     *
     * Valid range: 1-65535
     */
    @Min(1)
    @Max(65535)
    private int dofusPort = 5555;

    /**
     * Network interface name to capture on.
     * If null or empty, the service will auto-detect the first
     * non-loopback interface with an IP address.
     *
     * Examples: "eth0", "wlan0", "en0"
     * Default: null (auto-detect)
     */
    private String networkInterface;

    /**
     * Snapshot length - maximum bytes to capture per packet.
     * This determines how much of each packet to capture.
     *
     * Typical Dofus packets are < 2000 bytes, but setting higher
     * ensures we capture complete packets.
     *
     * Valid range: 1500-65536
     * Default: 65536 (64KB - full packet)
     */
    @Min(1500)
    private int snapLen = 65536;

    /**
     * Read timeout in milliseconds.
     * How long to wait for packets before checking if capture should stop.
     *
     * Lower values = more responsive shutdown
     * Higher values = less CPU usage
     *
     * Valid range: 100-10000
     * Default: 1000 (1 second)
     */
    @Min(100)
    private int timeout = 1000;

    /**
     * Packet queue capacity.
     * Maximum number of packets that can be queued for processing.
     *
     * If the queue fills up, new packets will be dropped.
     * Monitor queue size via actuator health endpoint.
     *
     * Valid range: 10-100000
     * Default: 1000
     */
    @Min(10)
    private int queueCapacity = 1000;

    /**
     * Queue offer timeout in milliseconds.
     * How long to wait when adding a packet to the queue before giving up.
     *
     * If the queue is full and timeout expires, the packet is dropped.
     *
     * Valid range: 10-5000
     * Default: 100 (0.1 seconds)
     */
    @Min(10)
    private int queueTimeout = 100;

    /**
     * Enable promiscuous mode.
     * When enabled, captures all packets on the network segment,
     * not just those addressed to this host.
     *
     * For Dofus tracking, this is usually not needed since we're
     * capturing local client traffic.
     *
     * Default: false
     */
    private boolean promiscuousMode = false;
}
