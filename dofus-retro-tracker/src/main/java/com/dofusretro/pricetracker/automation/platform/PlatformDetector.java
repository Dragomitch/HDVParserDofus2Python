package com.dofusretro.pricetracker.automation.platform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.*;

/**
 * Detects platform-specific information for automation.
 *
 * <p>Provides information about:
 * <ul>
 *   <li>Operating system</li>
 *   <li>Screen configuration (size, DPI, scaling)</li>
 *   <li>Multi-monitor setup</li>
 *   <li>Display server (X11/Wayland on Linux)</li>
 * </ul>
 *
 * @since 0.1.0
 */
@Component
@Slf4j
public class PlatformDetector {

    /**
     * Operating system types
     */
    public enum OS {
        WINDOWS,
        MACOS,
        LINUX,
        UNKNOWN
    }

    /**
     * Display server types (Linux)
     */
    public enum DisplayServer {
        X11,
        WAYLAND,
        UNKNOWN
    }

    private final OS operatingSystem;
    private final DisplayServer displayServer;
    private final double scalingFactor;
    private final int screenDPI;
    private final Dimension primaryScreenSize;
    private final int screenCount;

    /**
     * Create platform detector and detect current platform.
     */
    public PlatformDetector() {
        this.operatingSystem = detectOS();
        this.displayServer = detectDisplayServer();
        this.screenDPI = detectScreenDPI();
        this.scalingFactor = detectScalingFactor();
        this.primaryScreenSize = detectPrimaryScreenSize();
        this.screenCount = detectScreenCount();

        logPlatformInfo();
    }

    /**
     * Get operating system.
     *
     * @return Operating system type
     */
    public OS getOperatingSystem() {
        return operatingSystem;
    }

    /**
     * Get display server (Linux only).
     *
     * @return Display server type
     */
    public DisplayServer getDisplayServer() {
        return displayServer;
    }

    /**
     * Get screen DPI.
     *
     * @return DPI value (typically 96, 120, 144, 192)
     */
    public int getScreenDPI() {
        return screenDPI;
    }

    /**
     * Get scaling factor.
     *
     * @return Scaling factor (1.0 = 100%, 2.0 = 200%, etc.)
     */
    public double getScalingFactor() {
        return scalingFactor;
    }

    /**
     * Get primary screen size.
     *
     * @return Screen dimensions
     */
    public Dimension getPrimaryScreenSize() {
        return primaryScreenSize;
    }

    /**
     * Get number of screens.
     *
     * @return Screen count
     */
    public int getScreenCount() {
        return screenCount;
    }

    /**
     * Check if running on Windows.
     *
     * @return true if Windows
     */
    public boolean isWindows() {
        return operatingSystem == OS.WINDOWS;
    }

    /**
     * Check if running on macOS.
     *
     * @return true if macOS
     */
    public boolean isMacOS() {
        return operatingSystem == OS.MACOS;
    }

    /**
     * Check if running on Linux.
     *
     * @return true if Linux
     */
    public boolean isLinux() {
        return operatingSystem == OS.LINUX;
    }

    /**
     * Check if using HiDPI/Retina display.
     *
     * @return true if scaling factor > 1.0
     */
    public boolean isHiDPI() {
        return scalingFactor > 1.0;
    }

    /**
     * Check if using Wayland (Linux).
     *
     * @return true if Wayland display server
     */
    public boolean isWayland() {
        return displayServer == DisplayServer.WAYLAND;
    }

    /**
     * Check if Robot API is likely to work.
     *
     * @return true if Robot should work
     */
    public boolean isRobotSupported() {
        // Wayland has limited Robot support
        if (isLinux() && isWayland()) {
            log.warn("Robot API has limited support on Wayland");
            return false;
        }

        // Headless mode doesn't support Robot
        if (GraphicsEnvironment.isHeadless()) {
            log.warn("Robot API not supported in headless mode");
            return false;
        }

        return true;
    }

