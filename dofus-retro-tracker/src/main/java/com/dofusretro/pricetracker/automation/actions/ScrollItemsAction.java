package com.dofusretro.pricetracker.automation.actions;

import com.dofusretro.pricetracker.automation.Action;
import com.dofusretro.pricetracker.automation.ActionResult;
import com.dofusretro.pricetracker.automation.TemplateMatchingService;
import lombok.extern.slf4j.Slf4j;

import java.awt.Point;
import java.awt.Robot;
import java.awt.image.BufferedImage;

/**
 * Action to scroll through items list.
 *
 * <p>This action:
 * <ol>
 *   <li>Captures screen before scroll</li>
 *   <li>Performs scroll operation</li>
 *   <li>Captures screen after scroll</li>
 *   <li>Compares to detect if scroll succeeded</li>
 * </ol>
 *
 * <p>Used to navigate through long lists of items in the auction house.
 *
 * @since 0.1.0
 */
@Slf4j
public class ScrollItemsAction implements Action {

    private final TemplateMatchingService templateService;
    private final ScrollDirection direction;
    private final int amount;
    private BufferedImage beforeScroll;

    /**
     * Create scroll action.
     *
     * @param templateService Template matching service
     * @param direction Scroll direction
     * @param amount Number of scroll notches
     */
    public ScrollItemsAction(
        TemplateMatchingService templateService,
        ScrollDirection direction,
        int amount
    ) {
        this.templateService = templateService;
        this.direction = direction;
        this.amount = amount;
    }

    @Override
    public ActionResult execute(Robot robot) throws Exception {
        log.info("Scrolling {} by {} notches", direction, amount);

        // Capture screen before scroll
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        java.awt.Rectangle screenRect = new java.awt.Rectangle(screenSize);
        beforeScroll = robot.createScreenCapture(screenRect);

        // Perform scroll
        int scrollAmount = amount * direction.getMultiplier();
        robot.mouseWheel(scrollAmount);

        log.debug("Mouse wheel scrolled by {}", scrollAmount);

        // Wait for scroll to complete
        Thread.sleep(300);

        // Capture screen after scroll
        BufferedImage afterScroll = robot.createScreenCapture(screenRect);

        // Compare before and after to see if scroll succeeded
        boolean scrolled = !imagesEqual(beforeScroll, afterScroll);

        if (scrolled) {
            log.debug("Scroll succeeded (screen changed)");
            return ActionResult.SUCCESS;
        } else {
            log.info("Scroll did not change screen (reached end?)");
            return ActionResult.ITEMS_END;
        }
    }

    @Override
    public Action nextAction(ActionResult result) {
        if (result == ActionResult.SUCCESS) {
            // After scrolling, click on items
            return new ClickItemAction(templateService, 0);
        } else if (result == ActionResult.ITEMS_END) {
            // Reached end of items, move to next category
            log.info("Reached end of items list");
            return null; // This would transition back to category selection
        } else {
            // Retry scroll
            return new ScrollItemsAction(templateService, direction, amount);
        }
    }

    @Override
    public String getName() {
        return String.format("ScrollItems[%s, %d]", direction, amount);
    }

    @Override
    public int getTimeout() {
        return 5000;
    }

    /**
     * Compare two images for equality (simplified check).
     *
     * @param img1 First image
     * @param img2 Second image
     * @return true if images appear equal
     */
    private boolean imagesEqual(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            return false;
        }

        // Sample comparison (check a few pixels for performance)
        // In production, might use more sophisticated comparison
        int width = img1.getWidth();
        int height = img1.getHeight();
        int samples = 100; // Check 100 random pixels

        int differentPixels = 0;

        for (int i = 0; i < samples; i++) {
            int x = (int) (Math.random() * width);
            int y = (int) (Math.random() * height);

            if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                differentPixels++;
            }
        }

        // Images are "equal" if less than 10% of sampled pixels differ
        return differentPixels < (samples * 0.1);
    }
}
