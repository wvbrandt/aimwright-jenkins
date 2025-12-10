package com.spectralink.aimwright.helpers;

import ch.qos.logback.classic.Logger;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.spectralink.aimwright.common.Defaults;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

/**
 * Playwright helper for navigating the AMiE application.
 * Provides methods for menu navigation and entity selection.
 *
 * Uses Playwright's built-in auto-wait - no explicit sleeps needed.
 */
public class NavigationHelper {
    private static final Logger log = (Logger) LoggerFactory.getLogger(NavigationHelper.class.getName());

    private final Page page;

    public NavigationHelper(Page page) {
        this.page = page;
    }

    // ========== Main Navigation Methods ==========

    /**
     * Navigates to the Dashboard page.
     */
    public void toDashboard() {
        page.locator("#menu_dashboard").click();
        page.waitForURL("**/" );
    }

    /**
     * Navigates to the Devices Summary page.
     */
    public void toDevicesSummary() {
        page.locator("#menu_device").click();
        page.waitForURL("**/devices/summary");
        assertMenuSelected("#menu_device");
    }

    /**
     * Navigates to the Batteries Summary page.
     */
    public void toBatteriesSummary() {
        page.locator("#menu_battery").click();
        page.waitForURL("**/batteries/summary");
        assertMenuSelected("#menu_battery");
    }

    /**
     * Navigates to the Gateways page.
     */
    public void toGatewaysSummary() {
        page.locator("#menu_gateways").click();
        page.waitForURL("**/gateways");
        assertMenuSelected("#menu_gateways");
    }

    /**
     * Navigates to the Locations page.
     */
    public void toLocationList() {
        page.locator("#menu_locations").click();
        page.waitForURL("**/locations");
        assertMenuSelected("#menu_locations");
    }

    /**
     * Navigates to the Alerts page.
     */
    public void toAlerts() {
        page.locator("#menu_alert").click();
        page.waitForURL("**/alerts");
        assertMenuSelected("#menu_alert");
    }

    /**
     * Navigates to the Performance page.
     */
    public void toPerformance() {
        page.locator("#menu_performance").click();
        page.waitForURL("**/performance");
        assertMenuSelected("#menu_performance");
    }

    /**
     * Navigates to the Organizations page.
     */
    public void toOrganizations() {
        page.locator("#menu_organizations").click();
        page.waitForURL("**/organizations");
        assertMenuSelected("#menu_organizations");
    }

    // ========== Administration Navigation ==========

    /**
     * Administration page definitions.
     */
    private enum AdministrationPages {
        LICENSES("Licenses", "/administration/licenses", "menu_administration.license"),
        DEVICES_BATTERIES("Devices/Batteries", "/administration/devices-batteries/device-retirement-history", "menu_administration.devicesbatteries"),
        USERS("Users", "/administration/users", "menu_administration.admin-users"),
        DATA_RETENTION("Data Retention", "/administration/data-archive", "menu_administration.data-retention"),
        TIMEZONE("Timezone", "/administration/timezone", "menu_administration.timezone"),
        ALERTS("Alerts", "/administration/alerts", "menu_administration.alerts");

        final String linkText;
        final String endpoint;
        final String id;

        AdministrationPages(String linkText, String endpoint, String id) {
            this.linkText = linkText;
            this.endpoint = endpoint;
            this.id = id;
        }
    }

    private AdministrationPages getAdministrationPage(String pageName) {
        for (AdministrationPages page : AdministrationPages.values()) {
            if (page.linkText.equals(pageName)) return page;
        }
        log.error("Administration page '{}' does not exist", pageName);
        return null;
    }

    /**
     * Navigates to an administration section.
     *
     * @param section One of: "Licenses", "Devices/Batteries", "Users", "Data Retention", "Timezone", "Alerts"
     */
    public void toAdministration(String section) {
        AdministrationPages adminPage = getAdministrationPage(section);
        if (adminPage != null) {
            page.locator("#menu_administration").click();
            page.locator("#" + adminPage.id).click();
            page.waitForURL("**" + adminPage.endpoint);
        }
    }

    // ========== Tab Selection ==========

    /**
     * Selects a tab by its text content.
     */
    public void selectTab(String tabName) {
        Locator tabs = page.locator(".ant-tabs-tab");
        Locator targetTab = tabs.filter(new Locator.FilterOptions().setHasText(tabName));

        if (targetTab.count() > 0) {
            targetTab.first().click();
            log.debug("Selected tab: {}", tabName);
        } else {
            log.error("Tab '{}' not found", tabName);
        }
    }

    // ========== Entity Selection ==========

    /**
     * Selects an account from the accounts table by name.
     */
    public void selectAccount(String accountName) {
        TableHelper table = new TableHelper(page);
        Locator cell = table.getCellWithValue(accountName, "Account Name");
        if (cell != null) {
            cell.click();
        } else {
            log.error("Account '{}' not found in table", accountName);
        }
    }

    /**
     * Selects an organization from the table by name.
     */
    public void selectOrg(String orgName) {
        TableHelper table = new TableHelper(page);
        Locator cell = table.getCellWithValue(orgName, "Organization Name");
        if (cell != null) {
            cell.click();
            // Wait for org context to load
            page.waitForLoadState();
        } else {
            log.error("Organization '{}' not found in table", orgName);
        }
    }

    /**
     * Selects a location from the header location dropdown.
     */
    public void selectLocation(String locationName) {
        MenuHelper menu = new MenuHelper(page, "#header-location", true);
        menu.select(locationName);
    }

    /**
     * Selects a device from the devices table by serial number.
     */
    public void selectDevice(String deviceSerial) {
        TableHelper table = new TableHelper(page);
        Locator cell = table.getCellWithValue(deviceSerial, "SERIAL #");
        if (cell != null) {
            cell.click();
            page.waitForLoadState();
        } else {
            log.error("Device '{}' not found in table", deviceSerial);
        }
    }

    /**
     * Selects a battery from the batteries table by serial number.
     */
    public void selectBattery(String batterySerial) {
        TableHelper table = new TableHelper(page);
        Locator cell = table.getCellWithValue(batterySerial, "BATTERY SERIAL");
        if (cell != null) {
            cell.click();
            page.waitForLoadState();
        } else {
            log.error("Battery '{}' not found in table", batterySerial);
        }
    }

    /**
     * Selects a gateway from the gateways table by name.
     */
    public void selectGateway(String gatewayName) {
        TableHelper table = new TableHelper(page);
        Locator cell = table.getCellWithValue(gatewayName, "Gateway name");
        if (cell != null) {
            cell.click();
            page.waitForLoadState();
        } else {
            log.error("Gateway '{}' not found in table", gatewayName);
        }
    }

    // ========== Helper Methods ==========

    /**
     * Asserts that a menu item is selected (has 'selected' class).
     */
    private void assertMenuSelected(String menuSelector) {
        String className = page.locator(menuSelector).getAttribute("class");
        Assert.assertTrue(className != null && className.contains("selected"),
                "Menu item " + menuSelector + " should be selected");
    }

    /**
     * Returns the current page URL.
     */
    public String getCurrentUrl() {
        return page.url();
    }

    /**
     * Verifies the current URL contains the expected endpoint.
     */
    public void verifyOnPage(String endpoint) {
        String expectedUrl = Defaults.getUiInstance() + endpoint;
        Assert.assertEquals(page.url(), expectedUrl, "Expected to be on page: " + endpoint);
    }
}
