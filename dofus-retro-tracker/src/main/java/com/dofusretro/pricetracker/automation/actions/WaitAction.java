package com.dofusretro.pricetracker.automation.actions;

import com.dofusretro.pricetracker.automation.Action;
import com.dofusretro.pricetracker.automation.ActionResult;
import lombok.extern.slf4j.Slf4j;

import java.awt.Robot;

/**
 * Action that waits for a specified duration.
 *
 * <p>Useful for:
 * <ul>
 *   <li>Allowing UI to update after interaction</li>
 *   <li>Waiting for animations to complete</li>
 *   <li>Rate limiting automation</li>
 *   <li>Giving network requests time to complete</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * // Wait 1 second for UI to load
 * Action waitAction = new WaitAction(1000);
 * ActionResult result = waitAction.execute(robot);
 * }</pre>
 *
 * @since 0.1.0
 */
@Slf4j
public class WaitAction implements Action {

    /**
     * Wait duration in milliseconds
     */
    private final int durationMs;

    /**
     * Next action to execute after waiting
     */
    private final Action nextAction;

    /**
     * Create wait action with specified duration.
     *
     * @param durationMs Wait duration in milliseconds
     */
    public WaitAction(int durationMs) {
        this(durationMs, null);
    }

    /**
     * Create wait action with specified duration and next action.
     *
     * @param durationMs Wait duration in milliseconds
     * @param nextAction Next action to execute
     */
    public WaitAction(int durationMs, Action nextAction) {
        this.durationMs = durationMs;
        this.nextAction = nextAction;
    }

    @Override
    public ActionResult execute(Robot robot) throws Exception {
        log.debug("Waiting for {}ms", durationMs);

        Thread.sleep(durationMs);

        log.debug("Wait complete");
        return ActionResult.SUCCESS;
    }

    @Override
    public Action nextAction(ActionResult result) {
        return nextAction;
    }

    @Override
    public String getName() {
        return String.format("Wait[%dms]", durationMs);
    }

    @Override
    public int getTimeout() {
        // Timeout should be longer than wait duration
        return durationMs + 1000;
    }

    @Override
    public boolean canRetry() {
        // No point retrying a wait action
        return false;
    }
}
