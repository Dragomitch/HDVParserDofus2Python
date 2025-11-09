package com.dofusretro.pricetracker.automation.actions;

import com.dofusretro.pricetracker.automation.Action;
import com.dofusretro.pricetracker.automation.ActionResult;
import com.dofusretro.pricetracker.automation.TemplateMatchingService;
import lombok.extern.slf4j.Slf4j;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;

/**
 * Action to find and click on an unchecked category checkbox.
 *
 * <p>This action:
 * <ol>
 *   <li>Captures the category panel region</li>
 *   <li>Searches for unchecked category checkbox template</li>
 *   <li>Clicks on the checkbox if found</li>
 *   <li>Waits for UI to update</li>
 * </ol>
 *
 * <p>Success transitions to waiting for items to load.
 * Failure (no unchecked categories) indicates all categories processed.
 *
 * @since 0.1.0
 */
@Slf4j
public class ClickCategoryAction implements Action {

    private final TemplateMatchingService templateService;
    private final int categoryIndex;
    private final Rectangle categoryRegion;

    /**
     * Create action to click category at index.
     *
     * @param templateService Template matching service
     * @param categoryIndex Category index (0-based)
     * @param categoryRegion Screen region containing categories
     */
    public ClickCategoryAction(
        TemplateMatchingService templateService,
        int categoryIndex,
        Rectangle categoryRegion
    ) {
        this.templateService = templateService;
        this.categoryIndex = categoryIndex;
        this.categoryRegion = categoryRegion;
    }

    @Override
    public ActionResult execute(Robot robot) throws Exception {
        log.info("Searching for unchecked category (index: {})", categoryIndex);

        // Capture category panel region
        BufferedImage screenshot = robot.createScreenCapture(categoryRegion);

        // Find unchecked category checkbox
        Point categoryLocation = templateService.findTemplate(
            "category-unchecked",
            screenshot
        );

        if (categoryLocation == null) {
            log.info("No unchecked categories found");
            return ActionResult.CATEGORY_END;
        }

        // Convert to screen coordinates
        int screenX = categoryRegion.x + categoryLocation.x;
        int screenY = categoryRegion.y + categoryLocation.y;

        log.info("Found unchecked category at ({}, {})", screenX, screenY);

        // Move mouse to category
        robot.mouseMove(screenX, screenY);
        Thread.sleep(100); // Small delay for visual feedback

        // Click category checkbox
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(50);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

        log.info("Clicked category checkbox");

        // Wait for UI to update
        Thread.sleep(500);

        // Verify category was checked (optional validation)
        BufferedImage afterClick = robot.createScreenCapture(categoryRegion);
        Point checkedLocation = templateService.findTemplate(
            "category-checked",
            afterClick
        );

        if (checkedLocation != null) {
            log.debug("Verified category is now checked");
            return ActionResult.SUCCESS;
        } else {
            log.warn("Could not verify category was checked, assuming success");
            return ActionResult.SUCCESS;
        }
    }

    @Override
    public Action nextAction(ActionResult result) {
        if (result == ActionResult.SUCCESS) {
            // Wait for items to load, then start clicking items
            return new WaitAction(1000,
                new ScrollItemsAction(templateService, ScrollDirection.DOWN, 5));
        } else if (result == ActionResult.CATEGORY_END) {
            // All categories processed
            log.info("All categories processed");
            return null; // Done
        } else {
            // Failure - could retry or skip
            log.warn("Category click failed, retrying next category");
            return new ClickCategoryAction(
                templateService,
                categoryIndex + 1,
                categoryRegion
            );
        }
    }

    @Override
    public String getName() {
        return String.format("ClickCategory[%d]", categoryIndex);
    }

    @Override
    public int getTimeout() {
        return 10000; // 10 seconds (includes UI load time)
    }
}
