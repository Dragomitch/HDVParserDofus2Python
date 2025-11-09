package com.dofusretro.pricetracker.automation.actions;

/**
 * Scroll direction for scroll actions.
 *
 * @since 0.1.0
 */
public enum ScrollDirection {
    /**
     * Scroll up (negative scroll amount)
     */
    UP(-1),

    /**
     * Scroll down (positive scroll amount)
     */
    DOWN(1);

    private final int multiplier;

    ScrollDirection(int multiplier) {
        this.multiplier = multiplier;
    }

    /**
     * Get scroll multiplier.
     *
     * @return Multiplier for scroll amount (-1 for up, 1 for down)
     */
    public int getMultiplier() {
        return multiplier;
    }
}
