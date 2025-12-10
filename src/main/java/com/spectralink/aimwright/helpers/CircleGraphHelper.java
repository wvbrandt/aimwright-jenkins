package com.spectralink.aimwright.helpers;

import ch.qos.logback.classic.Logger;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;

/**
 * Playwright helper for interacting with doughnut/pie chart visualizations.
 * Handles clicking chart segments by legend color matching.
 *
 * Uses canvas image analysis to find clickable areas for chart segments.
 */
public class CircleGraphHelper {
    private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());

    private final Page page;
    private final Locator chartWrapper;
    private final Locator canvas;
    private final String chartTitle;

    /**
     * Creates a CircleGraphHelper for a chart with the specified title.
     *
     * @param page The Playwright page
     * @param chartTitle The title text displayed on the chart
     */
    public CircleGraphHelper(Page page, String chartTitle) {
        this.page = page;
        this.chartTitle = chartTitle;

        // Find the chart wrapper by title
        this.chartWrapper = page.locator(
                "//*[text() = '" + chartTitle + "' and @class = 'chart-doughnut-title']/parent::div"
        );

        // Wait for chart animation to complete
        page.waitForTimeout(4000);

        // Find the canvas element within the chart
        this.canvas = chartWrapper.locator("canvas");

        if (canvas.count() == 0) {
            log.error("Canvas element not found for chart: {}", chartTitle);
        }
    }

    /**
     * Clicks on a chart segment by its legend item name.
     * Uses pixel color matching to find the segment.
     *
     * @param chartItem The legend item text to click
     */
    public void clickChartOption(String chartItem) {
        int[] itemColor = getLegendItemRgbColor(chartItem);
        if (itemColor == null) {
            log.error("Could not find color for legend item: {}", chartItem);
            return;
        }

        int[] coordinates = getCanvasCoordinates(itemColor[0], itemColor[1], itemColor[2]);
        if (coordinates != null) {
            // Get canvas dimensions for positioning
            Locator.BoundingBoxOptions options = new Locator.BoundingBoxOptions();
            var box = canvas.boundingBox();

            if (box != null) {
                // Click at the found coordinates relative to the canvas
                log.debug("Clicking {} canvas at point: [{}, {}] for item {}",
                        chartTitle, coordinates[0], coordinates[1], chartItem);

                // Calculate absolute position
                double absoluteX = box.x + coordinates[0];
                double absoluteY = box.y + coordinates[1];

                page.mouse().click(absoluteX, absoluteY);
            } else {
                log.error("Could not get canvas bounding box");
            }
        } else {
            log.error("Could not find coordinates for color in chart");
        }
    }

    /**
     * Extracts the RGB color of a legend item.
     *
     * @param legendItem The legend item text
     * @return Array of [red, green, blue] values, or null if not found
     */
    private int[] getLegendItemRgbColor(String legendItem) {
        try {
            // Find the color span preceding the legend text
            Locator legendColor = chartWrapper.locator(
                    "//ul[@class = 'chart-legend']/li/span[text() = '" + legendItem + "']/preceding-sibling::span"
            );

            if (legendColor.count() == 0) {
                log.warn("Legend item '{}' not found", legendItem);
                return null;
            }

            String style = legendColor.getAttribute("style");
            if (style == null || !style.contains("rgb")) {
                log.warn("No RGB color found in style for legend item: {}", legendItem);
                return null;
            }

            // Parse rgb(r, g, b) from style
            String rgb = style.substring(style.lastIndexOf("(") + 1, style.lastIndexOf(")"));
            String[] parts = rgb.split(",");

            int red = Integer.parseInt(parts[0].trim());
            int green = Integer.parseInt(parts[1].trim());
            int blue = Integer.parseInt(parts[2].trim());

            log.trace("RGB color of {} is {} {} {}", legendItem, red, green, blue);
            return new int[]{red, green, blue};

        } catch (Exception e) {
            log.error("Error extracting legend color: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Converts a generic Image to BufferedImage.
     */
    private BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        BufferedImage bimage = new BufferedImage(
                img.getWidth(null),
                img.getHeight(null),
                BufferedImage.TYPE_INT_RGB
        );
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bimage;
    }

    /**
     * Scans the canvas image to find coordinates matching the expected color.
     * Looks for a pixel where the color matches and is surrounded by same-colored pixels.
     *
     * @param expectedRed Expected red component (0-255)
     * @param expectedGreen Expected green component (0-255)
     * @param expectedBlue Expected blue component (0-255)
     * @return Array of [x, y] coordinates, or null if not found
     */
    private int[] getCanvasCoordinates(int expectedRed, int expectedGreen, int expectedBlue) {
        try {
            // Get canvas dimensions
            String heightStyle = canvas.evaluate("el => getComputedStyle(el).height").toString();
            String widthStyle = canvas.evaluate("el => getComputedStyle(el).width").toString();

            int canvasHeight = Integer.parseInt(heightStyle.replace("px", ""));
            int canvasWidth = Integer.parseInt(widthStyle.replace("px", ""));

            // Extract canvas as base64 image using JavaScript
            String base64Image = (String) canvas.evaluate(
                    "el => el.toDataURL().substring(22)"
            );

            // Decode the base64 image
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
            BufferedImage bufferedImage = ImageIO.read(byteArrayInputStream);

            // Scale image to match display dimensions
            Image scaledImage = bufferedImage.getScaledInstance(canvasWidth, canvasHeight, Image.SCALE_SMOOTH);
            BufferedImage scaledBuffered = toBufferedImage(scaledImage);

            int height = scaledBuffered.getHeight();
            int width = scaledBuffered.getWidth();
            log.trace("Canvas width: {}, height: {}", width, height);

            // Scan for matching pixel with surrounding pixels of same color
            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < width - 1; x++) {
                    int RGBA = scaledBuffered.getRGB(x, y);
                    int red = (RGBA >> 16) & 255;
                    int green = (RGBA >> 8) & 255;
                    int blue = RGBA & 255;

                    // Check if this pixel matches the expected color
                    if (expectedRed == red && expectedGreen == green && expectedBlue == blue) {
                        // Verify surrounding pixels are the same (not an edge pixel)
                        if (RGBA == scaledBuffered.getRGB(x - 1, y) &&
                                RGBA == scaledBuffered.getRGB(x, y - 1) &&
                                RGBA == scaledBuffered.getRGB(x + 1, y) &&
                                RGBA == scaledBuffered.getRGB(x, y + 1)) {
                            return new int[]{x, y};
                        }
                    }
                }
            }

            log.warn("Could not find matching color in canvas");
            return null;

        } catch (Exception e) {
            log.error("Error scanning canvas: {}", e.getMessage(), e);
            return null;
        }
    }
}
