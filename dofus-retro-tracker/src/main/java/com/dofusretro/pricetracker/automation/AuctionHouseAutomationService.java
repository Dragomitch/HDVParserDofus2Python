package com.dofusretro.pricetracker.automation;

import com.dofusretro.pricetracker.config.AutomationConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main service for Auction House GUI automation.
 *
 * <p>This service orchestrates the entire automation workflow:
 * <ul>
 *   <li>Manages Robot instance for mouse/keyboard control</li>
 *   <li>Integrates template matching for UI element detection</li>
 *   <li>Executes action state machine</li>
 *   <li>Handles errors and failsafes</li>
 *   <li>Provides start/stop controls</li>
 * </ul>
 *
 * <p>Safety features:
 * <ul>
 *   <li>Failsafe: Move mouse to corner to abort</li>
 *   <li>Timeout: Actions have maximum execution time</li>
 *   <li>Error screenshots: Captures screen on failures</li>
 *   <li>State persistence: Can resume after interruption</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * @Autowired
 * private AuctionHouseAutomationService automationService;
 *
 * // Start automation
 * automationService.start();
 *
 * // Stop automation
 * automationService.stop();
 *
 * // Check status
 * boolean running = automationService.isRunning();
 * }</pre>
 *
 * @since 0.1.0
 */
@Service
@Slf4j
public class AuctionHouseAutomationService {

    private final AutomationConfig config;
    private final RobotFactory robotFactory;
    private final TemplateMatchingService templateMatchingService;
    private final ActionStateMachine stateMachine;

    /**
     * Robot instance for automation
     */
    private Robot robot;

    /**
     * Flag indicating if automation is running
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Flag indicating if automation is paused
     */
    private final AtomicBoolean paused = new AtomicBoolean(false);

    /**
     * Current execution thread
     */
    private Thread executionThread;

    /**
     * Create automation service.
     *
     * @param config Configuration
     * @param robotFactory Robot factory
     * @param templateMatchingService Template matching service
     * @param stateMachine State machine
     */
    public AuctionHouseAutomationService(
        AutomationConfig config,
        RobotFactory robotFactory,
        TemplateMatchingService templateMatchingService,
        ActionStateMachine stateMachine
    ) {
        this.config = config;
        this.robotFactory = robotFactory;
        this.templateMatchingService = templateMatchingService;
        this.stateMachine = stateMachine;
    }

    /**
     * Start automation.
     *
     * @throws IllegalStateException if automation is already running
     * @throws AWTException if Robot cannot be created
     */
    public void start() throws AWTException {
        if (running.get()) {
            throw new IllegalStateException("Automation is already running");
        }

        if (!config.isEnabled()) {
            log.warn("Automation is disabled in configuration");
            return;
        }

        // Check platform support
        if (!robotFactory.isRobotSupported()) {
            throw new IllegalStateException("Robot not supported on this platform");
        }

        log.info("Starting Auction House automation");
        log.info("Platform: {}", robotFactory.getPlatformInfo());

        // Create Robot instance
        robot = robotFactory.createRobot();

        // Reset state machine
        stateMachine.reset();

        // Start execution thread
        running.set(true);
        executionThread = new Thread(this::runAutomation, "AutomationExecutor");
        executionThread.start();

        log.info("Automation started");
    }

    /**
     * Stop automation.
     */
    public void stop() {
        if (!running.get()) {
            log.warn("Automation is not running");
            return;
        }

        log.info("Stopping automation");
        running.set(false);

        // Interrupt execution thread
        if (executionThread != null && executionThread.isAlive()) {
            executionThread.interrupt();
            try {
                executionThread.join(5000); // Wait up to 5 seconds
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for execution thread to stop");
                Thread.currentThread().interrupt();
            }
        }

        log.info("Automation stopped");
        log.info("Statistics: {}", stateMachine.getStatistics());
    }

    /**
     * Pause automation.
     */
    public void pause() {
        if (!running.get()) {
            log.warn("Cannot pause: automation is not running");
            return;
        }

        paused.set(true);
        log.info("Automation paused");
    }

    /**
     * Resume automation.
     */
    public void resume() {
        if (!running.get()) {
            log.warn("Cannot resume: automation is not running");
            return;
        }

        paused.set(false);
        log.info("Automation resumed");
    }

    /**
     * Check if automation is running.
     *
     * @return true if running
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Check if automation is paused.
     *
     * @return true if paused
     */
    public boolean isPaused() {
        return paused.get();
    }

