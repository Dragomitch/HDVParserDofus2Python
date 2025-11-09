package com.dofusretro.pricetracker.service;

import com.dofusretro.pricetracker.config.PacketCaptureConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.BlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for PacketCaptureService.
 *
 * These tests verify that the packet capture service integrates
 * correctly with the Spring application context. Actual packet
 * capture is disabled to avoid requiring special permissions.
 *
 * @author AGENT-NETWORK
 * @since 0.1.0
 */
@SpringBootTest
@TestPropertySource(properties = {
        "packet.capture.enabled=false", // Disable live capture in tests
        "spring.flyway.enabled=false",  // Disable Flyway for tests
        "spring.jpa.hibernate.ddl-auto=none" // Disable JPA auto-ddl
})
class PacketCaptureIntegrationTest {

    @Autowired
    private PacketCaptureService service;

    @Autowired
    private PacketCaptureConfig config;

    @Autowired
    @Qualifier("packetQueue")
    private BlockingQueue<byte[]> queue;

    @Test
    void contextLoads() {
        // Verify that the Spring context loads successfully with
        // all packet capture beans configured
        assertThat(service).isNotNull();
        assertThat(config).isNotNull();
        assertThat(queue).isNotNull();
    }

    @Test
    void shouldConfigurePacketCaptureService() {
        // Verify service is properly wired
        assertThat(service).isNotNull();
    }

    @Test
    void shouldConfigurePacketQueue() {
        // Verify queue is properly configured
        assertThat(queue).isNotNull();
        assertThat(queue).hasSize(0);
        assertThat(queue.remainingCapacity()).isGreaterThan(0);
    }

    @Test
    void shouldLoadConfiguration() {
        // Verify configuration is loaded correctly
        assertThat(config).isNotNull();
        assertThat(config.isEnabled()).isFalse(); // Disabled in test properties
        assertThat(config.getDofusPort()).isEqualTo(5555);
        assertThat(config.getSnapLen()).isGreaterThan(0);
        assertThat(config.getTimeout()).isGreaterThan(0);
        assertThat(config.getQueueCapacity()).isGreaterThan(0);
    }

    @Test
    void shouldNotStartWhenDisabled() {
        // Service should not be running because capture is disabled in test properties
        assertThat(service.isRunning()).isFalse();
    }

    @Test
    void shouldReportCorrectQueueSize() {
        // Add some test data to the queue
        byte[] testData1 = "test1".getBytes();
        byte[] testData2 = "test2".getBytes();

        queue.offer(testData1);
        queue.offer(testData2);

        // Verify service reports correct queue size
        assertThat(service.getQueueSize()).isEqualTo(2);

        // Clean up
        queue.clear();
    }
}
