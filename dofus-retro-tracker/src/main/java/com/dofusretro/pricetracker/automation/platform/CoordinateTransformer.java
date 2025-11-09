package com.dofusretro.pricetracker.automation.platform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * Transforms coordinates for cross-platform compatibility.
 *
 * <p>Handles:
 * <ul>
 *   <li>HiDPI/Retina display scaling</li>
 *   <li>DPI scaling (Windows)</li>
 *   <li>Multi-monitor coordinate translation</li>
 *   <li>Platform-specific coordinate systems</li>
 * </ul>
 *
 * <p>Coordinate systems:
 * <ul>
 *   <li><b>Logical coordinates:</b> Platform-independent, what user sees</li>
 *   <li><b>Physical coordinates:</b> Actual screen pixels</li>
 *   <li><b>Robot coordinates:</b> What Robot API uses</li>
 * </ul>
 *
 * @since 0.1.0
 */
@Component
@Slf4j
public class CoordinateTransformer {

    private final PlatformDetector platformDetector;

    /**
     * Create coordinate transformer.
     *
     * @param platformDetector Platform detector
     */
    public CoordinateTransformer(PlatformDetector platformDetector) {
        this.platformDetector = platformDetector;
    }

    /**
     * Transform logical coordinates to Robot coordinates.
     *
     * <p>On HiDPI displays, logical coordinates may differ from
     * physical coordinates. This method ensures Robot API receives
     * correct coordinates.
     *
     * @param logicalX Logical X coordinate
     * @param logicalY Logical Y coordinate
     * @return Physical coordinates for Robot
     */
    public Point toRobotCoordinates(int logicalX, int logicalY) {
        double scale = platformDetector.getScalingFactor();

        if (scale == 1.0) {
            // No scaling needed
            return new Point(logicalX, logicalY);
        }

        // Apply scaling
        int physicalX = (int) Math.round(logicalX * scale);
        int physicalY = (int) Math.round(logicalY * scale);

        log.debug("Transformed logical ({}, {}) to physical ({}, {}) [scale: {}]",
            logicalX, logicalY, physicalX, physicalY, scale);

        return new Point(physicalX, physicalY);
    }

    /**
     * Transform Robot coordinates to logical coordinates.
     *
     * @param physicalX Physical X coordinate
     * @param physicalY Physical Y coordinate
     * @return Logical coordinates
     */
    public Point toLogicalCoordinates(int physicalX, int physicalY) {
        double scale = platformDetector.getScalingFactor();

        if (scale == 1.0) {
            return new Point(physicalX, physicalY);
        }

        int logicalX = (int) Math.round(physicalX / scale);
        int logicalY = (int) Math.round(physicalY / scale);

        log.debug("Transformed physical ({}, {}) to logical ({}, {}) [scale: {}]",
            physicalX, physicalY, logicalX, logicalY, scale);

        return new Point(logicalX, logicalY);
    }

    /**
     * Transform logical point to Robot coordinates.
     *
     * @param logicalPoint Logical point
     * @return Physical point
     */
    public Point toRobotCoordinates(Point logicalPoint) {
        return toRobotCoordinates(logicalPoint.x, logicalPoint.y);
    }

    /**
     * Transform Robot point to logical coordinates.
     *
     * @param physicalPoint Physical point
     * @return Logical point
     */
    public Point toLogicalCoordinates(Point physicalPoint) {
        return toLogicalCoordinates(physicalPoint.x, physicalPoint.y);
    }

    /**
     * Transform logical rectangle to Robot coordinates.
     *
     * @param logicalRect Logical rectangle
     * @return Physical rectangle
     */
    public Rectangle toRobotCoordinates(Rectangle logicalRect) {
        double scale = platformDetector.getScalingFactor();

        if (scale == 1.0) {
            return new Rectangle(logicalRect);
        }

        return new Rectangle(
            (int) Math.round(logicalRect.x * scale),
            (int) Math.round(logicalRect.y * scale),
            (int) Math.round(logicalRect.width * scale),
            (int) Math.round(logicalRect.height * scale)
        );
    }

    /**
     * Transform Robot rectangle to logical coordinates.
     *
     * @param physicalRect Physical rectangle
     * @return Logical rectangle
     */
    public Rectangle toLogicalCoordinates(Rectangle physicalRect) {
        double scale = platformDetector.getScalingFactor();

        if (scale == 1.0) {
            return new Rectangle(physicalRect);
        }

        return new Rectangle(
            (int) Math.round(physicalRect.x / scale),
            (int) Math.round(physicalRect.y / scale),
            (int) Math.round(physicalRect.width / scale),
            (int) Math.round(physicalRect.height / scale)
        );
    }

    /**
     * Get current scaling factor.
     *
     * @return Scaling factor
     */
    public double getScalingFactor() {
        return platformDetector.getScalingFactor();
    }

    /**
     * Check if coordinate transformation is needed.
     *
     * @return true if scaling is applied
     */
    public boolean isScalingEnabled() {
        return platformDetector.getScalingFactor() != 1.0;
    }

    /**
     * Normalize coordinates to primary screen.
     *
     * <p>In multi-monitor setups, converts coordinates from
     * any screen to primary screen coordinate system.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param screenIndex Source screen index
     * @return Normalized point
     */
    public Point normalizeToPrimaryScreen(int x, int y, int screenIndex) {
        if (screenIndex == 0 || platformDetector.getScreenCount() == 1) {
            return new Point(x, y);
        }

        Rectangle[] bounds = platformDetector.getAllScreenBounds();
        if (screenIndex < 0 || screenIndex >= bounds.length) {
            log.warn("Invalid screen index: {}", screenIndex);
            return new Point(x, y);
        }

        Rectangle sourceBounds = bounds[screenIndex];
        Rectangle primaryBounds = bounds[0];

        // Translate from source screen to primary screen
        int normalizedX = x - sourceBounds.x + primaryBounds.x;
        int normalizedY = y - sourceBounds.y + primaryBounds.y;

        return new Point(normalizedX, normalizedY);
    }

    /**
     * Find which screen contains the given point.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @return Screen index, or -1 if not found
     */
    public int findScreenForPoint(int x, int y) {
        Rectangle[] bounds = platformDetector.getAllScreenBounds();

        for (int i = 0; i < bounds.length; i++) {
            if (bounds[i].contains(x, y)) {
                return i;
            }
        }

        return -1; // Point not on any screen
    }
}
