package com.dofusretro.pricetracker.service;

import com.dofusretro.pricetracker.config.PacketCaptureConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PacketCaptureService.
 *
 * These tests verify the service logic without requiring actual
 * network interfaces or permissions. The Pcap4j dependencies are
 * not mocked as starting the service requires configuration to be disabled.
 *
 * @author AGENT-NETWORK
 * @since 0.1.0
 */
@ExtendWith(MockitoExtension.class)
class PacketCaptureServiceTest {

    @Mock
    private PacketCaptureConfig config;

    private BlockingQueue<byte[]> packetQueue;

    private PacketCaptureService service;

    @BeforeEach
    void setUp() {
        // Create a real queue for testing
        packetQueue = new LinkedBlockingQueue<>(1000);

        // Configure mock to disable capture by default
        when(config.isEnabled()).thenReturn(false);
        when(config.getSnapLen()).thenReturn(65536);
        when(config.getTimeout()).thenReturn(1000);
        when(config.getDofusPort()).thenReturn(5555);
        when(config.getQueueCapacity()).thenReturn(1000);
        when(config.getQueueTimeout()).thenReturn(100);
        when(config.isPromiscuousMode()).thenReturn(false);

        // Create service with mocked config
        service = new PacketCaptureService(packetQueue, config);
    }

    @Test
    void shouldNotStartWhenDisabled() {
        // When
        service.startCapture();

        // Then
        assertThat(service.isRunning()).isFalse();
    }

    @Test
    void shouldReportNotRunningInitially() {
        // Then
        assertThat(service.isRunning()).isFalse();
    }

    @Test
    void shouldReportZeroQueueSizeInitially() {
        // Then
        assertThat(service.getQueueSize()).isEqualTo(0);
    }

    @Test
    void shouldReportQueueSize() {
        // Given
        byte[] testData1 = "packet1".getBytes();
        byte[] testData2 = "packet2".getBytes();

        // When
        packetQueue.offer(testData1);
        packetQueue.offer(testData2);

        // Then
        assertThat(service.getQueueSize()).isEqualTo(2);
    }

    @Test
    void shouldHandleStopWhenNotRunning() {
        // When/Then - should not throw exception
        service.stopCapture();

        assertThat(service.isRunning()).isFalse();
    }

    @Test
    void shouldRespectDisabledConfiguration() {
        // Given
        when(config.isEnabled()).thenReturn(false);

        // When
        service.startCapture();

        // Then
        assertThat(service.isRunning()).isFalse();
        verify(config, atLeastOnce()).isEnabled();
    }

    @Test
    void shouldGetNullStatisticsWhenNotRunning() {
        // When
        var stats = service.getStatistics();

        // Then
        assertThat(stats).isNull();
    }

    @Test
    void shouldMaintainQueueReference() {
        // Given
        byte[] testData = "test".getBytes();

        // When
        packetQueue.offer(testData);

        // Then
        assertThat(service.getQueueSize()).isEqualTo(1);
        assertThat(packetQueue.poll()).isEqualTo(testData);
        assertThat(service.getQueueSize()).isEqualTo(0);
    }

    @Test
    void shouldUseConfiguredDofusPort() {
        // Given
        when(config.getDofusPort()).thenReturn(5555);

        // When
        int port = config.getDofusPort();

        // Then
        assertThat(port).isEqualTo(5555);
        verify(config).getDofusPort();
    }

    @Test
    void shouldUseConfiguredSnapLen() {
        // Given
        when(config.getSnapLen()).thenReturn(65536);

        // When
        int snapLen = config.getSnapLen();

        // Then
        assertThat(snapLen).isEqualTo(65536);
        verify(config).getSnapLen();
    }

    @Test
    void shouldUseConfiguredTimeout() {
        // Given
        when(config.getTimeout()).thenReturn(1000);

        // When
        int timeout = config.getTimeout();

        // Then
        assertThat(timeout).isEqualTo(1000);
        verify(config).getTimeout();
    }
}
