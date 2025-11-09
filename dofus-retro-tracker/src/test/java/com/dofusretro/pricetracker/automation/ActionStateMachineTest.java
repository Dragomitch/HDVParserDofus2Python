package com.dofusretro.pricetracker.automation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Robot;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for ActionStateMachine.
 */
class ActionStateMachineTest {

    private ActionStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new ActionStateMachine();
    }

    @Test
    void shouldStartWithInitialAction() {
        assertThat(stateMachine.getCurrentAction()).isNotNull();
        assertThat(stateMachine.isDone()).isFalse();
        assertThat(stateMachine.getActionCount()).isZero();
    }

    @Test
    void shouldTransitionToNextActionOnSuccess() {
        // Create test action that transitions to another action
        Action firstAction = new TestAction("First", ActionResult.SUCCESS);
        Action secondAction = new TestAction("Second", ActionResult.SUCCESS);
        ((TestAction) firstAction).setNextAction(secondAction);

        stateMachine.reset(firstAction);

        // Execute first action
        stateMachine.transition(ActionResult.SUCCESS);

        assertThat(stateMachine.getCurrentAction()).isEqualTo(secondAction);
        assertThat(stateMachine.getActionCount()).isEqualTo(1);
    }

    @Test
    void shouldTerminateOnFailure() {
        Action action = new TestAction("Test", ActionResult.FAILURE);
        stateMachine.reset(action);

        stateMachine.transition(ActionResult.FAILURE);

        assertThat(stateMachine.isDone()).isTrue();
        assertThat(stateMachine.getCurrentAction()).isNull();
    }

    @Test
    void shouldTerminateOnDone() {
        Action action = new TestAction("Test", ActionResult.DONE);
        stateMachine.reset(action);

        stateMachine.transition(ActionResult.DONE);

        assertThat(stateMachine.isDone()).isTrue();
    }

    @Test
    void shouldRetryOnRetryResult() {
        Action retryableAction = new TestAction("Retryable", ActionResult.RETRY);
        stateMachine.reset(retryableAction);

        // First retry
        stateMachine.transition(ActionResult.RETRY);
        assertThat(stateMachine.getCurrentAction()).isEqualTo(retryableAction);
        assertThat(stateMachine.getRetryCount()).isEqualTo(1);

        // Second retry
        stateMachine.transition(ActionResult.RETRY);
        assertThat(stateMachine.getRetryCount()).isEqualTo(2);
    }

    @Test
    void shouldStopRetryingAfterMaxRetries() {
        Action retryableAction = new TestAction("Retryable", ActionResult.RETRY);
        stateMachine.reset(retryableAction);

        // Retry 3 times (default max)
        for (int i = 0; i < 3; i++) {
            stateMachine.transition(ActionResult.RETRY);
            assertThat(stateMachine.isDone()).isFalse();
        }

        // Fourth retry should convert to failure and terminate
        stateMachine.transition(ActionResult.RETRY);
        assertThat(stateMachine.isDone()).isTrue();
    }

    @Test
    void shouldTrackHistory() {
        Action action1 = new TestAction("Action1", ActionResult.SUCCESS);
        Action action2 = new TestAction("Action2", ActionResult.SUCCESS);
        ((TestAction) action1).setNextAction(action2);

        stateMachine.reset(action1);

        stateMachine.transition(ActionResult.SUCCESS);
        stateMachine.transition(ActionResult.SUCCESS);

        assertThat(stateMachine.getHistory()).hasSize(2);
        assertThat(stateMachine.getActionCount()).isEqualTo(2);
    }

    @Test
    void shouldCalculateSuccessRate() {
        Action action = new TestAction("Test", ActionResult.SUCCESS);
        stateMachine.reset(action);

        // 3 successes, 1 failure
        stateMachine.transition(ActionResult.SUCCESS);
        stateMachine.transition(ActionResult.SUCCESS);
        stateMachine.transition(ActionResult.SUCCESS);
        stateMachine.transition(ActionResult.FAILURE);

        double successRate = stateMachine.getSuccessRate();
        assertThat(successRate).isEqualTo(0.75);
    }

    @Test
    void shouldDetectConsecutiveFailures() {
        Action action = new TestAction("Test", ActionResult.FAILURE);
        stateMachine.reset(action);

        // 5 consecutive failures
        for (int i = 0; i < 5; i++) {
            stateMachine.transition(ActionResult.FAILURE);
        }

        assertThat(stateMachine.hasConsecutiveFailures(5)).isTrue();
        assertThat(stateMachine.hasConsecutiveFailures(6)).isFalse();
    }

    @Test
    void shouldResetStateMachine() {
        Action action = new TestAction("Test", ActionResult.SUCCESS);
        stateMachine.reset(action);

        stateMachine.transition(ActionResult.SUCCESS);
        assertThat(stateMachine.getActionCount()).isEqualTo(1);

        stateMachine.reset();
        assertThat(stateMachine.getActionCount()).isZero();
        assertThat(stateMachine.getHistory()).isEmpty();
        assertThat(stateMachine.getCurrentAction()).isNotNull();
    }

    @Test
    void shouldHandleNullNextAction() {
        // Action that returns null for next action
        Action terminalAction = new TestAction("Terminal", ActionResult.SUCCESS);
        ((TestAction) terminalAction).setNextAction(null);

        stateMachine.reset(terminalAction);
        stateMachine.transition(ActionResult.SUCCESS);

        assertThat(stateMachine.isDone()).isTrue();
    }

    @Test
    void shouldGenerateStatistics() {
        Action action = new TestAction("Test", ActionResult.SUCCESS);
        stateMachine.reset(action);

        stateMachine.transition(ActionResult.SUCCESS);
        stateMachine.transition(ActionResult.SUCCESS);
        stateMachine.transition(ActionResult.FAILURE);

        String stats = stateMachine.getStatistics();
        assertThat(stats).contains("Actions: 3");
        assertThat(stats).contains("Success: 2");
    }

    /**
     * Test action implementation
     */
    private static class TestAction implements Action {
        private final String name;
        private final ActionResult resultToReturn;
        private Action nextAction;

        public TestAction(String name, ActionResult resultToReturn) {
            this.name = name;
            this.resultToReturn = resultToReturn;
        }

        public void setNextAction(Action nextAction) {
            this.nextAction = nextAction;
        }

        @Override
        public ActionResult execute(Robot robot) {
            return resultToReturn;
        }

        @Override
        public Action nextAction(ActionResult result) {
            return nextAction;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
