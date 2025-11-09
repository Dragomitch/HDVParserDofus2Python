package com.dofusretro.pricetracker.automation;

import com.dofusretro.pricetracker.config.AutomationConfig;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for template matching using JavaCV/OpenCV.
 *
 * <p>This service provides image recognition capabilities for finding
 * UI elements on screen. It uses OpenCV's template matching algorithms
 * for fast and accurate detection.
 *
 * <p>Features:
 * <ul>
 *   <li>Template loading and caching</li>
 *   <li>Single template matching (find first occurrence)</li>
 *   <li>Multiple template matching (find all occurrences)</li>
 *   <li>Configurable similarity threshold</li>
 *   <li>Match scoring and ranking</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>{@code
 * BufferedImage screenshot = robot.createScreenCapture(screenRect);
 * Point location = templateService.findTemplate("category-unchecked", screenshot);
 * if (location != null) {
 *     robot.mouseMove(location.x, location.y);
 *     robot.click();
 * }
 * }</pre>
 *
 * @since 0.1.0
 */
@Service
@Slf4j
public class TemplateMatchingService {

    /**
     * Cache of loaded template images
     * Key: template name, Value: OpenCV Mat
     */
    private final Map<String, Mat> templates = new ConcurrentHashMap<>();

    /**
     * Automation configuration
     */
    private final AutomationConfig config;

    /**
     * Create template matching service.
     *
     * @param config Automation configuration
     */
    public TemplateMatchingService(AutomationConfig config) {
        this.config = config;
        loadTemplates();
    }

    /**
     * Find template on screen.
     *
     * <p>Returns the center point of the best match above threshold,
     * or null if no match found.
     *
     * @param templateName Template identifier (filename without extension)
     * @param screenshot Current screen capture
     * @return Match center point, or null if not found
     */
    public Point findTemplate(String templateName, BufferedImage screenshot) {
        Mat templateMat = templates.get(templateName);
        if (templateMat == null) {
            log.warn("Template not loaded: {}", templateName);
            return null;
        }

        // Convert BufferedImage to OpenCV Mat
        Mat screenMat = bufferedImageToMat(screenshot);
        if (screenMat == null) {
            log.error("Failed to convert screenshot to Mat");
            return null;
        }

        try {
            // Perform template matching
            MatchResult result = performTemplateMatch(screenMat, templateMat);

            if (result.score >= config.getMatchThreshold()) {
                // Calculate center point
                int centerX = result.location.x() + templateMat.cols() / 2;
                int centerY = result.location.y() + templateMat.rows() / 2;

                log.debug("Template '{}' found at ({}, {}) with score {:.3f}",
                    templateName, centerX, centerY, result.score);

                return new Point(centerX, centerY);
            } else {
                log.debug("Template '{}' not found (best score: {:.3f}, threshold: {:.3f})",
                    templateName, result.score, config.getMatchThreshold());
                return null;
            }
        } finally {
            screenMat.release();
        }
    }

    /**
     * Find all matches of template on screen.
     *
     * <p>Returns list of all match locations above threshold,
     * sorted by match score (best first).
     *
     * @param templateName Template identifier
     * @param screenshot Current screen capture
     * @return List of match center points
     */
    public List<Point> findAllMatches(String templateName, BufferedImage screenshot) {
        Mat templateMat = templates.get(templateName);
        if (templateMat == null) {
            log.warn("Template not loaded: {}", templateName);
            return List.of();
        }

        Mat screenMat = bufferedImageToMat(screenshot);
        if (screenMat == null) {
            log.error("Failed to convert screenshot to Mat");
            return List.of();
        }

        List<Point> matches = new ArrayList<>();

        try {
            // Create result matrix
            int resultCols = screenMat.cols() - templateMat.cols() + 1;
            int resultRows = screenMat.rows() - templateMat.rows() + 1;

            if (resultCols <= 0 || resultRows <= 0) {
                log.warn("Template is larger than screenshot");
                return List.of();
            }

            Mat result = new Mat(resultRows, resultCols, opencv_core.CV_32F);

            // Perform template matching
            opencv_imgproc.matchTemplate(screenMat, templateMat, result,
                opencv_imgproc.TM_CCOEFF_NORMED);

            // Find all matches above threshold
            double threshold = config.getMatchThreshold();
            int templateWidth = templateMat.cols();
            int templateHeight = templateMat.rows();

            // Use non-maximum suppression to avoid duplicate detections
            List<MatchResult> allResults = new ArrayList<>();

            for (int y = 0; y < result.rows(); y++) {
                for (int x = 0; x < result.cols(); x++) {
                    double score = result.ptr(y, x).getDouble();
                    if (score >= threshold) {
                        allResults.add(new MatchResult(new Point(x, y), score));
                    }
                }
            }

            // Sort by score (best first)
            allResults.sort((a, b) -> Double.compare(b.score, a.score));

            // Apply non-maximum suppression
            for (MatchResult match : allResults) {
                boolean tooClose = false;

                for (Point existing : matches) {
                    double distance = Math.sqrt(
                        Math.pow(match.location.x - existing.x, 2) +
                        Math.pow(match.location.y - existing.y, 2)
                    );

                    if (distance < templateWidth / 2) {
                        tooClose = true;
                        break;
                    }
                }

                if (!tooClose) {
                    // Add center point
                    int centerX = match.location.x + templateWidth / 2;
                    int centerY = match.location.y + templateHeight / 2;
                    matches.add(new Point(centerX, centerY));
                }
            }

            result.release();

            log.debug("Template '{}' found {} matches", templateName, matches.size());

        } finally {
            screenMat.release();
        }

        return matches;
    }

