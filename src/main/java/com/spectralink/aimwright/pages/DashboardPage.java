package com.spectralink.aimwright.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page object for the Dashboard page.
 *
 * Provides access to dashboard summary cards and navigation.
 */
public class DashboardPage extends BasePage {

    // Dashboard card selectors
    private static final String DEVICES_COUNT = "#dashboard-devices-count";
    private static final String BATTERIES_COUNT = "#dashboard-batteries-count";
    private static final String LOCATIONS_COUNT = "#dashboard-locations-count";
    private static final String GATEWAYS_COUNT = "#dashboard-gateways-count";

    // Chart selectors
    private static final String CALL_PERFORMANCE_CHART = ".chart-doughnut-title:has-text('Call Performance')";
    private static final String DEVICE_STATUS_CHART = ".chart-doughnut-title:has-text('Device Status')";

    public DashboardPage(Page page) {
        super(page);
    }

    /**
     * Navigates to the Dashboard page.
     */
    public void navigateTo() {
        navigation.toDashboard();
        waitForPageLoad();
    }

    // ========== Dashboard Card Values ==========

    /**
     * Gets the devices count from the dashboard card.
     */
    public int getDevicesCount() {
        String text = page.locator(DEVICES_COUNT).innerText().trim();
        return parseCount(text);
    }

    /**
     * Gets the batteries count from the dashboard card.
     */
    public int getBatteriesCount() {
        String text = page.locator(BATTERIES_COUNT).innerText().trim();
        return parseCount(text);
    }

    /**
     * Gets the locations count from the dashboard card.
     */
    public int getLocationsCount() {
        String text = page.locator(LOCATIONS_COUNT).innerText().trim();
        return parseCount(text);
    }

    /**
     * Gets the gateways count from the dashboard card.
     */
    public int getGatewaysCount() {
        String text = page.locator(GATEWAYS_COUNT).innerText().trim();
        return parseCount(text);
    }

    // ========== Dashboard Card Navigation ==========

    /**
     * Clicks the devices card to navigate to devices summary.
     */
    public void clickDevicesCard() {
        page.locator(DEVICES_COUNT).click();
        waitForPageLoad();
    }

    /**
     * Clicks the batteries card to navigate to batteries summary.
     */
    public void clickBatteriesCard() {
        page.locator(BATTERIES_COUNT).click();
        waitForPageLoad();
    }

    /**
     * Clicks the locations card to navigate to locations list.
     */
    public void clickLocationsCard() {
        page.locator(LOCATIONS_COUNT).click();
        waitForPageLoad();
    }

    /**
     * Clicks the gateways card to navigate to gateways summary.
     */
    public void clickGatewaysCard() {
        page.locator(GATEWAYS_COUNT).click();
        waitForPageLoad();
    }

    // ========== Chart Interactions ==========

    /**
     * Checks if the call performance chart is visible.
     */
    public boolean isCallPerformanceChartVisible() {
        return page.locator(CALL_PERFORMANCE_CHART).isVisible();
    }

    /**
     * Checks if the device status chart is visible.
     */
    public boolean isDeviceStatusChartVisible() {
        return page.locator(DEVICE_STATUS_CHART).isVisible();
    }

    // ========== Helper Methods ==========

    private int parseCount(String text) {
        try {
            // Remove any non-numeric characters (commas, etc.)
            return Integer.parseInt(text.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            log.warn("Could not parse count from: {}", text);
            return 0;
        }
    }
}
