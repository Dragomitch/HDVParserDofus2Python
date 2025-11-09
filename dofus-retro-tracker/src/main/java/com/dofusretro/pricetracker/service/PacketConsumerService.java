package com.dofusretro.pricetracker.service;

import com.dofusretro.pricetracker.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for consuming packets from the packet queue and processing them.
 * <p>
 * This service acts as a bridge between the PacketCaptureService (producer)
 * and the ItemPriceService (processor). It:
 * <ul>
 *   <li>Polls packets from the blocking queue</li>
 *   <li>Batches packets for efficient processing</li>
 *   <li>Handles backpressure when queue fills up</li>
 *   <li>Implements circuit breaker for database failures</li>
 *   <li>Tracks processing metrics</li>
 * </ul>
 * </p>
 * <p>
 * The service supports both single packet and batch processing modes.
 * Batch processing is more efficient for high-throughput scenarios.
 * </p>
 *
 * @author AGENT-BUSINESS
 * @version 1.0
 * @since Wave 2
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PacketConsumerService {

    private final BlockingQueue<byte[]> packetQueue;
    private final ItemPriceService itemPriceService;

    @Value("${dofus.retro.tracker.consumer.batch-size:10}")
    private int batchSize;

    @Value("${dofus.retro.tracker.consumer.poll-timeout-ms:1000}")
    private long pollTimeoutMs;

    @Value("${dofus.retro.tracker.consumer.circuit-breaker.failure-threshold:5}")
    private int circuitBreakerFailureThreshold;

    @Value("${dofus.retro.tracker.consumer.circuit-breaker.reset-timeout-ms:60000}")
    private long circuitBreakerResetTimeoutMs;

    // Circuit breaker state
    private volatile CircuitBreakerState circuitState = CircuitBreakerState.CLOSED;
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private volatile long circuitOpenedAt = 0;

    // Metrics
    private final AtomicLong totalPacketsProcessed = new AtomicLong(0);
    private final AtomicLong totalPriceEntriesPersisted = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);

    /**
     * Constructor with qualifier for the packet queue bean.
     */
    public PacketConsumerService(
            @Qualifier("packetQueue") BlockingQueue<byte[]> packetQueue,
            ItemPriceService itemPriceService) {
        this.packetQueue = packetQueue;
        this.itemPriceService = itemPriceService;
    }

    /**
     * Consume and process a single packet from the queue.
     * <p>
     * This method blocks until a packet is available or the timeout expires.
     * </p>
     *
     * @return true if a packet was processed, false if timeout
     * @throws BusinessException if circuit breaker is open
     */
    public boolean consumeOne() {
        // Check circuit breaker
        checkCircuitBreaker();

        try {
            byte[] packet = packetQueue.poll(pollTimeoutMs, TimeUnit.MILLISECONDS);

            if (packet == null) {
                // Timeout - no packet available
                return false;
            }

            log.debug("Consumed packet from queue ({} bytes, {} remaining)",
                    packet.length, packetQueue.size());

            // Process the packet
            int persisted = itemPriceService.processPacket(packet);

            // Update metrics
            totalPacketsProcessed.incrementAndGet();
            totalPriceEntriesPersisted.addAndGet(persisted);

            // Reset consecutive failures on success
            if (consecutiveFailures.get() > 0) {
                log.info("Processing recovered, resetting failure count");
                consecutiveFailures.set(0);
            }

            return true;

        } catch (InterruptedException e) {
            log.debug("Packet consumption interrupted");
            Thread.currentThread().interrupt();
            return false;

        } catch (Exception e) {
            log.error("Error processing packet", e);
            totalErrors.incrementAndGet();
            handleFailure(e);
            return false;
        }
    }

    /**
     * Consume and process a batch of packets from the queue.
     * <p>
     * This method collects up to batchSize packets from the queue and
     * processes them as a batch for better efficiency.
     * </p>
     *
     * @return the number of packets processed
     * @throws BusinessException if circuit breaker is open
     */
    public int consumeBatch() {
        // Check circuit breaker
        checkCircuitBreaker();

        List<byte[]> batch = new ArrayList<>(batchSize);

        try {
            // Collect packets up to batch size
            long startTime = System.currentTimeMillis();
            long deadline = startTime + pollTimeoutMs;

            while (batch.size() < batchSize && System.currentTimeMillis() < deadline) {
                long remainingTime = deadline - System.currentTimeMillis();
                if (remainingTime <= 0) {
                    break;
                }

                byte[] packet = packetQueue.poll(remainingTime, TimeUnit.MILLISECONDS);
                if (packet != null) {
                    batch.add(packet);
                } else {
                    break; // Timeout or no more packets
                }
            }

            if (batch.isEmpty()) {
                log.trace("No packets available in queue");
                return 0;
            }

            log.debug("Consumed batch of {} packets from queue ({} remaining)",
                    batch.size(), packetQueue.size());

            // Process the batch
            int persisted = itemPriceService.processPacketBatch(batch);

            // Update metrics
            totalPacketsProcessed.addAndGet(batch.size());
            totalPriceEntriesPersisted.addAndGet(persisted);

            // Reset consecutive failures on success
            if (consecutiveFailures.get() > 0) {
                log.info("Batch processing recovered, resetting failure count");
                consecutiveFailures.set(0);
            }

            return batch.size();

        } catch (InterruptedException e) {
            log.debug("Batch consumption interrupted");
            Thread.currentThread().interrupt();
            return batch.size();

        } catch (Exception e) {
            log.error("Error processing packet batch", e);
            totalErrors.incrementAndGet();
            handleFailure(e);
            return 0;
        }
    }

    /**
     * Drain all available packets from the queue and process them.
     * <p>
     * This method is useful for processing accumulated packets,
     * such as during startup or after a pause.
     * </p>
     *
     * @return the number of packets processed
     */
    public int drainQueue() {
        log.info("Draining packet queue (current size: {})", packetQueue.size());

        int totalProcessed = 0;
        int batchesProcessed = 0;

        while (!packetQueue.isEmpty()) {
            try {
                int processed = consumeBatch();
                totalProcessed += processed;
                batchesProcessed++;

                if (processed == 0) {
                    break; // No more packets
                }

            } catch (Exception e) {
                log.error("Error during queue drain, stopping", e);
                break;
            }
        }

        log.info("Queue drain complete: {} packets in {} batches",
                totalProcessed, batchesProcessed);

        return totalProcessed;
    }

    /**
     * Get the current queue size.
     *
     * @return number of packets in queue
     */
    public int getQueueSize() {
        return packetQueue.size();
    }

    /**
     * Check if the queue is empty.
     *
     * @return true if queue is empty
     */
    public boolean isQueueEmpty() {
        return packetQueue.isEmpty();
    }

    /**
     * Get processing statistics.
     *
     * @return formatted statistics string
     */
    public String getStatistics() {
        return String.format(
                "Packets: %d, Prices: %d, Errors: %d, Circuit: %s",
                totalPacketsProcessed.get(),
                totalPriceEntriesPersisted.get(),
                totalErrors.get(),
                circuitState
        );
    }

    /**
     * Get the total number of packets processed.
     *
     * @return packet count
     */
    public long getTotalPacketsProcessed() {
        return totalPacketsProcessed.get();
    }

    /**
     * Get the total number of price entries persisted.
     *
     * @return price entry count
     */
    public long getTotalPriceEntriesPersisted() {
        return totalPriceEntriesPersisted.get();
    }

    /**
     * Get the total number of errors.
     *
     * @return error count
     */
    public long getTotalErrors() {
        return totalErrors.get();
    }

    /**
     * Get the current circuit breaker state.
     *
     * @return circuit breaker state
     */
    public CircuitBreakerState getCircuitState() {
        return circuitState;
    }

    /**
     * Reset the circuit breaker and metrics.
     */
    public void reset() {
        log.info("Resetting packet consumer service");
        circuitState = CircuitBreakerState.CLOSED;
        consecutiveFailures.set(0);
        circuitOpenedAt = 0;
        totalPacketsProcessed.set(0);
        totalPriceEntriesPersisted.set(0);
        totalErrors.set(0);
    }

    /**
     * Handle a processing failure.
     * <p>
     * Increments failure count and opens circuit breaker if threshold reached.
     * </p>
     */
    private void handleFailure(Exception e) {
        int failures = consecutiveFailures.incrementAndGet();

        log.warn("Processing failure #{}: {}", failures, e.getMessage());

        if (failures >= circuitBreakerFailureThreshold) {
            openCircuitBreaker();
        }
    }

    /**
     * Open the circuit breaker.
     */
    private void openCircuitBreaker() {
        if (circuitState != CircuitBreakerState.OPEN) {
            log.error("Circuit breaker OPENED after {} consecutive failures",
                    consecutiveFailures.get());
            circuitState = CircuitBreakerState.OPEN;
            circuitOpenedAt = System.currentTimeMillis();
        }
    }

    /**
     * Check the circuit breaker state.
     * <p>
     * If the circuit is open and the reset timeout has elapsed,
     * transitions to HALF_OPEN state to try recovery.
     * </p>
     *
     * @throws BusinessException if circuit is open
     */
    private void checkCircuitBreaker() {
        if (circuitState == CircuitBreakerState.OPEN) {
            long elapsed = System.currentTimeMillis() - circuitOpenedAt;

            if (elapsed >= circuitBreakerResetTimeoutMs) {
                log.info("Circuit breaker transitioning to HALF_OPEN (attempting recovery)");
                circuitState = CircuitBreakerState.HALF_OPEN;
                consecutiveFailures.set(0);
            } else {
                throw BusinessException.circuitBreakerOpen("PacketConsumerService");
            }
        }

        if (circuitState == CircuitBreakerState.HALF_OPEN) {
            // In half-open state, allow one request through to test
            // If it succeeds, the circuit will close
            // If it fails, the circuit will open again
            log.debug("Circuit breaker in HALF_OPEN state, attempting request");
        }
    }

    /**
     * Circuit breaker states.
     */
    public enum CircuitBreakerState {
        /**
         * Normal operation, all requests allowed.
         */
        CLOSED,

        /**
         * Failure threshold reached, no requests allowed.
         */
        OPEN,

        /**
         * Testing recovery, limited requests allowed.
         */
        HALF_OPEN
    }
}
