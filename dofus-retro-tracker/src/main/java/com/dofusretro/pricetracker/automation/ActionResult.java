package com.dofusretro.pricetracker.automation;

/**
 * Represents the result of an action execution.
 *
 * <p>Action results drive the state machine, determining which action
 * should execute next. Each result has semantic meaning that actions
 * can use to make decisions.
 *
 * <p>Result flow example:
 * <pre>
 * ClickCategory → SUCCESS → WaitForUI
 *               ↓ FAILURE → RetryCategory
 *               ↓ CATEGORY_END → Complete
 *
 * ClickItem → SUCCESS → NextItem
 *           ↓ ITEMS_END → NextCategory
 *           ↓ FAILURE → SkipItem
 * </pre>
 *
 * @since 0.1.0
 */
public enum ActionResult {

    /**
     * Action completed successfully.
     *
     * <p>The action performed its task without errors and verified
     * the expected outcome (e.g., click registered, element found).
     *
     * <p>Typical next actions: Continue to next step in sequence.
     */
    SUCCESS("Action completed successfully"),

    /**
     * Action failed to complete.
     *
     * <p>The action could not perform its task (e.g., element not found,
     * click didn't register, timeout exceeded).
     *
     * <p>Typical next actions: Retry, skip, or abort.
     */
    FAILURE("Action failed"),

    /**
     * Reached end of category list.
     *
     * <p>While iterating through categories, no more categories are available.
     * This indicates all categories have been processed.
     *
     * <p>Typical next actions: Complete automation or reset to first category.
     */
    CATEGORY_END("Reached end of category"),

    /**
     * Reached end of items list.
     *
     * <p>While iterating through items, no more items are visible.
     * This indicates all items in the current category have been processed.
     *
     * <p>Typical next actions: Move to next category.
     */
    ITEMS_END("Reached end of items list"),

    /**
     * Action should be retried.
     *
     * <p>The action encountered a temporary condition (e.g., UI not ready,
     * animation in progress) and should be attempted again.
     *
     * <p>Typical next actions: Same action with retry counter incremented.
     */
    RETRY("Action should be retried"),

    /**
     * Action was skipped.
     *
     * <p>The action determined it should not execute based on current
     * conditions (e.g., item already processed, category disabled).
     *
     * <p>Typical next actions: Continue to next action in sequence.
     */
    SKIP("Action skipped"),

    /**
     * All actions complete.
     *
     * <p>The automation sequence has finished successfully. No more
     * actions should be executed.
     *
     * <p>Typical next actions: None (state machine terminates).
     */
    DONE("All actions complete"),

    /**
     * Action timed out.
     *
     * <p>The action exceeded its timeout duration without completing.
     * This is typically converted to FAILURE by the state machine.
     *
     * <p>Typical next actions: Retry or abort.
     */
    TIMEOUT("Action timed out"),

    /**
     * Action requires user intervention.
     *
     * <p>The automation encountered a condition that requires manual
     * user action (e.g., captcha, dialog box, error message).
     *
     * <p>Typical next actions: Pause automation and notify user.
     */
    USER_INPUT_REQUIRED("User intervention required"),

    /**
     * UI element not found.
     *
     * <p>Template matching or element detection failed to find the
     * expected UI element on screen.
     *
     * <p>Typical next actions: Retry, wait, or skip.
     */
    ELEMENT_NOT_FOUND("UI element not found"),

    /**
     * No operation performed.
     *
     * <p>The action completed without performing any operations,
     * typically because preconditions weren't met.
     *
     * <p>Typical next actions: Continue to next action.
     */
    NOOP("No operation performed");

    private final String description;

    ActionResult(String description) {
        this.description = description;
    }

    /**
     * Get human-readable description of this result.
     *
     * @return Description string
     */
    public String getDescription() {
        return description;
    }

    /**
     * Check if this result indicates terminal state.
     *
     * <p>Terminal results cause the state machine to stop executing actions.
     *
     * @return true if this result is terminal (DONE, FAILURE, TIMEOUT)
     */
    public boolean isTerminal() {
        return this == DONE || this == FAILURE || this == TIMEOUT;
    }

    /**
     * Check if this result indicates success.
     *
     * <p>Success results allow the state machine to continue to the next action.
     *
     * @return true if this result indicates success (SUCCESS, SKIP, NOOP)
     */
    public boolean isSuccess() {
        return this == SUCCESS || this == SKIP || this == NOOP;
    }

    /**
     * Check if this result should trigger a retry.
     *
     * @return true if action should be retried (RETRY, ELEMENT_NOT_FOUND)
     */
    public boolean shouldRetry() {
        return this == RETRY || this == ELEMENT_NOT_FOUND;
    }

    /**
     * Check if this result indicates completion.
     *
     * @return true if automation sequence is complete (DONE, CATEGORY_END, ITEMS_END)
     */
    public boolean isCompletion() {
        return this == DONE || this == CATEGORY_END || this == ITEMS_END;
    }

    @Override
    public String toString() {
        return name() + ": " + description;
    }
}
