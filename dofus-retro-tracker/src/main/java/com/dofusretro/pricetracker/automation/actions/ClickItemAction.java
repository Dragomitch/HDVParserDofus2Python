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
import java.util.List;

/**
 * Action to click on an item in the items list.
 *
 * <p>This action:
 * <ol>
 *   <li>Finds all visible item slots</li>
 *   <li>Clicks on item at specified index</li>
 *   <li>Waits for item details to load</li>
 *   <li>Captures item information (delegated to other services)</li>
 * </ol>
 *
 * <p>After clicking, transitions to next item or scrolling.
 *
 * @since 0.1.0
 */
@Slf4j
public class ClickItemAction implements Action {

    private final TemplateMatchingService templateService;
    private final int itemIndex;
    private Point clickedLocation;

    /**
     * Create action to click item at index.
     *
     * @param templateService Template matching service
     * @param itemIndex Item index in current view (0-based)
     */
    public ClickItemAction(
        TemplateMatchingService templateService,
        int itemIndex
    ) {
        this.templateService = templateService;
        this.itemIndex = itemIndex;
    }

    @Override
    public ActionResult execute(Robot robot) throws Exception {
        log.info("Searching for item slot (index: {})", itemIndex);

        // Capture screen
        java.awt.Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle screenRect = new Rectangle(screenSize);
        BufferedImage screenshot = robot.createScreenCapture(screenRect);

        // Find all item slots
        List<Point> itemSlots = templateService.findAllMatches("item-slot", screenshot);

        if (itemSlots.isEmpty()) {
            log.info("No item slots found");
            return ActionResult.ITEMS_END;
        }

        log.debug("Found {} item slots", itemSlots.size());

        // Check if requested index is available
        if (itemIndex >= itemSlots.size()) {
            log.info("Item index {} out of range (only {} items visible)",
                itemIndex, itemSlots.size());
            return ActionResult.ITEMS_END;
        }

        // Get item location
        Point itemLocation = itemSlots.get(itemIndex);
        clickedLocation = itemLocation;

        log.info("Clicking item at ({}, {})", itemLocation.x, itemLocation.y);

        // Move mouse to item
        robot.mouseMove(itemLocation.x, itemLocation.y);
        Thread.sleep(100);

        // Double-click on item (common pattern in auction houses)
        for (int i = 0; i < 2; i++) {
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(50);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(100);
        }

        log.info("Clicked item");

        // Wait for item details to load
        Thread.sleep(1000);

        // At this point, other services would capture item data
        // For now, we just return success

        return ActionResult.SUCCESS;
    }

    @Override
    public Action nextAction(ActionResult result) {
        if (result == ActionResult.SUCCESS) {
            // Click next item
            return new ClickItemAction(templateService, itemIndex + 1);
        } else if (result == ActionResult.ITEMS_END) {
            // No more items visible, scroll down to see more
            return new ScrollItemsAction(
                templateService,
                ScrollDirection.DOWN,
                3
            );
        } else {
            // Failure - skip this item
            return new ClickItemAction(templateService, itemIndex + 1);
        }
    }

    @Override
    public String getName() {
        return String.format("ClickItem[%d]", itemIndex);
    }

    @Override
    public int getTimeout() {
        return 5000;
    }

    /**
     * Get location where item was clicked (for verification).
     *
     * @return Click location, or null if not yet clicked
     */
    public Point getClickedLocation() {
        return clickedLocation;
    }
}
