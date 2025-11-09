package com.dofusretro.pricetracker.task;

import com.dofusretro.pricetracker.exception.BusinessException;
import com.dofusretro.pricetracker.service.PacketConsumerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled background task for processing packets from the queue.
 * <p>
 * This task runs periodically to:
 * <ul>
 *   <li>Consume packets from the packet queue</li>
 *   <li>Process them through the ItemPriceService</li>
 *   <li>Monitor queue depth and processing rate</li>
 *   <li>Handle backpressure when queue fills up</li>
 * </ul>
 * </p>
 * <p>
 * The task can be enabled/disabled via configuration and supports
 * both batch and single packet processing modes.
 * </p>
 * <p>
 * Scheduling options:
 * <ul>
 *   <li><strong>Fixed delay</strong> - Wait N ms after task completion</li>
 *   <li><strong>Fixed rate</strong> - Run every N ms regardless of completion</li>
 *   <li><strong>Cron</strong> - Run on cron schedule</li>
 * </ul>
 * </p>
 *
 * @author AGENT-BUSINESS
 * @version 1.0
 * @since Wave 2
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        value = "dofus.retro.tracker.packet-processing.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class PacketProcessingTask {

    private final PacketConsumerService consumerService;

    @Value("${dofus.retro.tracker.packet-processing.batch-mode:true}")
    private boolean batchMode;

    @Value("${dofus.retro.tracker.packet-processing.log-stats:true}")
    private boolean logStats;

    @Value("${dofus.retro.tracker.packet-processing.queue-warning-threshold:500}")
    private int queueWarningThreshold;

    /**
     * Process packets from the queue.
     * <p>
     * This method runs on a fixed delay schedule (default: 1 second).
     * It processes packets using either batch or single packet mode
     * based on configuration.
     * </p>
     * <p>
     * The @Scheduled annotation with fixedDelayString ensures this
     * task waits for the configured delay AFTER the previous execution
     * completes, preventing overlap.
     * </p>
     */
    @Scheduled(fixedDelayString = "${dofus.retro.tracker.packet-processing.interval-ms:1000}")
    public void processPackets() {
        try {
            // Check queue size
            int queueSize = consumerService.getQueueSize();

            if (queueSize == 0) {
                log.trace("Packet queue is empty, nothing to process");
                return;
            }

            // Log queue status if above threshold
            if (queueSize > queueWarningThreshold) {
                log.warn("Packet queue is growing large: {} packets pending", queueSize);
            }

            // Process packets
            int processed;
            if (batchMode) {
                processed = consumerService.consumeBatch();
                log.debug("Processed batch of {} packets ({} remaining in queue)",
                        processed, consumerService.getQueueSize());
            } else {
                processed = consumerService.consumeOne() ? 1 : 0;
                log.debug("Processed {} packet ({} remaining in queue)",
                        processed, consumerService.getQueueSize());
            }

            // Log statistics periodically
            if (logStats && processed > 0) {
                log.info("Packet processing stats: {}", consumerService.getStatistics());
            }

        } catch (BusinessException e) {
            // Circuit breaker or business error
            log.warn("Cannot process packets: {}", e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error in packet processing task", e);
        }
    }

    /**
     * Drain the queue on demand.
     * <p>
     * This method can be called manually (e.g., via REST API) to
     * aggressively process all pending packets. Useful for catching up
     * after a backlog.
     * </p>
     * <p>
     * The @Async annotation allows this to run in a separate thread
     * so it doesn't block the caller.
     * </p>
     */
    @Async("packetProcessorExecutor")
    public void drainQueue() {
        log.info("Starting queue drain operation");
        try {
            int processed = consumerService.drainQueue();
            log.info("Queue drain complete: {} packets processed", processed);
        } catch (Exception e) {
            log.error("Error during queue drain", e);
        }
    }

    /**
     * Get current queue size.
     *
     * @return number of packets in queue
     */
    public int getQueueSize() {
        return consumerService.getQueueSize();
    }

    /**
     * Get processing statistics.
     *
     * @return formatted statistics string
     */
    public String getStatistics() {
        return consumerService.getStatistics();
    }

    /**
     * Check if queue is above warning threshold.
     *
     * @return true if queue size exceeds threshold
     */
    public boolean isQueueAboveThreshold() {
        return consumerService.getQueueSize() > queueWarningThreshold;
    }
}
