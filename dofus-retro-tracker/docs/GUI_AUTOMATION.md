# GUI Automation Setup

## Overview

The GUI Automation module provides cross-platform automation capabilities for interacting with the Dofus Retro client. It combines Java Robot API for mouse/keyboard control with JavaCV/SikuliX for image recognition.

## Technologies

### Java Robot API
- **Purpose**: Mouse/keyboard control, screen capture
- **Package**: `java.awt.Robot`
- **Capabilities**:
  - Mouse movement and clicking
  - Keyboard input simulation
  - Screen capture (full screen or regions)
  - Pixel color detection
  - Auto-delay between operations

### SikuliX
- **Purpose**: Template matching, image recognition
- **Package**: `org.sikuli.script`
- **Capabilities**:
  - Find UI elements by image
  - Click on matched regions
  - OCR text recognition
  - Visual feedback (highlighting)

### JavaCV (OpenCV)
- **Purpose**: Advanced image processing
- **Package**: `org.bytedeco.opencv`
- **Capabilities**:
  - Template matching algorithms
  - Image filtering and transformation
  - Multi-scale detection
  - Custom similarity thresholds

## Architecture

```
┌─────────────────────────────────────────┐
│  AuctionHouseAutomationService          │
│  (Main orchestrator)                    │
└────────────┬────────────────────────────┘
             │
     ┌───────┴───────┐
     │               │
┌────▼────┐    ┌────▼──────────────────┐
│  Robot  │    │ TemplateMatchingService│
│ (AWT)   │    │ (JavaCV/SikuliX)       │
└─────────┘    └───────────────────────┘
     │                  │
     └────────┬─────────┘
              │
     ┌────────▼────────┐
     │ Action State    │
     │ Machine         │
     └─────────────────┘
              │
     ┌────────▼────────┐
     │ Action Impls    │
     │ (Click, Scroll, │
     │  Wait, etc.)    │
     └─────────────────┘
```

## Coordinate Systems

### General Principles
- **Origin**: Top-left corner (0, 0) on all platforms
- **X-axis**: Increases from left to right
- **Y-axis**: Increases from top to bottom

### Windows
- **Origin**: Top-left (0, 0)
- **DPI Scaling**:
  - Windows 10/11 supports DPI scaling (100%, 125%, 150%, 200%)
  - Robot API may need coordinate scaling
  - Use `Toolkit.getDefaultToolkit().getScreenResolution()` to detect DPI
  - Default: 96 DPI (100%), Scaled: 120 DPI (125%), 144 DPI (150%), etc.
- **Multi-monitor**: Each monitor has its own coordinate space
  - Primary monitor: starts at (0, 0)
  - Secondary monitors: offset based on position

### Linux
- **Origin**: Top-left (0, 0)
- **Display Server Considerations**:
  - **X11**: Full Robot API support
  - **Wayland**: Limited support (security restrictions)
    - May need `XWayland` for compatibility
    - Robot may fail on pure Wayland sessions
- **DPI Scaling**: Varies by desktop environment
  - GNOME: Integer scaling (1x, 2x)
  - KDE: Fractional scaling supported
  - Use `GDK_SCALE` and `GDK_DPI_SCALE` environment variables
- **Testing**: Verify with `xdpyinfo` or `xrandr` commands

### macOS
- **Origin**: Top-left (0, 0)
- **Retina Displays**:
  - 2x scaling factor (HiDPI)
  - Robot coordinates are in logical pixels
  - Screen capture returns @2x images (double resolution)
  - Use `GraphicsConfiguration.getDefaultTransform()` to detect scaling
- **Accessibility**:
  - Requires "Accessibility" permission in System Preferences
  - Grant permission to Java/IDE before running automation
- **Multi-display**: Similar to Windows, offset coordinate spaces

## Template Matching Tips

### 1. Capture Templates at Target Resolution
- Capture templates on the same screen resolution you'll be automating
- For multi-DPI support, create multiple template sets:
  - `templates/1x/` for standard displays
  - `templates/2x/` for Retina/HiDPI displays
- Use platform-specific templates if UI rendering differs

### 2. Use High-Contrast UI Elements
- **Good candidates**:
  - Icons with distinct shapes
  - Buttons with clear borders
  - Checkboxes (checked/unchecked states)
  - Unique UI symbols
- **Avoid**:
  - Plain text (use OCR instead)
  - Gradients or shadows
  - Elements that change color/state frequently

### 3. Template Size Recommendations
- **Minimum**: 20x20 pixels (too small = false positives)
- **Optimal**: 40x40 to 100x100 pixels
- **Maximum**: 200x200 pixels (too large = slow matching)
- Include enough context without too much background

### 4. Similarity Threshold
- **Default**: 0.8 (80% match)
- **High precision**: 0.9-0.95 (fewer false positives, may miss slight variations)
- **More lenient**: 0.7-0.75 (more matches, risk of false positives)
- **Dynamic**: Adjust per template based on testing