    /**
     * Detect operating system.
     */
    private OS detectOS() {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            return OS.WINDOWS;
        } else if (osName.contains("mac")) {
            return OS.MACOS;
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return OS.LINUX;
        } else {
            return OS.UNKNOWN;
        }
    }

    /**
     * Detect display server (Linux only).
     */
    private DisplayServer detectDisplayServer() {
        if (!isLinux()) {
            return DisplayServer.UNKNOWN;
        }

        // Check environment variables
        String waylandDisplay = System.getenv("WAYLAND_DISPLAY");
        String x11Display = System.getenv("DISPLAY");

        if (waylandDisplay != null && !waylandDisplay.isEmpty()) {
            return DisplayServer.WAYLAND;
        } else if (x11Display != null && !x11Display.isEmpty()) {
            return DisplayServer.X11;
        } else {
            return DisplayServer.UNKNOWN;
        }
    }

    /**
     * Detect screen DPI.
     */
    private int detectScreenDPI() {
        try {
            return Toolkit.getDefaultToolkit().getScreenResolution();
        } catch (Exception e) {
            log.warn("Failed to detect screen DPI, using default 96", e);
            return 96;
        }
    }

    /**
     * Detect scaling factor.
     */
    private double detectScalingFactor() {
        try {
            // Method 1: DPI-based calculation
            int dpi = getScreenDPI();
            double dpiScale = dpi / 96.0;

            // Method 2: Graphics transform (more accurate for HiDPI)
            GraphicsDevice gd = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();

            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            java.awt.geom.AffineTransform transform = gc.getDefaultTransform();
            double transformScale = transform.getScaleX();

            // Use the larger of the two methods
            double scale = Math.max(dpiScale, transformScale);

            log.debug("Detected scaling: DPI={}, DPIScale={}, TransformScale={}, Final={}",
                dpi, dpiScale, transformScale, scale);

            return scale;

        } catch (Exception e) {
            log.warn("Failed to detect scaling factor, using 1.0", e);
            return 1.0;
        }
    }

    /**
     * Detect primary screen size.
     */
    private Dimension detectPrimaryScreenSize() {
        try {
            return Toolkit.getDefaultToolkit().getScreenSize();
        } catch (Exception e) {
            log.warn("Failed to detect screen size", e);
            return new Dimension(1920, 1080);
        }
    }

    /**
     * Detect number of screens.
     */
    private int detectScreenCount() {
        try {
            GraphicsDevice[] screens = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getScreenDevices();
            return screens.length;
        } catch (Exception e) {
            log.warn("Failed to detect screen count", e);
            return 1;
        }
    }

    /**
     * Get all screen bounds.
     *
     * @return Array of screen bounds
     */
    public Rectangle[] getAllScreenBounds() {
        GraphicsDevice[] screens = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getScreenDevices();

        Rectangle[] bounds = new Rectangle[screens.length];
        for (int i = 0; i < screens.length; i++) {
            GraphicsConfiguration config = screens[i].getDefaultConfiguration();
            bounds[i] = config.getBounds();
        }

        return bounds;
    }

    /**
     * Log platform information.
     */
    private void logPlatformInfo() {
        log.info("Platform Detection:");
        log.info("  OS: {}", operatingSystem);
        log.info("  Display Server: {}", displayServer);
        log.info("  Screen DPI: {}", screenDPI);
        log.info("  Scaling Factor: {}", scalingFactor);
        log.info("  Primary Screen: {}x{}", primaryScreenSize.width, primaryScreenSize.height);
        log.info("  Screen Count: {}", screenCount);
        log.info("  HiDPI: {}", isHiDPI());
        log.info("  Robot Supported: {}", isRobotSupported());

        if (screenCount > 1) {
            Rectangle[] bounds = getAllScreenBounds();
            for (int i = 0; i < bounds.length; i++) {
                log.info("  Screen {}: {}x{} at ({},{})",
                    i, bounds[i].width, bounds[i].height, bounds[i].x, bounds[i].y);
            }
        }
    }

    /**
     * Get platform summary string.
     *
     * @return Platform info string
     */
    public String getPlatformSummary() {
        return String.format("%s %s DPI:%d Scale:%.1fx Screens:%d",
            operatingSystem,
            displayServer != DisplayServer.UNKNOWN ? "(" + displayServer + ")" : "",
            screenDPI,
            scalingFactor,
            screenCount
        );
    }
}
