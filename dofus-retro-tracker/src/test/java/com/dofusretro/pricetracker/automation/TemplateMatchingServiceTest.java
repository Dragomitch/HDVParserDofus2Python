package com.dofusretro.pricetracker.automation;

import com.dofusretro.pricetracker.config.AutomationConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for TemplateMatchingService.
 */
class TemplateMatchingServiceTest {

    @TempDir
    Path tempDir;

    private TemplateMatchingService templateService;
    private AutomationConfig config;

    @BeforeEach
    void setUp() throws IOException {
        config = new AutomationConfig();
        config.setTemplatesPath(tempDir.toString());
        config.setMatchThreshold(0.8);

        // Create test templates
        createTestTemplate("test-square", createSquareImage(50, Color.RED));
        createTestTemplate("test-circle", createCircleImage(50, Color.BLUE));

        templateService = new TemplateMatchingService(config);
    }

    @Test
    void shouldLoadTemplatesOnStartup() {
        List<String> templates = templateService.getLoadedTemplates();

        assertThat(templates).contains("test-square", "test-circle");
        assertThat(templates).hasSize(2);
    }

    @Test
    void shouldFindExactMatch() {
        // Create screenshot with template
        BufferedImage screenshot = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = screenshot.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 200, 200);

        // Draw red square at (50, 50)
        g.setColor(Color.RED);
        g.fillRect(50, 50, 50, 50);
        g.dispose();

        // Find template
        Point match = templateService.findTemplate("test-square", screenshot);

        assertThat(match).isNotNull();
        // Match should be near center of square (50 + 25 = 75)
        assertThat(match.x).isBetween(70, 80);
        assertThat(match.y).isBetween(70, 80);
    }

    @Test
    void shouldReturnNullWhenTemplateNotFound() {
        // Create blank screenshot
        BufferedImage screenshot = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = screenshot.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 200, 200);
        g.dispose();

        Point match = templateService.findTemplate("test-square", screenshot);

        assertThat(match).isNull();
    }

    @Test
    void shouldReturnNullForNonexistentTemplate() {
        BufferedImage screenshot = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);

        Point match = templateService.findTemplate("nonexistent", screenshot);

        assertThat(match).isNull();
    }

    @Test
    void shouldFindAllMatches() {
        // Create screenshot with multiple red squares
        BufferedImage screenshot = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = screenshot.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 300, 300);

        // Draw 3 red squares
        g.setColor(Color.RED);
        g.fillRect(10, 10, 50, 50);
        g.fillRect(100, 10, 50, 50);
        g.fillRect(10, 100, 50, 50);
        g.dispose();

        List<Point> matches = templateService.findAllMatches("test-square", screenshot);

        assertThat(matches).hasSizeGreaterThanOrEqualTo(2); // Should find at least 2
    }

    @Test
    void shouldCheckTemplateExistence() {
        BufferedImage screenshot = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = screenshot.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 200, 200);
        g.setColor(Color.RED);
        g.fillRect(50, 50, 50, 50);
        g.dispose();

        boolean exists = templateService.templateExists("test-square", screenshot);

        assertThat(exists).isTrue();
    }

    @Test
    void shouldGetMatchScore() {
        // Create screenshot with template
        BufferedImage screenshot = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = screenshot.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 200, 200);
        g.setColor(Color.RED);
        g.fillRect(50, 50, 50, 50);
        g.dispose();

        double score = templateService.getMatchScore("test-square", screenshot);

        assertThat(score).isGreaterThan(0.0);
        assertThat(score).isLessThanOrEqualTo(1.0);
    }

    @Test
    void shouldReloadTemplates() throws IOException {
        // Create new template after initialization
        createTestTemplate("test-triangle", createTriangleImage(50, Color.GREEN));

        // Reload
        templateService.reloadTemplates();

        List<String> templates = templateService.getLoadedTemplates();
        assertThat(templates).contains("test-triangle");
    }

    @Test
    void shouldFindTemplateInRegion() {
        // Create screenshot with template in specific region
        BufferedImage screenshot = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = screenshot.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 400, 400);
        g.setColor(Color.RED);
        g.fillRect(250, 250, 50, 50); // Square in bottom-right quadrant
        g.dispose();

        // Search only in bottom-right region
        Point match = templateService.findTemplateInRegion(
            "test-square",
            screenshot,
            200, 200, 200, 200
        );

        assertThat(match).isNotNull();
        assertThat(match.x).isGreaterThan(200); // In screen coordinates
        assertThat(match.y).isGreaterThan(200);
    }

    // Helper methods

    private void createTestTemplate(String name, BufferedImage image) throws IOException {
        File templateFile = tempDir.resolve(name + ".png").toFile();
        ImageIO.write(image, "png", templateFile);
    }

    private BufferedImage createSquareImage(int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, size, size);
        g.dispose();
        return image;
    }

    private BufferedImage createCircleImage(int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, size, size);
        g.setColor(color);
        g.fillOval(0, 0, size, size);
        g.dispose();
        return image;
    }

    private BufferedImage createTriangleImage(int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, size, size);
        g.setColor(color);
        int[] xPoints = {size / 2, 0, size};
        int[] yPoints = {0, size, size};
        g.fillPolygon(xPoints, yPoints, 3);
        g.dispose();
        return image;
    }
}
