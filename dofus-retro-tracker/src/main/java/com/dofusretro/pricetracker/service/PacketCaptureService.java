package com.dofusretro.pricetracker.service;

import com.dofusretro.pricetracker.config.PacketCaptureConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Service responsible for capturing network packets using Pcap4j.
 *
 * This service:
 * - Opens a network interface for packet capture
 * - Applies BPF filters to capture only Dofus traffic
 * - Extracts TCP payloads and queues them for processing
 * - Manages the lifecycle of the capture process
 *
 * The service starts automatically when the Spring context is initialized
 * (if enabled in configuration) and stops gracefully on shutdown.
 *
 * @author AGENT-NETWORK
 * @since 0.1.0
 */
@Service
@Slf4j
public class PacketCaptureService {

    private final BlockingQueue<byte[]> packetQueue;
    private final PacketCaptureConfig config;
    private PcapHandle handle;
    private Thread captureThread;
    private volatile boolean running = false;

    /**
     * Constructor for PacketCaptureService.
     *
     * @param packetQueue queue for storing captured packet data
     * @param config      packet capture configuration
     */
    public PacketCaptureService(
            @Qualifier("packetQueue") BlockingQueue<byte[]> packetQueue,
            PacketCaptureConfig config) {
        this.packetQueue = packetQueue;
        this.config = config;
    }

    /**
     * Starts the packet capture service.
     * Called automatically after bean construction.
     *
     * If packet capture is disabled in configuration, this method
     * returns immediately without starting capture.
     */
    @PostConstruct
    public void startCapture() {
        if (!config.isEnabled()) {
            log.info("Packet capture is disabled in configuration");
            return;
        }

        try {
            log.info("Starting packet capture service...");

            // Select network interface
            PcapNetworkInterface nif = selectNetworkInterface();
            log.info("Selected network interface: {} ({})",
                    nif.getName(),
                    nif.getDescription() != null ? nif.getDescription() : "no description");

            // Show interface addresses for debugging
            if (!nif.getAddresses().isEmpty()) {
                nif.getAddresses().forEach(addr ->
                        log.debug("  Interface address: {}", addr.getAddress()));
            }

            // Open interface for live capture
            PcapNetworkInterface.PromiscuousMode mode = config.isPromiscuousMode()
                    ? PcapNetworkInterface.PromiscuousMode.PROMISCUOUS
                    : PcapNetworkInterface.PromiscuousMode.NONPROMISCUOUS;

            handle = nif.openLive(
                    config.getSnapLen(),
                    mode,
                    config.getTimeout()
            );

            log.info("Opened capture handle (snaplen={}, timeout={}ms, mode={})",
                    config.getSnapLen(),
                    config.getTimeout(),
                    config.isPromiscuousMode() ? "promiscuous" : "non-promiscuous");

            // Apply BPF filter
            String filter = buildBpfFilter();
            handle.setFilter(filter, BpfCompileMode.OPTIMIZE);
            log.info("BPF filter applied: {}", filter);

            // Start capture thread
            running = true;
            captureThread = new Thread(this::captureLoop, "PacketCapture");
            captureThread.setDaemon(false); // Non-daemon to ensure graceful shutdown
            captureThread.start();

            log.info("Packet capture service started successfully");

        } catch (PcapNativeException e) {
            log.error("Failed to start packet capture: {}", e.getMessage());
            log.error("This may be due to:");
            log.error("  1. Insufficient permissions (try running with sudo or setting capabilities)");
            log.error("  2. libpcap/Npcap not installed");
            log.error("  3. Network interface not available");
            log.error("See docs/PCAP4J_SETUP.md for troubleshooting");
            throw new RuntimeException("Packet capture initialization failed", e);
        } catch (Exception e) {
            log.error("Unexpected error starting packet capture", e);
            throw new RuntimeException("Packet capture initialization failed", e);
        }
    }

    /**
     * Stops the packet capture service.
     * Called automatically before bean destruction.
     */
    @PreDestroy
    public void stopCapture() {
        if (!running) {
            log.debug("Packet capture is not running, nothing to stop");
            return;
        }

        log.info("Stopping packet capture service...");
        running = false;

        // Interrupt capture thread
        if (captureThread != null && captureThread.isAlive()) {
            captureThread.interrupt();
            try {
                captureThread.join(5000); // Wait up to 5 seconds
                if (captureThread.isAlive()) {
                    log.warn("Capture thread did not stop within timeout");
                }
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for capture thread to stop");
                Thread.currentThread().interrupt();
            }
        }

        // Close capture handle
        if (handle != null && handle.isOpen()) {
            handle.close();
            log.debug("Capture handle closed");
        }

        log.info("Packet capture service stopped");
    }

