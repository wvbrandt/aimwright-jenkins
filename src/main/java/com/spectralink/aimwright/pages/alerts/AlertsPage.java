package com.spectralink.aimwright.pages.alerts;

import com.microsoft.playwright.Page;
import com.spectralink.aimwright.common.AmieOptions;
import com.spectralink.aimwright.helpers.MenuHelper;
import com.spectralink.aimwright.helpers.TableHelper;
import com.spectralink.aimwright.pages.BasePage;

import java.util.List;

/**
 * Page object for the Alerts page.
 *
 * Provides access to alert list operations including sorting and filtering.
 */
public class AlertsPage extends BasePage {

    // Page selectors
    private static final String ALERTS_TABLE = ".ant-table";
    private static final String FILTER_BUTTON = "button:has-text('Filter')";
    private static final String EXPORT_BUTTON = "button:has-text('Export')";

    // Filter selectors
    private static final String ALERT_TYPE_FILTER = "#alert_type";
    private static final String SEVERITY_FILTER = "#severity";
    private static final String STATUS_FILTER = "#status";
    private static final String APPLY_FILTER_BUTTON = "button:has-text('Apply')";
    private static final String CLEAR_FILTER_BUTTON = "button:has-text('Clear')";

    // Column names
    public static final String COL_ALERT_TYPE = "ALERT TYPE";
    public static final String COL_SEVERITY = "SEVERITY";
    public static final String COL_STATUS = "STATUS";
    public static final String COL_DEVICE = "DEVICE";
    public static final String COL_LOCATION = "LOCATION";
    public static final String COL_TIMESTAMP = "TIMESTAMP";
    public static final String COL_MESSAGE = "MESSAGE";

    private TableHelper table;

    public AlertsPage(Page page) {
        super(page);
    }

    /**
     * Navigates to the Alerts page.
     */
    public void navigateTo() {
        navigation.toAlerts();
        waitForTableLoad();
        initTable();
    }

    private void initTable() {
        table = new TableHelper(page, ALERTS_TABLE);
    }

    @Override
    public TableHelper getTable() {
        if (table == null) {
            initTable();
        }
        return table;
    }

    // ========== Alert List Operations ==========

    /**
     * Gets the total number of alerts in the table.
     */
    public int getAlertCount() {
        return getTable().getRowCount();
    }

    /**
     * Gets the list of alert types.
     */
    public List<String> getAlertTypes() {
        return getTable().getColumnValues(COL_ALERT_TYPE);
    }

    /**
     * Gets the list of severity values.
     */
    public List<String> getSeverities() {
        return getTable().getColumnValues(COL_SEVERITY);
    }

    /**
     * Gets the list of status values.
     */
    public List<String> getStatuses() {
        return getTable().getColumnValues(COL_STATUS);
    }

    // ========== Sorting Operations ==========

    /**
     * Sorts alerts by alert type.
     */
    public void sortByAlertType(AmieOptions.Sort order) {
        log.debug("Sorting alerts by type: {}", order);
        getTable().sort(COL_ALERT_TYPE, order);
        waitForTableLoad();
    }

    /**
     * Sorts alerts by severity.
     */
    public void sortBySeverity(AmieOptions.Sort order) {
        log.debug("Sorting alerts by severity: {}", order);
        getTable().sort(COL_SEVERITY, order);
        waitForTableLoad();
    }

    /**
     * Sorts alerts by status.
     */
    public void sortByStatus(AmieOptions.Sort order) {
        log.debug("Sorting alerts by status: {}", order);
        getTable().sort(COL_STATUS, order);
        waitForTableLoad();
    }

    /**
     * Sorts alerts by timestamp.
     */
    public void sortByTimestamp(AmieOptions.Sort order) {
        log.debug("Sorting alerts by timestamp: {}", order);
        getTable().sort(COL_TIMESTAMP, order);
        waitForTableLoad();
    }

    /**
     * Sorts alerts by device.
     */
    public void sortByDevice(AmieOptions.Sort order) {
        log.debug("Sorting alerts by device: {}", order);
        getTable().sort(COL_DEVICE, order);
        waitForTableLoad();
    }

    // ========== Filter Operations ==========

    /**
     * Opens the filter panel.
     */
    public void openFilters() {
        page.locator(FILTER_BUTTON).click();
    }

    /**
     * Filters alerts by type.
     */
    public void filterByAlertType(String alertType) {
        openFilters();
        new MenuHelper(page, ALERT_TYPE_FILTER, true).select(alertType);
        applyFilters();
    }

    /**
     * Filters alerts by severity.
     */
    public void filterBySeverity(String severity) {
        openFilters();
        new MenuHelper(page, SEVERITY_FILTER, true).select(severity);
        applyFilters();
    }

    /**
     * Filters alerts by status.
     */
    public void filterByStatus(String status) {
        openFilters();
        new MenuHelper(page, STATUS_FILTER, true).select(status);
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

    // ========== Export ==========

    public void clickExport() {
        page.locator(EXPORT_BUTTON).click();
    }
}
