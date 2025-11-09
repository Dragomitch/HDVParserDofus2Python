package com.dofusretro.pricetracker.automation;

import java.awt.Robot;

/**
 * Represents a single automation action in the state machine.
 *
 * <p>Each action encapsulates:
 * <ul>
 *   <li>The logic to execute the action (e.g., click, scroll, wait)</li>
 *   <li>Determination of the next action based on the result</li>
 *   <li>Timeout configuration for the action</li>
 * </ul>
 *
 * <p>Actions form a state machine where each action determines its successor
 * based on the execution result. This allows for dynamic, context-aware
 * automation flows.
 *
 * <p>Example implementation:
 * <pre>{@code
 * public class ClickCategoryAction implements Action {
 *     private final int categoryIndex;
 *
 *     public ActionResult execute(Robot robot) {
 *         // Find and click category
 *         Point categoryLocation = findCategory(categoryIndex);
 *         if (categoryLocation == null) {
 *             return ActionResult.FAILURE;
 *         }
 *         robot.mouseMove(categoryLocation.x, categoryLocation.y);
 *         robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
 *         robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
 *         return ActionResult.SUCCESS;
 *     }
 *
 *     public Action nextAction(ActionResult result) {
 *         if (result == ActionResult.SUCCESS) {
 *             return new WaitAction(1000); // Wait for UI to update
 *         }
 *         return null; // Terminate on failure
 *     }
 * }
 * }</pre>
 *
 * @see ActionResult
 * @see ActionStateMachine
 * @since 0.1.0
 */
public interface Action {

    /**
     * Execute this action using the provided Robot instance.
     *
     * <p>This method contains the core logic of the action. It should:
     * <ul>
     *   <li>Perform the necessary automation steps (move mouse, click, etc.)</li>
     *   <li>Verify the action succeeded (e.g., template matching)</li>
     *   <li>Return an appropriate ActionResult</li>
     * </ul>
     *
     * <p>Implementation guidelines:
     * <ul>
     *   <li>Keep actions atomic and focused on a single task</li>
     *   <li>Handle errors gracefully and return appropriate results</li>
     *   <li>Avoid long-running operations (respect timeout)</li>
     *   <li>Log important events for debugging</li>
     * </ul>
     *
     * @param robot Robot instance for automation (mouse, keyboard, screen capture)
     * @return Result of action execution
     * @throws Exception if action fails catastrophically (will be caught by state machine)
     */
    ActionResult execute(Robot robot) throws Exception;

    /**
     * Determine the next action based on the execution result.
     *
     * <p>This method implements the state machine logic, deciding which action
     * should execute next based on the result of this action.
     *
     * <p>Common patterns:
     * <ul>
     *   <li>SUCCESS → Next logical action in sequence</li>
     *   <li>FAILURE → Retry action or alternate path</li>
     *   <li>CATEGORY_END → Move to next category</li>
     *   <li>ITEMS_END → Move to next category or complete</li>
     *   <li>RETRY → Return this same action</li>
     *   <li>DONE → Return null to terminate</li>
     * </ul>
     *
     * @param result Result from execute()
     * @return Next action to execute, or null if state machine should terminate
     */
    Action nextAction(ActionResult result);

    /**
     * Get action name for logging and debugging.
     *
     * <p>Should return a descriptive name that identifies the action type
     * and any relevant parameters.
     *
     * <p>Examples:
     * <ul>
     *   <li>"ClickCategory[0]"</li>
     *   <li>"ScrollItems[down, 3]"</li>
     *   <li>"WaitForUI[2000ms]"</li>
     *   <li>"ClickItem[x=100, y=200]"</li>
     * </ul>
     *
     * @return Human-readable action name
     */
    String getName();

    /**
     * Get action timeout in milliseconds.
     *
     * <p>If action execution exceeds this timeout, it will be interrupted
     * and considered failed.
     *
     * <p>Default implementation returns 5000ms (5 seconds), which is suitable
     * for most UI interactions. Override for actions that may take longer
     * (e.g., loading screens, network requests).
     *
     * @return Timeout in milliseconds (default: 5000)
     */
    default int getTimeout() {
        return 5000;
    }

    /**
     * Get action priority for logging and monitoring.
     *
     * <p>Higher priority actions may trigger alerts if they fail.
     *
     * <p>Priority levels:
     * <ul>
     *   <li>0: Low - Optional actions, failures are acceptable</li>
     *   <li>1: Normal - Standard actions (default)</li>
     *   <li>2: High - Important actions, failures should be logged</li>
     *   <li>3: Critical - Essential actions, failures require immediate attention</li>
     * </ul>
     *
     * @return Priority level (default: 1)
     */
    default int getPriority() {
        return 1;
    }

    /**
     * Check if this action can be retried after failure.
     *
     * <p>Some actions (like clicks) can be retried safely, while others
     * (like one-time initializations) should not be retried.
     *
     * @return true if action can be retried (default: true)
     */
    default boolean canRetry() {
        return true;
    }

    /**
     * Get maximum number of retry attempts.
     *
     * <p>Only applies if {@link #canRetry()} returns true.
     *
     * @return Maximum retry attempts (default: 3)
     */
    default int getMaxRetries() {
        return 3;
    }
}