    /**
     * Main capture loop that runs in a separate thread.
     * Continuously captures packets and queues them for processing.
     */
    private void captureLoop() {
        log.info("Entering packet capture loop");

        PacketListener listener = packet -> {
            try {
                // Only process TCP packets
                if (!packet.contains(TcpPacket.class)) {
                    return;
                }

                TcpPacket tcp = packet.get(TcpPacket.class);

                // Extract TCP payload
                Packet payload = tcp.getPayload();
                if (payload == null) {
                    // No payload (control packet like SYN, ACK, FIN)
                    log.trace("Received TCP control packet (no payload)");
                    return;
                }

                byte[] data = payload.getRawData();
                if (data == null || data.length == 0) {
                    return;
                }

                log.debug("Captured packet: {} bytes from {}:{} -> {}:{}",
                        data.length,
                        tcp.getHeader().getSrcAddr(),
                        tcp.getHeader().getSrcPort(),
                        tcp.getHeader().getDstAddr(),
                        tcp.getHeader().getDstPort());

                // Enqueue for processing
                boolean added = packetQueue.offer(
                        data,
                        config.getQueueTimeout(),
                        TimeUnit.MILLISECONDS
                );

                if (!added) {
                    log.warn("Packet queue full (capacity={}), dropping {} byte packet",
                            packetQueue.size() + packetQueue.remainingCapacity(),
                            data.length);
                } else {
                    log.trace("Packet queued successfully (queue size: {})",
                            packetQueue.size());
                }

            } catch (InterruptedException e) {
                log.debug("Packet queueing interrupted");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Error processing packet", e);
            }
        };

        try {
            // Loop indefinitely until interrupted
            log.info("Starting packet capture loop (waiting for packets on port {})...",
                    config.getDofusPort());
            handle.loop(-1, listener);

        } catch (InterruptedException e) {
            log.info("Packet capture loop interrupted");
        } catch (PcapNativeException e) {
            if (running) {
                log.error("Error in capture loop: {}", e.getMessage(), e);
            } else {
                log.debug("Capture loop terminated during shutdown");
            }
        } catch (Exception e) {
            log.error("Unexpected error in capture loop", e);
        }

        log.info("Exited packet capture loop");
    }

    /**
     * Selects the network interface to use for capture.
     *
     * If an interface is specified in configuration, uses that.
     * Otherwise, selects the first non-loopback interface with an IP address.
     *
     * @return selected network interface
     * @throws PcapNativeException if no suitable interface is found
     */
    private PcapNetworkInterface selectNetworkInterface() throws PcapNativeException {
        List<PcapNetworkInterface> allDevs = Pcaps.findAllDevs();

        if (allDevs.isEmpty()) {
            throw new RuntimeException("No network interfaces found. " +
                    "Check permissions and libpcap/Npcap installation.");
        }

        log.debug("Found {} network interface(s)", allDevs.size());
        allDevs.forEach(dev ->
                log.debug("  - {} ({})",
                        dev.getName(),
                        dev.getDescription() != null ? dev.getDescription() : "no description"));

        // If interface specified in config, use it
        String configuredInterface = config.getNetworkInterface();
        if (configuredInterface != null && !configuredInterface.trim().isEmpty()) {
            log.info("Looking for configured interface: {}", configuredInterface);

            return allDevs.stream()
                    .filter(dev -> dev.getName().equals(configuredInterface))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(
                            "Specified network interface not found: " + configuredInterface +
                            ". Available interfaces: " + allDevs));
        }

        // Otherwise, auto-select first non-loopback interface
        log.info("Auto-selecting network interface (no interface specified in config)");

        return allDevs.stream()
                .filter(dev -> {
                    String name = dev.getName().toLowerCase();
                    boolean isLoopback = name.contains("lo") || name.contains("loopback");
                    boolean hasAddresses = !dev.getAddresses().isEmpty();

                    log.debug("Evaluating {}: loopback={}, hasAddresses={}",
                            dev.getName(), isLoopback, hasAddresses);

                    return !isLoopback && hasAddresses;
                })
                .findFirst()
                .orElseGet(() -> {
                    log.warn("No suitable non-loopback interface found, using first available: {}",
                            allDevs.get(0).getName());
                    return allDevs.get(0);
                });
    }

    /**
     * Builds the BPF (Berkeley Packet Filter) filter string.
     *
     * The filter captures TCP traffic on the configured Dofus port.
     *
     * @return BPF filter string
     */
    private String buildBpfFilter() {
        // Filter for Dofus Retro traffic on configured port
        return String.format("tcp port %d", config.getDofusPort());
    }

    /**
     * Checks if packet capture is currently running.
     *
     * @return true if capture is active, false otherwise
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Gets the current size of the packet queue.
     *
     * @return number of packets currently queued
     */
    public int getQueueSize() {
        return packetQueue.size();
    }

    /**
     * Gets the capture handle statistics (if available).
     *
     * @return capture statistics or null if not available
     */
    public PcapStat getStatistics() {
        if (handle != null && handle.isOpen()) {
            try {
                return handle.getStats();
            } catch (Exception e) {
                log.debug("Could not retrieve capture statistics: {}", e.getMessage());
            }
        }
        return null;
    }
}
