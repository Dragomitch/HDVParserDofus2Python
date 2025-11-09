package com.dofusretro.pricetracker.service;

import com.dofusretro.pricetracker.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PacketConsumerService.
 *
 * @author AGENT-BUSINESS
 * @version 1.0
 * @since Wave 2
 */
@ExtendWith(MockitoExtension.class)
class PacketConsumerServiceTest {

    @Mock
    private ItemPriceService itemPriceService;

    private BlockingQueue<byte[]> packetQueue;

    private PacketConsumerService service;

    private byte[] testPacket;

    @BeforeEach
    void setUp() {
        packetQueue = new LinkedBlockingQueue<>(100);
        service = new PacketConsumerService(packetQueue, itemPriceService);

        // Set configuration values via reflection
        ReflectionTestUtils.setField(service, "batchSize", 10);
        ReflectionTestUtils.setField(service, "pollTimeoutMs", 100L);
        ReflectionTestUtils.setField(service, "circuitBreakerFailureThreshold", 5);
        ReflectionTestUtils.setField(service, "circuitBreakerResetTimeoutMs", 1000L);

        testPacket = new byte[]{0x01, 0x02, 0x03};
    }

    @Test
    void testConsumeOne_Success() throws Exception {
        // Arrange
        packetQueue.offer(testPacket);
        when(itemPriceService.processPacket(testPacket)).thenReturn(3);

        // Act
        boolean result = service.consumeOne();

        // Assert
        assertThat(result).isTrue();
        assertThat(packetQueue).isEmpty();
        verify(itemPriceService).processPacket(testPacket);
        assertThat(service.getTotalPacketsProcessed()).isEqualTo(1);
        assertThat(service.getTotalPriceEntriesPersisted()).isEqualTo(3);
    }

    @Test
    void testConsumeOne_EmptyQueue() {
        // Act
        boolean result = service.consumeOne();

        // Assert
        assertThat(result).isFalse();
        verifyNoInteractions(itemPriceService);
    }

    @Test
    void testConsumeOne_ProcessingError() throws Exception {
        // Arrange
        packetQueue.offer(testPacket);
        when(itemPriceService.processPacket(testPacket))
                .thenThrow(new RuntimeException("Processing error"));

        // Act
        boolean result = service.consumeOne();

        // Assert
        assertThat(result).isFalse();
        assertThat(service.getTotalErrors()).isEqualTo(1);
    }

    @Test
    void testConsumeBatch_Success() throws Exception {
        // Arrange
        for (int i = 0; i < 5; i++) {
            packetQueue.offer(testPacket);
        }
        when(itemPriceService.processPacketBatch(anyList())).thenReturn(15);

        // Act
        int processed = service.consumeBatch();

        // Assert
        assertThat(processed).isEqualTo(5);
        assertThat(packetQueue).isEmpty();
        verify(itemPriceService).processPacketBatch(argThat(list -> list.size() == 5));
        assertThat(service.getTotalPacketsProcessed()).isEqualTo(5);
        assertThat(service.getTotalPriceEntriesPersisted()).isEqualTo(15);
    }

    @Test
    void testConsumeBatch_EmptyQueue() {
        // Act
        int processed = service.consumeBatch();

        // Assert
        assertThat(processed).isZero();
        verifyNoInteractions(itemPriceService);
    }

    @Test
    void testConsumeBatch_PartialFill() throws Exception {
        // Arrange - Add fewer packets than batch size
        ReflectionTestUtils.setField(service, "batchSize", 10);
        for (int i = 0; i < 3; i++) {
            packetQueue.offer(testPacket);
        }
        when(itemPriceService.processPacketBatch(anyList())).thenReturn(9);

        // Act
        int processed = service.consumeBatch();

        // Assert
        assertThat(processed).isEqualTo(3);
        verify(itemPriceService).processPacketBatch(argThat(list -> list.size() == 3));
    }

    @Test
    void testConsumeBatch_ProcessingError() throws Exception {
        // Arrange
        packetQueue.offer(testPacket);
        when(itemPriceService.processPacketBatch(anyList()))
                .thenThrow(new RuntimeException("Batch processing error"));

        // Act
        int processed = service.consumeBatch();

        // Assert
        assertThat(processed).isZero();
        assertThat(service.getTotalErrors()).isEqualTo(1);
    }

    @Test
    void testDrainQueue_Success() throws Exception {
        // Arrange
        for (int i = 0; i < 25; i++) {
            packetQueue.offer(testPacket);
        }
        when(itemPriceService.processPacketBatch(anyList())).thenReturn(3);

        // Act
        int drained = service.drainQueue();

        // Assert
        assertThat(drained).isEqualTo(25);
        assertThat(packetQueue).isEmpty();
    }

    @Test
    void testDrainQueue_EmptyQueue() {
        // Act
        int drained = service.drainQueue();

        // Assert
        assertThat(drained).isZero();
    }

    @Test
    void testCircuitBreaker_OpensAfterFailures() throws Exception {
        // Arrange - Set low failure threshold
        ReflectionTestUtils.setField(service, "circuitBreakerFailureThreshold", 3);

        packetQueue.offer(testPacket);
        packetQueue.offer(testPacket);
        packetQueue.offer(testPacket);

        when(itemPriceService.processPacket(any()))
                .thenThrow(new RuntimeException("Error"));

        // Act - Trigger failures
        service.consumeOne();
        service.consumeOne();
        service.consumeOne();

        // Assert - Circuit should be open
        assertThat(service.getCircuitState())
                .isEqualTo(PacketConsumerService.CircuitBreakerState.OPEN);

        // Try to consume with open circuit
        assertThatThrownBy(() -> service.consumeOne())
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("circuit breaker open");
    }