    /**
     * Find template in specific region of screen.
     *
     * @param templateName Template identifier
     * @param screenshot Current screen capture
     * @param regionX Region X coordinate
     * @param regionY Region Y coordinate
     * @param regionWidth Region width
     * @param regionHeight Region height
     * @return Match center point (in screen coordinates), or null if not found
     */
    public Point findTemplateInRegion(String templateName, BufferedImage screenshot,
                                      int regionX, int regionY, int regionWidth, int regionHeight) {
        // Extract region from screenshot
        BufferedImage region = screenshot.getSubimage(regionX, regionY, regionWidth, regionHeight);

        // Find in region
        Point localPoint = findTemplate(templateName, region);

        if (localPoint == null) {
            return null;
        }

        // Convert to screen coordinates
        return new Point(
            localPoint.x + regionX,
            localPoint.y + regionY
        );
    }

    /**
     * Check if template exists on screen.
     *
     * @param templateName Template identifier
     * @param screenshot Current screen capture
     * @return true if template found
     */
    public boolean templateExists(String templateName, BufferedImage screenshot) {
        return findTemplate(templateName, screenshot) != null;
    }

    /**
     * Get match score for template.
     *
     * @param templateName Template identifier
     * @param screenshot Current screen capture
     * @return Match score (0.0 to 1.0), or 0 if not found
     */
    public double getMatchScore(String templateName, BufferedImage screenshot) {
        Mat templateMat = templates.get(templateName);
        if (templateMat == null) {
            return 0.0;
        }

        Mat screenMat = bufferedImageToMat(screenshot);
        if (screenMat == null) {
            return 0.0;
        }

        try {
            MatchResult result = performTemplateMatch(screenMat, templateMat);
            return result.score;
        } finally {
            screenMat.release();
        }
    }

    /**
     * Perform template matching and return best match.
     *
     * @param screen Screen image as Mat
     * @param template Template image as Mat
     * @return Match result with location and score
     */
    private MatchResult performTemplateMatch(Mat screen, Mat template) {
        // Create result matrix
        int resultCols = screen.cols() - template.cols() + 1;
        int resultRows = screen.rows() - template.rows() + 1;

        if (resultCols <= 0 || resultRows <= 0) {
            log.warn("Template is larger than screen");
            return new MatchResult(new Point(0, 0), 0.0);
        }

        Mat result = new Mat(resultRows, resultCols, opencv_core.CV_32F);

        // Perform template matching using normalized correlation coefficient
        opencv_imgproc.matchTemplate(screen, template, result,
            opencv_imgproc.TM_CCOEFF_NORMED);

        // Find best match
        double[] minVal = new double[1];
        double[] maxVal = new double[1];
        Point minLoc = new Point();
        Point maxLoc = new Point();

        opencv_core.minMaxLoc(result, minVal, maxVal,
            minLoc, maxLoc, null);

        result.release();

        // For TM_CCOEFF_NORMED, higher is better
        return new MatchResult(maxLoc, maxVal[0]);
    }

    /**
     * Convert BufferedImage to OpenCV Mat.
     *
     * @param image BufferedImage to convert
     * @return OpenCV Mat, or null if conversion fails
     */
    private Mat bufferedImageToMat(BufferedImage image) {
        try {
            // Convert to BGR format (OpenCV default)
            BufferedImage bgrImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR
            );

            bgrImage.getGraphics().drawImage(image, 0, 0, null);

            byte[] data = ((DataBufferByte) bgrImage.getRaster().getDataBuffer()).getData();
            Mat mat = new Mat(bgrImage.getHeight(), bgrImage.getWidth(), opencv_core.CV_8UC3);
            mat.data().put(data);

            return mat;
        } catch (Exception e) {
            log.error("Failed to convert BufferedImage to Mat", e);
            return null;
        }
    }

    /**
     * Load template images from configured directory.
     */
    private void loadTemplates() {
        log.info("Loading templates from: {}", config.getTemplatesPath());

        File templatesDir = new File(config.getTemplatesPath());
        if (!templatesDir.exists() || !templatesDir.isDirectory()) {
            log.warn("Templates directory not found: {}", templatesDir.getAbsolutePath());
            log.info("Creating templates directory");
            templatesDir.mkdirs();
            return;
        }

        File[] templateFiles = templatesDir.listFiles(
            (dir, name) -> name.toLowerCase().endsWith(".png")
        );

        if (templateFiles == null || templateFiles.length == 0) {
            log.warn("No template files found in: {}", templatesDir.getAbsolutePath());
            return;
        }

        int loadedCount = 0;
        for (File file : templateFiles) {
            try {
                String name = file.getName().replace(".png", "");
                Mat template = opencv_imgcodecs.imread(file.getAbsolutePath());

                if (template.empty()) {
                    log.error("Failed to load template (empty): {}", file.getName());
                    continue;
                }

                templates.put(name, template);
                loadedCount++;
                log.debug("Loaded template: {} ({}x{})", name,
                    template.cols(), template.rows());

            } catch (Exception e) {
                log.error("Failed to load template: {}", file.getName(), e);
            }
        }

        log.info("Loaded {} templates", loadedCount);
    }

    /**
     * Reload templates (for testing/development).
     */
    public void reloadTemplates() {
        log.info("Reloading templates");
        // Release existing templates
        templates.values().forEach(Mat::release);
        templates.clear();
        loadTemplates();
    }

    /**
     * Get list of loaded template names.
     *
     * @return List of template names
     */
    public List<String> getLoadedTemplates() {
        return new ArrayList<>(templates.keySet());
    }

    /**
     * Match result record.
     */
    private record MatchResult(Point location, double score) {}
}
