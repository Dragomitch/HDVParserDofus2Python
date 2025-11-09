package com.dofusretro.pricetracker.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Application-wide configuration properties.
 *
 * Binds to 'dofus.retro.tracker' in application.yml files.
 * All properties are validated on startup.
 *
 * @author AGENT-INFRA
 * @since Wave 2
 */
@ConfigurationProperties(prefix = "dofus.retro.tracker")
@Validated
public class AppProperties {

    @NotBlank(message = "Application version must not be blank")
    private String version = "0.1.0";

    private PacketCaptureProperties packetCapture = new PacketCaptureProperties();
    private GuiAutomationProperties guiAutomation = new GuiAutomationProperties();
    private CacheProperties cache = new CacheProperties();

    // Getters and Setters
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public PacketCaptureProperties getPacketCapture() {
        return packetCapture;
    }

    public void setPacketCapture(PacketCaptureProperties packetCapture) {
        this.packetCapture = packetCapture;
    }

    public GuiAutomationProperties getGuiAutomation() {
        return guiAutomation;
    }

    public void setGuiAutomation(GuiAutomationProperties guiAutomation) {
        this.guiAutomation = guiAutomation;
    }

    public CacheProperties getCache() {
        return cache;
    }

    public void setCache(CacheProperties cache) {
        this.cache = cache;
    }

    /**
     * Packet capture configuration properties.
     */
    public static class PacketCaptureProperties {
        private boolean enabled = true;

        @NotBlank(message = "Network interface must not be blank")
        private String networkInterface = "eth0";

        @NotBlank(message = "Capture filter must not be blank")
        private String filter = "tcp port 5555";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getNetworkInterface() {
            return networkInterface;
        }

        public void setNetworkInterface(String networkInterface) {
            this.networkInterface = networkInterface;
        }

        public String getFilter() {
            return filter;
        }

        public void setFilter(String filter) {
            this.filter = filter;
        }
    }

    /**
     * GUI automation configuration properties.
     */
    public static class GuiAutomationProperties {
        private boolean enabled = false;
        private boolean debugMode = false;

        @Min(value = 1000, message = "Screenshot interval must be at least 1000ms")
        @Max(value = 60000, message = "Screenshot interval must not exceed 60000ms")
        private int screenshotInterval = 5000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isDebugMode() {
            return debugMode;
        }

        public void setDebugMode(boolean debugMode) {
            this.debugMode = debugMode;
        }

        public int getScreenshotInterval() {
            return screenshotInterval;
        }

        public void setScreenshotInterval(int screenshotInterval) {
            this.screenshotInterval = screenshotInterval;
        }
    }
}
