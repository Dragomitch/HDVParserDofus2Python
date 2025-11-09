package com.dofusretro.pricetracker.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for PacketCaptureConfig.
 *
 * Verifies that configuration properties are properly loaded
 * from YAML files and bound to the configuration class.
 *
 * @author AGENT-NETWORK
 * @since 0.1.0
 */
@SpringBootTest
@TestPropertySource(properties = {
        "packet.capture.enabled=true",
        "packet.capture.dofus-port=5555",
        "packet.capture.snap-len=32768",
        "packet.capture.timeout=2000",
        "packet.capture.queue-capacity=500",
        "packet.capture.queue-timeout=200",
        "packet.capture.promiscuous-mode=true",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none"
})
class PacketCaptureConfigTest {

    @Autowired
    private PacketCaptureConfig config;

    @Test
    void shouldLoadConfiguration() {
        assertThat(config).isNotNull();
    }

    @Test
    void shouldLoadEnabledProperty() {
        assertThat(config.isEnabled()).isTrue();
    }

    @Test
    void shouldLoadDofusPort() {
        assertThat(config.getDofusPort()).isEqualTo(5555);
    }

    @Test
    void shouldLoadSnapLen() {
        assertThat(config.getSnapLen()).isEqualTo(32768);
    }

    @Test
    void shouldLoadTimeout() {
        assertThat(config.getTimeout()).isEqualTo(2000);
    }

    @Test
    void shouldLoadQueueCapacity() {
        assertThat(config.getQueueCapacity()).isEqualTo(500);
    }

    @Test
    void shouldLoadQueueTimeout() {
        assertThat(config.getQueueTimeout()).isEqualTo(200);
    }

    @Test
    void shouldLoadPromiscuousMode() {
        assertThat(config.isPromiscuousMode()).isTrue();
    }
}
