package com.dofusretro.pricetracker.automation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * State machine for managing automation action sequences.
 *
 * <p>The state machine:
 * <ul>
 *   <li>Maintains the current action to execute</li>
 *   <li>Transitions between actions based on results</li>
 *   <li>Tracks execution history for debugging</li>
 *   <li>Supports retry logic for failed actions</li>
 *   <li>Provides reset capability for restarting automation</li>
 * </ul>
 *
 * <p>Execution flow:
 * <pre>
 * 1. Get current action via {@link #getCurrentAction()}
 * 2. Execute action externally
 * 3. Report result via {@link #transition(ActionResult)}
 * 4. State machine determines next action
 * 5. Repeat until {@link #isDone()} returns true
 * </pre>
 *
 * <p>Example usage:
 * <pre>{@code
 * ActionStateMachine machine = new ActionStateMachine();
 * Robot robot = new Robot();
 *
 * while (!machine.isDone()) {
 *     Action action = machine.getCurrentAction();
 *     ActionResult result = action.execute(robot);
 *     machine.transition(result);
 * }
 * }</pre>
 *
 * @see Action
 * @see ActionResult
 * @since 0.1.0
 */
@Component
@Slf4j
public class ActionStateMachine {

    /**
     * Current action to be executed
     */
    private Action currentAction;

    /**
     * History of action results for debugging and analysis
     */
    private final List<ActionResultRecord> history = new ArrayList<>();

    /**
     * Total number of actions executed
     */
    private int actionCount = 0;

    /**
     * Current retry attempt for retryable actions
     */
    private int retryCount = 0;

    /**
     * Maximum retries allowed per action
     */
    private static final int DEFAULT_MAX_RETRIES = 3;

    /**
     * Record of an action execution
     */
    public record ActionResultRecord(
        int sequence,
        String actionName,
        ActionResult result,
        long timestamp,
        int retryAttempt
    ) {}

    /**
     * Creates a new state machine with default initial action.
     */
    public ActionStateMachine() {
        this.currentAction = createInitialAction();
        log.info("ActionStateMachine initialized with: {}", currentAction.getName());
    }

    /**
     * Creates a new state machine with custom initial action.
     *
     * @param initialAction First action to execute
     */
    public ActionStateMachine(Action initialAction) {
        this.currentAction = initialAction;
        log.info("ActionStateMachine initialized with custom action: {}",
            currentAction.getName());
    }

    /**
     * Get current action to execute.
     *
     * <p>Returns null if state machine has completed.
     *
     * @return Current action, or null if done
     */
    public Action getCurrentAction() {
        return currentAction;
    }

    /**
     * Transition to next action based on result.
     *
     * <p>This method:
     * <ul>
     *   <li>Records the result in history</li>
     *   <li>Handles retry logic for retryable results</li>
     *   <li>Asks current action for next action</li>
     *   <li>Updates current action</li>
     *   <li>Logs transition for debugging</li>
     * </ul>
     *
     * @param result Result of current action execution
     */
    public void transition(ActionResult result) {
        if (currentAction == null) {
            log.warn("Transition called on completed state machine");
            return;
        }

        actionCount++;

        // Record execution
        ActionResultRecord record = new ActionResultRecord(
            actionCount,
            currentAction.getName(),
            result,
            System.currentTimeMillis(),
            retryCount
        );
        history.add(record);

        log.info("Action #{}: {} -> {} (retry: {})",
            actionCount,
            currentAction.getName(),
            result,
            retryCount);

        // Handle terminal results
        if (result.isTerminal()) {
            log.info("State machine terminated: {}", result);
            currentAction = null;
            retryCount = 0;
            return;
        }

        // Handle retry logic
        if (result.shouldRetry() && currentAction.canRetry()) {
            if (retryCount < currentAction.getMaxRetries()) {
                retryCount++;
                log.info("Retrying action {} (attempt {}/{})",
                    currentAction.getName(),
                    retryCount,
                    currentAction.getMaxRetries());
                // Keep current action, increment retry counter
                return;
            } else {
                log.warn("Max retries ({}) exceeded for action: {}",
                    currentAction.getMaxRetries(),
                    currentAction.getName());
                // Convert to failure and proceed
                result = ActionResult.FAILURE;
            }
        }

        // Reset retry counter for new action
        retryCount = 0;

        // Get next action from current action
        Action nextAction = currentAction.nextAction(result);

        if (nextAction == null) {
            log.info("No more actions, state machine complete (total actions: {})",
                actionCount);
            currentAction = null;
        } else {
            log.debug("Transitioning from [{}] to [{}]",
                currentAction.getName(),
                nextAction.getName());
            currentAction = nextAction;
        }
    }

    /**
     * Check if state machine is done.
     *
     * @return true if no more actions to execute
     */
    public boolean isDone() {
        return currentAction == null;
    }

    /**
     * Reset state machine to initial state.
     *
     * <p>Clears history and resets to initial action.
     */
    public void reset() {
        history.clear();
        actionCount = 0;
        retryCount = 0;
        currentAction = createInitialAction();
        log.info("State machine reset (total previous actions: {})", actionCount);
    }

    /**
     * Reset with custom initial action.
     *
     * @param initialAction New starting action
     */
    public void reset(Action initialAction) {
        history.clear();
        actionCount = 0;
        retryCount = 0;
        currentAction = initialAction;
        log.info("State machine reset with custom action: {}", initialAction.getName());
    }

    /**
     * Get execution history.
     *
     * <p>Returns immutable copy of history.
     *
     * @return List of action result records
     */
    public List<ActionResultRecord> getHistory() {
        return Collections.unmodifiableList(history);
    }

    /**
     * Get total number of actions executed.
     *
     * @return Action count
     */
    public int getActionCount() {
        return actionCount;
    }

    /**
     * Get current retry attempt.
     *
     * @return Retry count for current action
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * Get success rate of executed actions.
     *
     * @return Percentage of successful actions (0.0 to 1.0)
     */
    public double getSuccessRate() {
        if (history.isEmpty()) {
            return 0.0;
        }

        long successCount = history.stream()
            .filter(record -> record.result().isSuccess())
            .count();

        return (double) successCount / history.size();
    }

    /**
     * Get statistics about action execution.
     *
     * @return Formatted statistics string
     */
    public String getStatistics() {
        if (history.isEmpty()) {
            return "No actions executed yet";
        }

        long successes = history.stream()
            .filter(record -> record.result().isSuccess())
            .count();

        long failures = history.stream()
            .filter(record -> record.result() == ActionResult.FAILURE)
            .count();

        long retries = history.stream()
            .filter(record -> record.retryAttempt() > 0)
            .count();

        return String.format(
            "Actions: %d, Success: %d (%.1f%%), Failures: %d, Retries: %d",
            actionCount,
            successes,
            getSuccessRate() * 100,
            failures,
            retries
        );
    }

    /**
     * Check if last N actions were failures.
     *
     * <p>Useful for detecting stuck states.
     *
     * @param count Number of recent actions to check
     * @return true if last N actions all failed
     */
    public boolean hasConsecutiveFailures(int count) {
        if (history.size() < count) {
            return false;
        }

        List<ActionResultRecord> recent = history.subList(
            history.size() - count,
            history.size()
        );

        return recent.stream()
            .allMatch(record -> record.result() == ActionResult.FAILURE);
    }

    /**
     * Create the default initial action.
     *
     * <p>This is a placeholder that should be replaced with actual
     * automation logic. In production, this would start with the
     * first category click action.
     *
     * @return Initial action
     */
    private Action createInitialAction() {
        // This is a placeholder - actual implementation will be in
        // concrete action classes (e.g., ClickCategoryAction)
        return new Action() {
            @Override
            public ActionResult execute(Robot robot) {
                log.info("Executing initial placeholder action");
                return ActionResult.SUCCESS;
            }

            @Override
            public Action nextAction(ActionResult result) {
                // Placeholder returns DONE
                return null;
            }

            @Override
            public String getName() {
                return "InitialAction[placeholder]";
            }
        };
    }
}
