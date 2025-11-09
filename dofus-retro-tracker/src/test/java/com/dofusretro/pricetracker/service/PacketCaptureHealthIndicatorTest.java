package com.dofusretro.pricetracker.service;

import com.dofusretro.pricetracker.config.PacketCaptureConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PacketCaptureHealthIndicator.
 *
 * @author AGENT-NETWORK
 * @since 0.1.0
 */
@ExtendWith(MockitoExtension.class)
class PacketCaptureHealthIndicatorTest {

    @Mock
    private PacketCaptureService captureService;

    @Mock
    private PacketCaptureConfig config;

    private PacketCaptureHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new PacketCaptureHealthIndicator(captureService, config);
    }

    @Test
    void shouldReportUpWhenDisabled() {
        // Given
        when(config.isEnabled()).thenReturn(false);

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("status", "disabled");
    }

    @Test
    void shouldReportDownWhenEnabledButNotRunning() {
        // Given
        when(config.isEnabled()).thenReturn(true);
        when(captureService.isRunning()).thenReturn(false);

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("status", "stopped");
    }

    @Test
    void shouldReportUpWhenRunningWithLowQueueUtilization() {
        // Given
        when(config.isEnabled()).thenReturn(true);
        when(captureService.isRunning()).thenReturn(true);
        when(captureService.getQueueSize()).thenReturn(10);
        when(config.getQueueCapacity()).thenReturn(1000);
        when(config.getDofusPort()).thenReturn(5555);

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("status", "capturing");
        assertThat(health.getDetails()).containsKey("queueSize");
        assertThat(health.getDetails()).containsKey("queueUtilization");
    }

    @Test
    void shouldReportWarningWhenQueueUtilizationHigh() {
        // Given - 85% utilization
        when(config.isEnabled()).thenReturn(true);
        when(captureService.isRunning()).thenReturn(true);
        when(captureService.getQueueSize()).thenReturn(850);
        when(config.getQueueCapacity()).thenReturn(1000);
        when(config.getDofusPort()).thenReturn(5555);

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(new Status("WARNING"));
        assertThat(health.getDetails()).containsKey("warning");
    }

    @Test
    void shouldReportDownWhenQueueCriticallyFull() {
        // Given - 96% utilization
        when(config.isEnabled()).thenReturn(true);
        when(captureService.isRunning()).thenReturn(true);
        when(captureService.getQueueSize()).thenReturn(960);
        when(config.getQueueCapacity()).thenReturn(1000);
        when(config.getDofusPort()).thenReturn(5555);

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("issue", "Queue is critically full (>95%)");
    }

    @Test
    void shouldIncludeNetworkInterfaceWhenConfigured() {
        // Given
        when(config.isEnabled()).thenReturn(true);
        when(captureService.isRunning()).thenReturn(true);
        when(captureService.getQueueSize()).thenReturn(10);
        when(config.getQueueCapacity()).thenReturn(1000);
        when(config.getDofusPort()).thenReturn(5555);
        when(config.getNetworkInterface()).thenReturn("eth0");

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getDetails()).containsEntry("networkInterface", "eth0");
    }

    @Test
    void shouldIndicateAutoDetectWhenInterfaceNotConfigured() {
        // Given
        when(config.isEnabled()).thenReturn(true);
        when(captureService.isRunning()).thenReturn(true);
        when(captureService.getQueueSize()).thenReturn(10);
        when(config.getQueueCapacity()).thenReturn(1000);
        when(config.getDofusPort()).thenReturn(5555);
        when(config.getNetworkInterface()).thenReturn(null);

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getDetails()).containsEntry("networkInterface", "auto-detected");
    }
}