### 5. Multi-Template Strategy
For critical UI elements, create multiple templates:
- Normal state
- Hover state
- Selected state
- Different lighting conditions
- Try each template until one matches

## Screen Regions

Define specific regions to search instead of full screen for performance:

```java
// Example: Search only in left panel for categories
Rectangle categoryRegion = new Rectangle(0, 100, 300, 600);
BufferedImage regionCapture = robot.createScreenCapture(categoryRegion);
```

## Troubleshooting

### Robot API Issues

#### Mouse/Keyboard Not Working
- **Linux/Wayland**: Use X11 session instead
- **macOS**: Grant Accessibility permissions
  - System Preferences → Security & Privacy → Privacy → Accessibility
  - Add your Java executable or IDE
- **All platforms**: Check if another automation tool has grabbed input

#### Screen Capture Returns Black Images
- **Linux**: May be Wayland restriction
- **Multi-GPU systems**: Specify which GPU/screen to capture
- **Headless systems**: Won't work (requires display)

#### Coordinates Are Wrong
- **HiDPI displays**: Apply scaling factor
- **Multi-monitor**: Verify screen bounds with `GraphicsDevice.getDefaultConfiguration().getBounds()`
- **Different DPI settings**: Normalize coordinates

### Template Matching Issues

#### Template Not Found
1. **Verify template is visible**: Take a screenshot and compare
2. **Check similarity threshold**: Lower it temporarily (e.g., 0.6)
3. **Inspect template quality**: Re-capture at current resolution
4. **Region too small**: Expand search region
5. **UI changed**: Update template image

#### Too Many False Positives
1. **Increase similarity threshold**: Try 0.9 or higher
2. **Capture more context**: Include unique surrounding elements
3. **Use multiple verification**: Check for nearby elements too
4. **Color matching**: Ensure color mode matches (RGB vs BGR)

#### Matching Is Slow
1. **Reduce search region**: Don't search entire screen
2. **Use smaller templates**: Keep under 100x100 pixels
3. **Multi-threading**: Search multiple templates in parallel
4. **Caching**: Cache screen captures when checking multiple templates
5. **Downscale**: Match on downscaled images for initial detection

### SikuliX Specific Issues

#### `FindFailed` Exception
- Template doesn't exist on screen
- Similarity threshold too high
- Template resolution mismatch
- Use `exists()` instead of `find()` to avoid exceptions

#### Slow Performance
- SikuliX includes overhead for AI/ML features
- For simple matching, use JavaCV directly (faster)
- Reduce region size
- Cache `Screen` instances

## Performance Optimization

### Batch Operations
```java
// Good: Capture once, match multiple
BufferedImage screen = robot.createScreenCapture(screenRect);
Point match1 = templateService.findTemplate("template1", screen);
Point match2 = templateService.findTemplate("template2", screen);

// Bad: Capture for each match
Point match1 = templateService.findTemplate("template1"); // captures internally
Point match2 = templateService.findTemplate("template2"); // captures again
```

### Pre-load Templates
Load all template images at startup, not during matching.

### Use ROI (Region of Interest)
Narrow down search areas based on UI layout knowledge.

### Parallel Matching
Use multiple threads to check different templates simultaneously.

## Safety Features

### Failsafe
Implement emergency stop mechanism:
- Move mouse to screen corner to abort automation
- Monitor for specific key press (e.g., ESC)
- Timeout after N failed actions

### Rate Limiting
Add delays between actions to:
- Avoid overwhelming the game client
- Appear more human-like
- Give UI time to update

### Validation
Always verify action results:
- Check if click registered (UI changed)
- Verify scroll succeeded (content moved)
- Confirm expected elements appear

## Testing

### Manual Testing
Run PoC classes to verify platform compatibility:
```bash
mvn test -Dtest=RobotPoC
mvn test -Dtest=SikuliPoC
```

### Automated Testing
Use `MockRobot` for unit tests without actual automation.

### Integration Testing
Test against a test UI or sandboxed game environment.

## References

- [Java Robot API Documentation](https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/java/awt/Robot.html)
- [SikuliX Documentation](http://sikulix-2014.readthedocs.io/)
- [JavaCV/OpenCV Guide](https://github.com/bytedeco/javacv)
- [Template Matching Theory](https://docs.opencv.org/master/d4/dc6/tutorial_py_template_matching.html)

## Future Enhancements

- **OCR Integration**: Read text from UI (Tesseract)
- **AI-based Element Detection**: Use YOLO/TensorFlow for robust detection
- **Recorder**: Record user actions to generate automation scripts
- **Visual Debugger**: Highlight matched regions in real-time
- **Cloud Templates**: Centralized template repository with versioning