    @Test
    void testCircuitBreaker_RecoveryToHalfOpen() throws Exception {
        // Arrange - Set low threshold and short timeout
        ReflectionTestUtils.setField(service, "circuitBreakerFailureThreshold", 2);
        ReflectionTestUtils.setField(service, "circuitBreakerResetTimeoutMs", 100L);

        packetQueue.offer(testPacket);
        packetQueue.offer(testPacket);

        when(itemPriceService.processPacket(any()))
                .thenThrow(new RuntimeException("Error"));

        // Act - Trigger failures to open circuit
        service.consumeOne();
        service.consumeOne();

        assertThat(service.getCircuitState())
                .isEqualTo(PacketConsumerService.CircuitBreakerState.OPEN);

        // Wait for reset timeout
        Thread.sleep(150);

        // Try to consume again - should transition to HALF_OPEN
        packetQueue.offer(testPacket);
        when(itemPriceService.processPacket(any())).thenReturn(1);

        service.consumeOne();

        // Circuit should now allow requests (HALF_OPEN)
        assertThat(service.getCircuitState())
                .isIn(PacketConsumerService.CircuitBreakerState.HALF_OPEN,
                        PacketConsumerService.CircuitBreakerState.CLOSED);
    }

    @Test
    void testGetQueueSize() throws Exception {
        // Arrange
        for (int i = 0; i < 5; i++) {
            packetQueue.offer(testPacket);
        }

        // Act & Assert
        assertThat(service.getQueueSize()).isEqualTo(5);
    }

    @Test
    void testIsQueueEmpty() {
        // Act & Assert
        assertThat(service.isQueueEmpty()).isTrue();

        packetQueue.offer(testPacket);
        assertThat(service.isQueueEmpty()).isFalse();
    }

    @Test
    void testGetStatistics() throws Exception {
        // Arrange
        packetQueue.offer(testPacket);
        when(itemPriceService.processPacket(any())).thenReturn(3);

        // Act
        service.consumeOne();
        String stats = service.getStatistics();

        // Assert
        assertThat(stats).contains("Packets: 1");
        assertThat(stats).contains("Prices: 3");
        assertThat(stats).contains("Errors: 0");
        assertThat(stats).contains("Circuit: CLOSED");
    }

    @Test
    void testReset() throws Exception {
        // Arrange
        packetQueue.offer(testPacket);
        when(itemPriceService.processPacket(any())).thenReturn(3);
        service.consumeOne();

        // Act
        service.reset();

        // Assert
        assertThat(service.getTotalPacketsProcessed()).isZero();
        assertThat(service.getTotalPriceEntriesPersisted()).isZero();
        assertThat(service.getTotalErrors()).isZero();
        assertThat(service.getCircuitState())
                .isEqualTo(PacketConsumerService.CircuitBreakerState.CLOSED);
    }

    @Test
    void testConsumeBatch_FullBatch() throws Exception {
        // Arrange - Fill queue beyond batch size
        ReflectionTestUtils.setField(service, "batchSize", 5);
        for (int i = 0; i < 10; i++) {
            packetQueue.offer(testPacket);
        }
        when(itemPriceService.processPacketBatch(anyList())).thenReturn(15);

        // Act
        int processed = service.consumeBatch();

        // Assert - Should only process batch size
        assertThat(processed).isEqualTo(5);
        assertThat(packetQueue.size()).isEqualTo(5);  // 5 remain
        verify(itemPriceService).processPacketBatch(argThat(list -> list.size() == 5));
    }

    @Test
    void testConsumeBatch_Timeout() throws Exception {
        // Arrange - Set very short timeout
        ReflectionTestUtils.setField(service, "pollTimeoutMs", 10L);
        ReflectionTestUtils.setField(service, "batchSize", 10);

        // Add one packet
        packetQueue.offer(testPacket);

        when(itemPriceService.processPacketBatch(anyList())).thenReturn(3);

        // Act - Should timeout waiting for more packets
        int processed = service.consumeBatch();

        // Assert - Should process the one packet available
        assertThat(processed).isEqualTo(1);
    }

    @Test
    void testConsecutiveSuccessResetsFailureCount() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(service, "circuitBreakerFailureThreshold", 5);

        // Trigger some failures
        packetQueue.offer(testPacket);
        when(itemPriceService.processPacket(any())).thenThrow(new RuntimeException("Error"));
        service.consumeOne();
        service.consumeOne();

        assertThat(service.getTotalErrors()).isEqualTo(2);

        // Now succeed
        packetQueue.offer(testPacket);
        when(itemPriceService.processPacket(any())).thenReturn(3);
        service.consumeOne();

        // More failures - should need 5 more to open circuit
        when(itemPriceService.processPacket(any())).thenThrow(new RuntimeException("Error"));
        for (int i = 0; i < 4; i++) {
            packetQueue.offer(testPacket);
            service.consumeOne();
        }

        // Circuit should still be closed (only 4 consecutive failures)
        assertThat(service.getCircuitState())
                .isEqualTo(PacketConsumerService.CircuitBreakerState.CLOSED);
    }
}
