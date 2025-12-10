package com.spectralink.aimwright.pages.gateways;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.spectralink.aimwright.common.AmieOptions;
import com.spectralink.aimwright.helpers.MenuHelper;
import com.spectralink.aimwright.helpers.TableHelper;
import com.spectralink.aimwright.pages.BasePage;

import java.util.List;

/**
 * Page object for the Gateways Summary page.
 *
 * Provides access to gateway list operations including sorting and filtering.
 */
public class GatewaysSummaryPage extends BasePage {

    // Page selectors
    private static final String GATEWAYS_TABLE = ".ant-table";
    private static final String FILTER_BUTTON = "button:has-text('Filter')";

    // Filter selectors
    private static final String HEALTH_FILTER = "#health";
    private static final String APPLY_FILTER_BUTTON = "button:has-text('Apply')";
    private static final String CLEAR_FILTER_BUTTON = "button:has-text('Clear')";

    // Column names
    public static final String COL_GATEWAY_NAME = "Gateway name";
    public static final String COL_LOCATION = "Location";
    public static final String COL_HEALTH = "Health";
    public static final String COL_IP_ADDRESS = "IP Address";
    public static final String COL_VERSION = "Version";
    public static final String COL_DEVICES = "Devices";
    public static final String COL_LAST_CHECK_IN = "Last Check-In";

    private TableHelper table;

    public GatewaysSummaryPage(Page page) {
        super(page);
    }

    /**
     * Navigates to the Gateways Summary page.
     */
    public void navigateTo() {
        navigation.toGatewaysSummary();
        waitForTableLoad();
        initTable();
    }

    private void initTable() {
        table = new TableHelper(page, GATEWAYS_TABLE);
    }

    @Override
    public TableHelper getTable() {
        if (table == null) {
            initTable();
        }
        return table;
    }

    // ========== Gateway List Operations ==========

    /**
     * Gets the total number of gateways in the table.
     */
    public int getGatewayCount() {
        return getTable().getRowCount();
    }

    /**
     * Gets the list of gateway names.
     */
    public List<String> getGatewayNames() {
        return getTable().getColumnValues(COL_GATEWAY_NAME);
    }

    /**
     * Gets the list of locations.
     */
    public List<String> getLocations() {
        return getTable().getColumnValues(COL_LOCATION);
    }

    /**
     * Gets the list of health values.
     */
    public List<String> getHealthValues() {
        return getTable().getColumnValues(COL_HEALTH);
    }

    // ========== Sorting Operations ==========

    /**
     * Sorts gateways by name.
     */
    public void sortByName(AmieOptions.Sort order) {
        log.debug("Sorting gateways by name: {}", order);
        getTable().sort(COL_GATEWAY_NAME, order);
        waitForTableLoad();
    }

    /**
     * Sorts gateways by location.
     */
    public void sortByLocation(AmieOptions.Sort order) {
        log.debug("Sorting gateways by location: {}", order);
        getTable().sort(COL_LOCATION, order);
        waitForTableLoad();
    }

    /**
     * Sorts gateways by health.
     */
    public void sortByHealth(AmieOptions.Sort order) {
        log.debug("Sorting gateways by health: {}", order);
        getTable().sort(COL_HEALTH, order);
        waitForTableLoad();
    }

    /**
     * Sorts gateways by device count.
     */
    public void sortByDevices(AmieOptions.Sort order) {
        log.debug("Sorting gateways by devices: {}", order);
        getTable().sort(COL_DEVICES, order);
        waitForTableLoad();
    }

    /**
     * Sorts gateways by last check-in.
     */
    public void sortByLastCheckIn(AmieOptions.Sort order) {
        log.debug("Sorting gateways by last check-in: {}", order);
        getTable().sort(COL_LAST_CHECK_IN, order);
        waitForTableLoad();
    }

    // ========== Gateway Selection ==========

    /**
     * Clicks on a gateway by name to open details.
     */
    public void selectGateway(String gatewayName) {
        log.debug("Selecting gateway: {}", gatewayName);
        Locator cell = getTable().getCellWithValue(gatewayName, COL_GATEWAY_NAME);
        if (cell != null) {
            cell.click();
            waitForPageLoad();
        } else {
            log.error("Gateway {} not found", gatewayName);
        }
    }

    // ========== Filter Operations ==========

    /**
     * Opens the filter panel.
     */
    public void openFilters() {
        page.locator(FILTER_BUTTON).click();
    }

    /**
     * Filters gateways by health status.
     */
    public void filterByHealth(String health) {
        openFilters();
        new MenuHelper(page, HEALTH_FILTER, true).select(health);
        applyFilters();
    }

    /**
     * Applies the current filters.
     */
    public void applyFilters() {
        page.locator(APPLY_FILTER_BUTTON).click();
        waitForTableLoad();
    }

    /**
     * Clears all filters.
     */
    public void clearFilters() {
        page.locator(CLEAR_FILTER_BUTTON).click();
        waitForTableLoad();
    }
}