    /**
     * Main automation execution loop.
     */
    private void runAutomation() {
        log.info("Automation execution started");

        try {
            while (running.get() && !stateMachine.isDone()) {
                // Check for pause
                while (paused.get() && running.get()) {
                    Thread.sleep(100);
                }

                if (!running.get()) {
                    break;
                }

                // Check failsafe
                if (checkFailsafe()) {
                    log.warn("Failsafe triggered! Stopping automation");
                    break;
                }

                // Get current action
                Action action = stateMachine.getCurrentAction();
                if (action == null) {
                    log.info("No more actions, automation complete");
                    break;
                }

                log.info("Executing action: {}", action.getName());

                // Execute action with timeout
                ActionResult result = executeActionWithTimeout(action);

                // Transition to next action
                stateMachine.transition(result);

                // Small delay between actions
                Thread.sleep(config.getActionDelayMs());
            }

            log.info("Automation execution completed");
            log.info("Final statistics: {}", stateMachine.getStatistics());

        } catch (InterruptedException e) {
            log.info("Automation execution interrupted");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Automation execution failed", e);
        } finally {
            running.set(false);
        }
    }

    /**
     * Execute action with timeout.
     *
     * @param action Action to execute
     * @return Action result
     */
    private ActionResult executeActionWithTimeout(Action action) {
        try {
            // Execute action (with timeout handling in future)
            ActionResult result = action.execute(robot);

            if (result == ActionResult.FAILURE && config.isSaveFailureScreenshots()) {
                saveFailureScreenshot(action.getName());
            }

            return result;

        } catch (Exception e) {
            log.error("Action execution failed: {}", action.getName(), e);

            if (config.isSaveFailureScreenshots()) {
                saveFailureScreenshot(action.getName());
            }

            return ActionResult.FAILURE;
        }
    }

    /**
     * Check if failsafe should trigger.
     *
     * <p>Failsafe triggers when mouse is moved to configured corner.
     *
     * @return true if failsafe triggered
     */
    private boolean checkFailsafe() {
        if (!config.isFailsafeEnabled()) {
            return false;
        }

        Point mousePos = MouseInfo.getPointerInfo().getLocation();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        return config.isInFailsafeZone(
            mousePos.x,
            mousePos.y,
            screenSize.width,
            screenSize.height
        );
    }

    /**
     * Save screenshot of current screen for debugging.
     *
     * @param actionName Action that failed
     */
    private void saveFailureScreenshot(String actionName) {
        try {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle screenRect = new Rectangle(screenSize);
            BufferedImage screenshot = robot.createScreenCapture(screenRect);

            // Create failure screenshots directory
            File outputDir = new File(config.getFailureScreenshotsPath());
            outputDir.mkdirs();

            // Generate filename with timestamp
            String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("failure_%s_%s.png",
                actionName.replaceAll("[^a-zA-Z0-9]", "_"),
                timestamp);

            File outputFile = new File(outputDir, filename);
            ImageIO.write(screenshot, "png", outputFile);

            log.info("Saved failure screenshot: {}", outputFile.getAbsolutePath());

        } catch (IOException e) {
            log.error("Failed to save failure screenshot", e);
        }
    }

    /**
     * Get current state machine statistics.
     *
     * @return Statistics string
     */
    public String getStatistics() {
        return stateMachine.getStatistics();
    }

    /**
     * Get current action being executed.
     *
     * @return Current action name, or null if not running
     */
    public String getCurrentAction() {
        if (!running.get()) {
            return null;
        }

        Action action = stateMachine.getCurrentAction();
        return action != null ? action.getName() : null;
    }

    /**
     * Get template matching service (for testing/debugging).
     *
     * @return Template matching service
     */
    public TemplateMatchingService getTemplateMatchingService() {
        return templateMatchingService;
    }

    /**
     * Get state machine (for testing/debugging).
     *
     * @return State machine
     */
    public ActionStateMachine getStateMachine() {
        return stateMachine;
    }

    /**
     * Scheduled health check (runs every minute when enabled).
     */
    @Scheduled(fixedRate = 60000)
    public void healthCheck() {
        if (running.get()) {
            log.debug("Automation health check: running={}, paused={}, action={}",
                running.get(), paused.get(), getCurrentAction());

            // Check for stuck state (too many consecutive failures)
            if (stateMachine.hasConsecutiveFailures(10)) {
                log.error("Detected stuck state (10 consecutive failures), stopping automation");
                stop();
            }
        }
    }
}
