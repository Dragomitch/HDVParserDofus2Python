package com.dofusretro.pricetracker.automation;

import com.dofusretro.pricetracker.config.AutomationConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.*;

/**
 * Factory for creating and configuring Robot instances.
 *
 * <p>This factory centralizes Robot creation and configuration,
 * applying consistent settings across all automation operations.
 *
 * <p>Features:
 * <ul>
 *   <li>Auto-delay configuration for human-like interaction</li>
 *   <li>Auto waiting for idle after operations</li>
 *   <li>Error handling for Robot creation failures</li>
 *   <li>Platform-specific optimizations</li>
 * </ul>
 *
 * @since 0.1.0
 */
@Component
@Slf4j
public class RobotFactory {

    private final AutomationConfig config;

    public RobotFactory(AutomationConfig config) {
        this.config = config;
    }

    /**
     * Create a new Robot instance with configured settings.
     *
     * @return Configured Robot instance
     * @throws AWTException if Robot cannot be created
     */
    public Robot createRobot() throws AWTException {
        try {
            Robot robot = new Robot();

            // Configure auto delay (delay between Robot operations)
            robot.setAutoDelay(config.getActionDelayMs());

            // Auto wait for idle
            robot.setAutoWaitForIdle(true);

            log.debug("Created Robot with auto-delay: {}ms", config.getActionDelayMs());

            return robot;
        } catch (AWTException e) {
            log.error("Failed to create Robot instance", e);
            throw e;
        }
    }

    /**
     * Create a Robot instance for testing (no delays).
     *
     * @return Robot configured for testing
     * @throws AWTException if Robot cannot be created
     */
    public Robot createTestRobot() throws AWTException {
        Robot robot = new Robot();
        robot.setAutoDelay(0);
        robot.setAutoWaitForIdle(false);
        log.debug("Created test Robot (no delays)");
        return robot;
    }

    /**
     * Check if Robot is supported on this platform.
     *
     * <p>Robot may not work on:
     * <ul>
     *   <li>Headless systems (no display)</li>
     *   <li>Linux with Wayland (security restrictions)</li>
     *   <li>macOS without Accessibility permissions</li>
     * </ul>
     *
     * @return true if Robot is supported
     */
    public boolean isRobotSupported() {
        // Check for headless mode
        if (GraphicsEnvironment.isHeadless()) {
            log.warn("Robot not supported: headless environment");
            return false;
        }

        // Try to create a Robot
        try {
            Robot testRobot = new Robot();
            testRobot.setAutoDelay(0);

            // Try a simple operation
            Point mousePos = MouseInfo.getPointerInfo().getLocation();
            log.debug("Robot test successful, mouse at: {}", mousePos);

            return true;
        } catch (AWTException e) {
            log.error("Robot not supported on this platform", e);
            return false;
        } catch (SecurityException e) {
            log.error("Robot blocked by security manager", e);
            return false;
        }
    }

    /**
     * Get platform information for debugging.
     *
     * @return Platform info string
     */
    public String getPlatformInfo() {
        return String.format(
            "OS: %s, Java: %s, Headless: %s, Screens: %d",
            System.getProperty("os.name"),
            System.getProperty("java.version"),
            GraphicsEnvironment.isHeadless(),
            GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length
        );
    }
}
