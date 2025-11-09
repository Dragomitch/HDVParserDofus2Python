package com.dofusretro.pricetracker.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Configuration for packet queues and related beans.
 *
 * This configuration provides:
 * - A thread-safe blocking queue for captured packets
 * - Monitoring and metrics for queue health
 *
 * @author AGENT-NETWORK
 * @since 0.1.0
 */
@Configuration
@EnableScheduling
public class QueueConfig {

    /**
     * Creates the packet queue bean.
     *
     * This queue serves as a buffer between the packet capture service
     * and the packet processor. It decouples packet capture from packet
     * processing, allowing them to operate at different speeds.
     *
     * @param config packet capture configuration
     * @return thread-safe blocking queue for packet data
     */
    @Bean("packetQueue")
    public BlockingQueue<byte[]> packetQueue(PacketCaptureConfig config) {
        int capacity = config.getQueueCapacity();
        return new LinkedBlockingQueue<>(capacity);
    }

    /**
     * Creates a metrics bean to monitor queue health.
     *
     * @param queue the packet queue to monitor
     * @return QueueMetrics instance
     */
    @Bean
    public QueueMetrics queueMetrics(@Qualifier("packetQueue") BlockingQueue<byte[]> queue) {
        return new QueueMetrics(queue);
    }
}

/**
 * Component that monitors and logs packet queue metrics.
 *
 * This component periodically checks the queue size and logs warnings
 * if the queue is getting full, which could indicate that packet
 * processing is falling behind packet capture.
 */
@Component
@Slf4j
class QueueMetrics {

    private final BlockingQueue<byte[]> queue;
    private static final double WARNING_THRESHOLD = 0.8; // Warn at 80% full
    private static final double CRITICAL_THRESHOLD = 0.95; // Critical at 95% full

    public QueueMetrics(BlockingQueue<byte[]> queue) {
        this.queue = queue;
    }

    /**
     * Periodically logs queue size and health status.
     * Runs every 10 seconds.
     */
    @Scheduled(fixedDelay = 10000)
    public void logQueueSize() {
        int size = queue.size();
        int remaining = queue.remainingCapacity();
        int capacity = size + remaining;

        // Only log if there are packets in the queue
        if (size > 0) {
            log.debug("Packet queue: {} packets ({}/{} capacity)",
                    size, size, capacity);
        }

        // Calculate fill percentage
        if (capacity > 0) {
            double fillRatio = (double) size / capacity;

            if (fillRatio >= CRITICAL_THRESHOLD) {
                log.error("CRITICAL: Packet queue is {}% full ({}/{}). " +
                         "Packets may be dropped! Consider increasing queue capacity " +
                         "or optimizing packet processing.",
                        Math.round(fillRatio * 100), size, capacity);
            } else if (fillRatio >= WARNING_THRESHOLD) {
                log.warn("WARNING: Packet queue is {}% full ({}/{}). " +
                        "Processing may be falling behind capture rate.",
                        Math.round(fillRatio * 100), size, capacity);
            }
        }
    }

    /**
     * Get current queue size.
     *
     * @return number of packets currently in queue
     */
    public int getCurrentSize() {
        return queue.size();
    }

    /**
     * Get queue capacity.
     *
     * @return maximum queue capacity
     */
    public int getCapacity() {
        return queue.size() + queue.remainingCapacity();
    }

    /**
     * Get queue utilization as a percentage.
     *
     * @return utilization percentage (0-100)
     */
    public double getUtilizationPercent() {
        int size = queue.size();
        int capacity = size + queue.remainingCapacity();
        return capacity > 0 ? (size * 100.0 / capacity) : 0.0;
    }
}
