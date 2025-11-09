package com.dofusretro.pricetracker.automation;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock Robot for testing automation without actual GUI interaction.
 *
 * <p>This mock implementation:
 * <ul>
 *   <li>Records all operations (mouse moves, clicks, etc.)</li>
 *   <li>Returns test screenshots instead of actual captures</li>
 *   <li>Allows verification of automation logic</li>
 *   <li>Runs without requiring display/graphics environment</li>
 * </ul>
 *
 * <p>Usage in tests:
 * <pre>{@code
 * MockRobot mockRobot = new MockRobot();
 * mockRobot.setScreenshot(testImage);
 *
 * Action action = new ClickCategoryAction(...);
 * action.execute(mockRobot);
 *
 * // Verify operations
 * assertTrue(mockRobot.wasMouseMoved());
 * assertTrue(mockRobot.wasClicked());
 * }</pre>
 *
 * @since 0.1.0
 */
public class MockRobot extends Robot {

    /**
     * Operation types for recording
     */
    public enum Operation {
        MOUSE_MOVE,
        MOUSE_PRESS,
        MOUSE_RELEASE,
        MOUSE_WHEEL,
        KEY_PRESS,
        KEY_RELEASE,
        SCREEN_CAPTURE
    }

    /**
     * Record of an operation
     */
    public record OperationRecord(
        Operation operation,
        int param1,  // x coordinate or button mask or key code
        int param2,  // y coordinate or wheel amount
        long timestamp
    ) {}

    private final List<OperationRecord> operations = new ArrayList<>();
    private BufferedImage mockScreenshot;
    private Point mousePosition = new Point(0, 0);
    private int autoDelay = 0;

    /**
     * Create mock robot.
     *
     * @throws AWTException never thrown (for compatibility with Robot)
     */
    public MockRobot() throws AWTException {
        super();
        // Create default empty screenshot
        this.mockScreenshot = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
    }

    /**
     * Set mock screenshot to return on capture.
     *
     * @param screenshot Screenshot image
     */
    public void setScreenshot(BufferedImage screenshot) {
        this.mockScreenshot = screenshot;
    }

    /**
     * Get list of recorded operations.
     *
     * @return Immutable list of operations
     */
    public List<OperationRecord> getOperations() {
        return List.copyOf(operations);
    }

    /**
     * Clear recorded operations.
     */
    public void clearOperations() {
        operations.clear();
    }

    /**
     * Get current mock mouse position.
     *
     * @return Mouse position
     */
    public Point getMousePosition() {
        return new Point(mousePosition);
    }

    // Robot method overrides

    @Override
    public synchronized void mouseMove(int x, int y) {
        operations.add(new OperationRecord(
            Operation.MOUSE_MOVE, x, y, System.currentTimeMillis()
        ));
        mousePosition.setLocation(x, y);
    }

    @Override
    public synchronized void mousePress(int buttons) {
        operations.add(new OperationRecord(
            Operation.MOUSE_PRESS, buttons, 0, System.currentTimeMillis()
        ));
    }

    @Override
    public synchronized void mouseRelease(int buttons) {
        operations.add(new OperationRecord(
            Operation.MOUSE_RELEASE, buttons, 0, System.currentTimeMillis()
        ));
    }

    @Override
    public synchronized void mouseWheel(int wheelAmt) {
        operations.add(new OperationRecord(
            Operation.MOUSE_WHEEL, wheelAmt, 0, System.currentTimeMillis()
        ));
    }

    @Override
    public synchronized void keyPress(int keycode) {
        operations.add(new OperationRecord(
            Operation.KEY_PRESS, keycode, 0, System.currentTimeMillis()
        ));
    }

    @Override
    public synchronized void keyRelease(int keycode) {
        operations.add(new OperationRecord(
            Operation.KEY_RELEASE, keycode, 0, System.currentTimeMillis()
        ));
    }

    @Override
    public synchronized BufferedImage createScreenCapture(Rectangle screenRect) {
        operations.add(new OperationRecord(
            Operation.SCREEN_CAPTURE,
            screenRect.width,
            screenRect.height,
            System.currentTimeMillis()
        ));

        // Return subsection of mock screenshot if possible
        if (mockScreenshot != null &&
            screenRect.x + screenRect.width <= mockScreenshot.getWidth() &&
            screenRect.y + screenRect.height <= mockScreenshot.getHeight()) {

            return mockScreenshot.getSubimage(
                screenRect.x,
                screenRect.y,
                screenRect.width,
                screenRect.height
            );
        }

        // Return full mock screenshot
        return mockScreenshot;
    }

