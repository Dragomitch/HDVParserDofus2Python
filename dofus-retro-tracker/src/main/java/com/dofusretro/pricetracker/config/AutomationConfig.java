package com.dofusretro.pricetracker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for GUI automation.
 *
 * <p>Configure these properties in application.yml:
 * <pre>
 * automation:
 *   enabled: true
 *   templates-path: src/main/resources/templates
 *   match-threshold: 0.8
 *   action-delay-ms: 500
 *   screenshot-delay-ms: 200
 *   click-delay-ms: 100
 *   scroll-amount: 3
 *   max-retries: 3
 *   timeout-ms: 5000
 *   failsafe-enabled: true
 *   failsafe-corner: top-left
 * </pre>
 *
 * @since 0.1.0
 */
@Configuration
@ConfigurationProperties(prefix = "automation")
@Data
public class AutomationConfig {

    /**
     * Enable/disable automation system
     */
    private boolean enabled = false;

    /**
     * Path to template images directory
     */
    private String templatesPath = "src/main/resources/templates";

    /**
     * Template matching threshold (0.0 to 1.0)
     * Higher = more strict matching
     */
    private double matchThreshold = 0.8;

    /**
     * Delay between actions in milliseconds
     * Makes automation more human-like
     */
    private int actionDelayMs = 500;

    /**
     * Delay after screenshot before processing
     * Allows UI to stabilize
     */
    private int screenshotDelayMs = 200;

    /**
     * Delay between mouse press and release
     */
    private int clickDelayMs = 100;

    /**
     * Number of scroll wheel notches per scroll action
     */
    private int scrollAmount = 3;

    /**
     * Maximum retry attempts for failed actions
     */
    private int maxRetries = 3;

    /**
     * Default action timeout in milliseconds
     */
    private int timeoutMs = 5000;

    /**
     * Enable failsafe (emergency stop by moving mouse to corner)
     */
    private boolean failsafeEnabled = true;

    /**
     * Which corner triggers failsafe
     * Options: top-left, top-right, bottom-left, bottom-right
     */
    private String failsafeCorner = "top-left";

    /**
     * Failsafe trigger distance from corner (pixels)
     */
    private int failsafeDistance = 10;

    /**
     * Enable automation logging
     */
    private boolean loggingEnabled = true;

    /**
     * Save screenshots of failed actions for debugging
     */
    private boolean saveFailureScreenshots = true;

    /**
     * Directory to save failure screenshots
     */
    private String failureScreenshotsPath = "target/automation-failures";

    /**
     * UI regions configuration
     */
    private RegionsConfig regions = new RegionsConfig();

    /**
     * UI regions for different screen areas
     */
    @Data
    public static class RegionsConfig {
        /**
         * Left panel where categories are displayed
         */
        private Region categoryPanel = new Region(0, 100, 300, 600);

        /**
         * Main area where items are listed
         */
        private Region itemsPanel = new Region(300, 100, 700, 600);

        /**
         * Bottom area for pagination/controls
         */
        private Region controlsPanel = new Region(0, 700, 1000, 100);
    }

    /**
     * Screen region definition
     */
    @Data
    public static class Region {
        private int x;
        private int y;
        private int width;
        private int height;

        public Region() {}

        public Region(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public java.awt.Rectangle toRectangle() {
            return new java.awt.Rectangle(x, y, width, height);
        }
    }

    /**
     * Get match threshold as a value between 0 and 1
     *
     * @return Match threshold
     */
    public double getMatchThreshold() {
        return Math.max(0.0, Math.min(1.0, matchThreshold));
    }

    /**
     * Check if failsafe should trigger for given mouse position
     *
     * @param mouseX Mouse X coordinate
     * @param mouseY Mouse Y coordinate
     * @param screenWidth Screen width
     * @param screenHeight Screen height
     * @return true if mouse is in failsafe zone
     */
    public boolean isInFailsafeZone(int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (!failsafeEnabled) {
            return false;
        }

        return switch (failsafeCorner.toLowerCase()) {
            case "top-left" ->
                mouseX <= failsafeDistance && mouseY <= failsafeDistance;
            case "top-right" ->
                mouseX >= screenWidth - failsafeDistance && mouseY <= failsafeDistance;
            case "bottom-left" ->
                mouseX <= failsafeDistance && mouseY >= screenHeight - failsafeDistance;
            case "bottom-right" ->
                mouseX >= screenWidth - failsafeDistance && mouseY >= screenHeight - failsafeDistance;
            default -> false;
        };
    }
}
