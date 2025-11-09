package com.dofusretro.pricetracker.automation;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Proof of Concept for Java Robot API
 *
 * This demonstrates basic Robot capabilities:
 * - Mouse position detection
 * - Screen size detection
 * - Mouse movement
 * - Mouse clicking
 * - Screen capture
 *
 * Run this class manually to verify Robot functionality on your platform.
 */
public class RobotPoC {

    public static void main(String[] args) throws Exception {
        Robot robot = new Robot();

        System.out.println("Robot PoC - Basic Operations");
        System.out.println("==============================");

        // 1. Get mouse position
        Point mousePos = MouseInfo.getPointerInfo().getLocation();
        System.out.println("Current mouse position: " + mousePos);

        // 2. Get screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        System.out.println("Screen size: " + screenSize.width + "x" + screenSize.height);

        // 3. Get screen resolution (DPI)
        int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        System.out.println("Screen DPI: " + dpi);

        // 4. Check for multiple monitors
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();
        System.out.println("Number of screens: " + screens.length);

        for (int i = 0; i < screens.length; i++) {
            GraphicsConfiguration config = screens[i].getDefaultConfiguration();
            Rectangle bounds = config.getBounds();
            System.out.println("  Screen " + i + ": " +
                bounds.width + "x" + bounds.height +
                " at (" + bounds.x + ", " + bounds.y + ")");
        }

        // 5. Move mouse (with countdown)
        System.out.println("\nMoving mouse in 3 seconds...");
        Thread.sleep(3000);
        robot.mouseMove(screenSize.width / 2, screenSize.height / 2);
        System.out.println("Mouse moved to center: (" +
            screenSize.width / 2 + ", " + screenSize.height / 2 + ")");

        // 6. Click
        Thread.sleep(1000);
        System.out.println("Executing click...");
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        System.out.println("Click executed");

        // 7. Capture screen
        System.out.println("\nCapturing screenshot...");
        Rectangle screenRect = new Rectangle(screenSize);
        BufferedImage screenshot = robot.createScreenCapture(screenRect);
        System.out.println("Screenshot captured: " +
            screenshot.getWidth() + "x" + screenshot.getHeight());

        // 8. Save screenshot
        File output = new File("target/robot-poc-screenshot.png");
        output.getParentFile().mkdirs();
        ImageIO.write(screenshot, "png", output);
        System.out.println("Saved to: " + output.getAbsolutePath());

        // 9. Capture region
        System.out.println("\nCapturing center region (400x300)...");
        Rectangle centerRect = new Rectangle(
            screenSize.width / 2 - 200,
            screenSize.height / 2 - 150,
            400,
            300
        );
        BufferedImage regionCapture = robot.createScreenCapture(centerRect);
        File regionOutput = new File("target/robot-poc-region.png");
        ImageIO.write(regionCapture, "png", regionOutput);
        System.out.println("Region saved to: " + regionOutput.getAbsolutePath());

        // 10. Test auto delay
        robot.setAutoDelay(100); // 100ms between operations
        System.out.println("\nTesting auto delay (100ms)...");
        long startTime = System.currentTimeMillis();
        robot.mouseMove(100, 100);
        robot.mouseMove(200, 200);
        robot.mouseMove(300, 300);
        long endTime = System.currentTimeMillis();
        System.out.println("Three moves took: " + (endTime - startTime) + "ms");

        System.out.println("\n==============================");
        System.out.println("PoC complete!");
        System.out.println("==============================");
    }
}
