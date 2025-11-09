# Template Images

This directory contains template images for GUI automation.

## Required Templates

Create the following template images by capturing screenshots from the Dofus Retro client:

### Category Templates
- **category-unchecked.png** - Unchecked category checkbox (left panel)
- **category-checked.png** - Checked category checkbox (left panel)

### Item Templates
- **item-slot.png** - Item slot in the items list
- **item-empty.png** - Empty item slot (no item)

### Scroll Templates
- **scroll-bar-top.png** - Scroll bar at the top position
- **scroll-bar-bottom.png** - Scroll bar at the bottom position

### UI Elements
- **hdv-window.png** - Main HDV (Auction House) window header
- **close-button.png** - Window close button

## How to Create Templates

1. **Launch Dofus Retro** and open the Auction House (HDV)
2. **Take screenshots** of the UI elements listed above
3. **Crop** each element to:
   - Remove extra background
   - Keep the element centered
   - Maintain consistent size (40x40 to 100x100 pixels recommended)
4. **Save as PNG** with exact names listed above
5. **Place in this directory**

## Template Guidelines

- **Size**: 40x40 to 100x100 pixels (optimal for matching)
- **Format**: PNG with transparency if applicable
- **Quality**: High quality, no compression artifacts
- **Resolution**: Capture at the same resolution you'll be automating
- **Uniqueness**: Include enough context to make matches unique

## Testing Templates

Run the template matching PoC to verify templates:

```bash
mvn test -Dtest=SikuliPoC
```

Or use the template matching service directly:

```java
TemplateMatchingService service = new TemplateMatchingService(config);
BufferedImage screenshot = robot.createScreenCapture(screenRect);
Point location = service.findTemplate("category-unchecked", screenshot);
```

## Multi-Resolution Support

For multi-DPI support, create subdirectories:

- `1x/` - Standard resolution (96 DPI)
- `2x/` - HiDPI/Retina (192 DPI)

The automation service will automatically select appropriate templates based on screen DPI.

## Troubleshooting

**Template not found:**
- Verify template is visible on screen
- Lower similarity threshold in `application.yml`
- Re-capture template at current resolution
- Check template file name matches exactly

**Too many false positives:**
- Increase similarity threshold
- Capture more unique elements
- Include surrounding context
- Use color matching (ensure RGB/BGR correct)
