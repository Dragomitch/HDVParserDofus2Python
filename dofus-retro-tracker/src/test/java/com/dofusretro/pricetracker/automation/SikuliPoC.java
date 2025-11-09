package com.dofusretro.pricetracker.automation;

import org.sikuli.script.*;

import java.io.File;

/**
 * Proof of Concept for SikuliX Template Matching
 *
 * This demonstrates SikuliX capabilities:
 * - Template image loading
 * - Image recognition on screen
 * - Match scoring and highlighting
 * - Clicking on matched regions
 *
 * SETUP REQUIRED:
 * 1. Create directory: src/test/resources/templates/
 * 2. Capture a small UI element (button, icon, etc.)
 * 3. Save it as: src/test/resources/templates/test-button.png
 *
 * Run this class manually to verify SikuliX functionality.
 */
public class SikuliPoC {

    public static void main(String[] args) throws Exception {
        Screen screen = new Screen();

        System.out.println("Sikuli PoC - Template Matching");
        System.out.println("================================");

        // Get screen info
        System.out.println("Screen ID: " + screen.getID());
        System.out.println("Screen bounds: " + screen.getBounds());

        // Template image (prepare beforehand)
        String templatePath = "src/test/resources/templates/test-button.png";
        File templateFile = new File(templatePath);

        if (!templateFile.exists()) {
            System.out.println("\n⚠ Template not found: " + templatePath);
            System.out.println("\nTO CREATE A TEMPLATE:");
            System.out.println("1. Take a screenshot of a UI element (button, icon, etc.)");
            System.out.println("2. Crop it to just the element (e.g., 50x50 pixels)");
            System.out.println("3. Save as: " + templatePath);
            System.out.println("\nRunning fallback demo with screen capture...\n");

            // Fallback: capture a region and try to find it
            demonstrateCaptureAndFind(screen);
            return;
        }

        System.out.println("Template found: " + templateFile.getAbsolutePath());
        System.out.println("Searching for template on screen...");

        try {
            // Find template on screen with lower confidence for testing
            Settings.MinSimilarity = 0.7;
            Match match = screen.find(templatePath);

            System.out.println("\n✓ Template found!");
            System.out.println("  Location: (" + match.x + ", " + match.y + ")");
            System.out.println("  Target center: " + match.getTarget());
            System.out.println("  Match score: " + String.format("%.2f", match.getScore()));
            System.out.println("  Size: " + match.w + "x" + match.h);

            // Highlight match
            System.out.println("\nHighlighting match for 2 seconds...");
            match.highlight(2); // 2 seconds

            // Option to click
            System.out.println("\nTo click on match, uncomment the code below");
            // System.out.println("Clicking in 2 seconds...");
            // Thread.sleep(2000);
            // match.click();
            // System.out.println("Clicked!");

        } catch (FindFailed e) {
            System.out.println("\n✗ Template not found on screen");
            System.out.println("  Reason: " + e.getMessage());
            System.out.println("\nTIPS:");
            System.out.println("- Ensure the UI element is visible on screen");
            System.out.println("- Try capturing template at same resolution as current screen");
            System.out.println("- Check if UI element appearance matches template exactly");
        }

        // Test findAll
        System.out.println("\n--- Testing findAll (multiple matches) ---");
        try {
            Finder finder = new Finder(screen.capture(screen.getBounds()));
            finder.findAll(templatePath);

            int count = 0;
            while (finder.hasNext()) {
                Match m = finder.next();
                count++;
                System.out.println("Match " + count + ": " +
                    "(" + m.x + ", " + m.y + ") " +
                    "score=" + String.format("%.2f", m.getScore()));
            }
            System.out.println("Total matches found: " + count);

        } catch (Exception e) {
            System.out.println("No multiple matches found");
        }

        System.out.println("\n================================");
        System.out.println("PoC complete!");
        System.out.println("================================");
    }

    /**
     * Fallback demonstration: capture a region and try to find it
     */
    private static void demonstrateCaptureAndFind(Screen screen) throws Exception {
        System.out.println("--- Capture and Find Demo ---");

        // Capture center region
        int x = screen.w / 2 - 50;
        int y = screen.h / 2 - 50;
        Region centerRegion = new Region(x, y, 100, 100);

        System.out.println("Capturing center region: " + centerRegion);
        ScreenImage captured = screen.capture(centerRegion);

        // Save captured image
        String capturePath = "target/sikuli-poc-capture.png";
        new File(capturePath).getParentFile().mkdirs();
        captured.save(capturePath);
        System.out.println("Saved capture to: " + capturePath);

        // Wait a moment
        Thread.sleep(2000);

        // Try to find the captured region
        System.out.println("\nSearching for captured region...");
        try {
            Match match = screen.find(capturePath);
            System.out.println("✓ Found captured region!");
            System.out.println("  Location: " + match.getTarget());
            System.out.println("  Score: " + String.format("%.2f", match.getScore()));
            match.highlight(1);
        } catch (FindFailed e) {
            System.out.println("✗ Could not find captured region");
        }
    }
}
