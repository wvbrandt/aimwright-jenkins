package com.spectralink.aimwright.helpers;

import ch.qos.logback.classic.Logger;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Playwright helper for interacting with Ant Design select/dropdown menus.
 * Handles expanding options, selection by text/index, and value retrieval.
 *
 * Uses Playwright's Locator API with built-in auto-wait.
 */
public class MenuHelper {
    private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());

    private final Page page;
    private Locator menuElement;
    private Locator optionsCanvas = null;

    /**
     * Creates a MenuHelper by finding the menu following a label text.
     * Finds the ant-select sibling of a span containing the label.
     */
    public MenuHelper(Page page, String menuLabel) {
        this.page = page;
        // Find the menu element following the label
        Locator labelElement = page.locator("//span[text() = '" + menuLabel + "']/following-sibling::div");

        // Ensure we have the ant-select element
        if (labelElement.locator(".ant-select").count() > 0) {
            this.menuElement = labelElement.locator(".ant-select").first();
        } else if (labelElement.getAttribute("class") != null &&
                   labelElement.getAttribute("class").contains("ant-select")) {
            this.menuElement = labelElement;
        } else {
            this.menuElement = labelElement;
        }
    }

    /**
     * Creates a MenuHelper for a menu matching the given CSS selector.
     */
    public MenuHelper(Page page, String selector, boolean isSelector) {
        this.page = page;
        Locator element = page.locator(selector);

        // Ensure we have the ant-select element
        String className = element.getAttribute("class");
        if (className != null && className.contains("ant-select")) {
            this.menuElement = element;
        } else if (element.locator(".ant-select").count() > 0) {
            this.menuElement = element.locator(".ant-select").first();
        } else {
            this.menuElement = element;
        }
    }

    /**
     * Creates a MenuHelper for an existing Locator.
     */
    public MenuHelper(Page page, Locator menuLocator) {
        this.page = page;

        String className = menuLocator.getAttribute("class");
        if (className != null && className.contains("ant-select")) {
            this.menuElement = menuLocator;
        } else if (menuLocator.locator(".ant-select").count() > 0) {
            this.menuElement = menuLocator.locator(".ant-select").first();
        } else {
            this.menuElement = menuLocator;
        }
    }

    /**
     * Expands the dropdown options by clicking the menu.
     */
    private void expandOptions() {
        menuElement.click();

        // Find the dropdown canvas using aria-controls attribute
        Locator selection = menuElement.locator(".ant-select-selection");
        String canvasId = selection.getAttribute("aria-controls");
        log.trace("Menu options canvas id: {}", canvasId);

        if (canvasId != null && !canvasId.isEmpty()) {
            optionsCanvas = page.locator("#" + canvasId);
        } else {
            // Fallback to finding the open dropdown
            optionsCanvas = page.locator(".ant-select-dropdown:not(.ant-select-dropdown-hidden)");
        }
    }

    /**
     * Returns the list of option elements.
     */
    private Locator getOptionElements() {
        if (optionsCanvas == null) {
            log.error("Options canvas not properly initialized - call expandOptions first");
            return null;
        }
        return optionsCanvas.locator(".ant-select-dropdown-menu-item");
    }

    /**
     * Returns the text values of all options.
     */
    private List<String> getOptionValues() {
        List<String> optionValues = new ArrayList<>();
        Locator options = getOptionElements();

        if (options != null) {
            optionValues.addAll(options.allTextContents());
        }
        log.debug("Menu options: {}", optionValues);
        return optionValues;
    }

    /**
     * Selects an option by its index.
     */
    private void selectOption(int index) {
        Locator options = getOptionElements();
        if (options == null) return;

        int count = options.count();
        if (index >= 0 && index < count) {
            Locator option = options.nth(index);
            option.scrollIntoViewIfNeeded();
            option.click();
        } else {
            log.error("Option index {} exceeds list size {}", index, count);
        }
    }

    /**
     * Returns the number of available options.
     */
    public int getOptionsCount() {
        expandOptions();
        Locator options = getOptionElements();
        return options != null ? options.count() : 0;
    }

    /**
     * Selects an option by its text value.
     */
    public void select(String optionValue) {
        expandOptions();
        List<String> values = getOptionValues();
        int index = values.indexOf(optionValue);

        if (index >= 0) {
            selectOption(index);
        } else {
            log.error("Option '{}' not found in menu", optionValue);
        }
    }

    /**
     * Selects an option by its index (0-based).
     */
    public void select(int optionIndex) {
        expandOptions();
        selectOption(optionIndex);
    }

    /**
     * Types a value into a searchable dropdown and selects the first result.
     */
    public void typeAndSelect(String value) {
        expandOptions();

        // Type into the menu's input
        Locator input = menuElement.locator("input").first();
        if (input.count() > 0) {
            input.fill(value);
        } else {
            menuElement.pressSequentially(value);
        }

        // Select the first option
        selectOption(0);
    }

    /**
     * Returns all available options as a list of strings.
     */
    public List<String> options() {
        expandOptions();
        return getOptionValues();
    }

    /**
     * Returns the currently selected value.
     */
    public String currentValue() {
        Locator rendered = menuElement.locator(".ant-select-selection__rendered");
        if (rendered.count() > 0) {
            return rendered.textContent().trim();
        }

        // Fallback for different ant-select versions
        Locator selected = menuElement.locator(".ant-select-selection-item");
        if (selected.count() > 0) {
            return selected.textContent().trim();
        }

        return "";
    }
}
