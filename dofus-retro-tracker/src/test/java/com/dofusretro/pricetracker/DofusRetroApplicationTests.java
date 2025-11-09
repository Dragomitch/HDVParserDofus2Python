package com.dofusretro.pricetracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic integration test to verify Spring Boot application context loads correctly.
 *
 * @author AGENT-INFRA
 * @since 0.1.0
 */
@SpringBootTest
@ActiveProfiles("test")
class DofusRetroApplicationTests {

    @Test
    void contextLoads() {
        // This test will fail if the application context cannot start
        // It's a basic smoke test to ensure all configurations are valid
    }

    @Test
    void mainMethodRuns() {
        // Test that the main method doesn't throw exceptions
        // Actual startup is tested by contextLoads()
        DofusRetroApplication.main(new String[]{});
    }
}