    @Override
    public synchronized void setAutoDelay(int ms) {
        this.autoDelay = ms;
    }

    @Override
    public synchronized int getAutoDelay() {
        return autoDelay;
    }

    // Test helper methods

    /**
     * Check if mouse was moved.
     *
     * @return true if mouseMove was called
     */
    public boolean wasMouseMoved() {
        return operations.stream()
            .anyMatch(op -> op.operation() == Operation.MOUSE_MOVE);
    }

    /**
     * Check if mouse was moved to specific location.
     *
     * @param x Expected X coordinate
     * @param y Expected Y coordinate
     * @return true if mouse moved to (x, y)
     */
    public boolean wasMouseMovedTo(int x, int y) {
        return operations.stream()
            .anyMatch(op ->
                op.operation() == Operation.MOUSE_MOVE &&
                op.param1() == x &&
                op.param2() == y
            );
    }

    /**
     * Check if mouse was clicked (press + release).
     *
     * @return true if click occurred
     */
    public boolean wasClicked() {
        boolean hasPress = operations.stream()
            .anyMatch(op -> op.operation() == Operation.MOUSE_PRESS);
        boolean hasRelease = operations.stream()
            .anyMatch(op -> op.operation() == Operation.MOUSE_RELEASE);
        return hasPress && hasRelease;
    }

    /**
     * Check if left mouse button was clicked.
     *
     * @return true if left click occurred
     */
    public boolean wasLeftClicked() {
        boolean hasPress = operations.stream()
            .anyMatch(op ->
                op.operation() == Operation.MOUSE_PRESS &&
                op.param1() == InputEvent.BUTTON1_DOWN_MASK
            );
        boolean hasRelease = operations.stream()
            .anyMatch(op ->
                op.operation() == Operation.MOUSE_RELEASE &&
                op.param1() == InputEvent.BUTTON1_DOWN_MASK
            );
        return hasPress && hasRelease;
    }

    /**
     * Check if mouse wheel was scrolled.
     *
     * @return true if scroll occurred
     */
    public boolean wasScrolled() {
        return operations.stream()
            .anyMatch(op -> op.operation() == Operation.MOUSE_WHEEL);
    }

    /**
     * Get total scroll amount.
     *
     * @return Sum of all scroll wheel amounts
     */
    public int getTotalScrollAmount() {
        return operations.stream()
            .filter(op -> op.operation() == Operation.MOUSE_WHEEL)
            .mapToInt(OperationRecord::param1)
            .sum();
    }

    /**
     * Check if screen was captured.
     *
     * @return true if capture occurred
     */
    public boolean wasCaptured() {
        return operations.stream()
            .anyMatch(op -> op.operation() == Operation.SCREEN_CAPTURE);
    }

    /**
     * Get number of screen captures.
     *
     * @return Capture count
     */
    public int getCaptureCount() {
        return (int) operations.stream()
            .filter(op -> op.operation() == Operation.SCREEN_CAPTURE)
            .count();
    }

    /**
     * Get number of operations.
     *
     * @return Operation count
     */
    public int getOperationCount() {
        return operations.size();
    }

    /**
     * Get number of operations of specific type.
     *
     * @param operation Operation type
     * @return Count of operations
     */
    public int getOperationCount(Operation operation) {
        return (int) operations.stream()
            .filter(op -> op.operation() == operation)
            .count();
    }

    /**
     * Get operations summary for debugging.
     *
     * @return Summary string
     */
    public String getOperationsSummary() {
        return String.format(
            "Operations: %d (moves: %d, clicks: %d, scrolls: %d, captures: %d)",
            operations.size(),
            getOperationCount(Operation.MOUSE_MOVE),
            getOperationCount(Operation.MOUSE_PRESS),
            getOperationCount(Operation.MOUSE_WHEEL),
            getOperationCount(Operation.SCREEN_CAPTURE)
        );
    }
}
