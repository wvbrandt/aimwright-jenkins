package com.spectralink.aimwright.pages.devices;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.spectralink.aimwright.common.AmieOptions;
import com.spectralink.aimwright.helpers.TableHelper;
import com.spectralink.aimwright.pages.BasePage;

import java.util.List;

/**
 * Page object for the Devices Summary page.
 *
 * Provides access to device list table operations including sorting and filtering.
 */
public class DevicesSummaryPage extends BasePage {

    // Page selectors
    private static final String DEVICES_TABLE = ".ant-table";
    private static final String EXPORT_BUTTON = "button:has-text('Export')";
    private static final String FILTER_BUTTON = "button:has-text('Filter')";

    // Column names
    public static final String COL_MODEL = "MODEL";
    public static final String COL_NAME = "NAME";
    public static final String COL_SERIAL = "SERIAL #";
    public static final String COL_SOFTWARE = "SPECTRALINK SOFTWARE";
    public static final String COL_SIP_EXT = "SIP EXT";
    public static final String COL_LAST_CHECK_IN = "LAST CHECK-IN";
    public static final String COL_LOCATION = "LOCATION";

    private TableHelper table;

    public DevicesSummaryPage(Page page) {
        super(page);
    }

    /**
     * Navigates to the Devices Summary page.
     */
    public void navigateTo() {
        navigation.toDevicesSummary();
        waitForTableLoad();
        initTable();
    }

    private void initTable() {
        table = new TableHelper(page, DEVICES_TABLE);
    }

    /**
     * Returns the table helper for direct table operations.
     */
    @Override
    public TableHelper getTable() {
        if (table == null) {
            initTable();
        }
        return table;
    }

    // ========== Device List Operations ==========

    /**
     * Gets the total number of devices in the table.
     */
    public int getDeviceCount() {
        return getTable().getRowCount();
    }

    /**
     * Gets the list of device serial numbers.
     */
    public List<String> getSerialNumbers() {
        return getTable().getColumnValues(COL_SERIAL);
    }

    /**
     * Gets the list of device models.
     */
    public List<String> getDeviceModels() {
        return getTable().getColumnValues(COL_MODEL);
    }

    /**
     * Gets the list of device names.
     */
    public List<String> getDeviceNames() {
        return getTable().getColumnValues(COL_NAME);
    }

    /**
     * Gets the software versions for all devices.
     */
    public List<String> getSoftwareVersions() {
        return getTable().getColumnValues(COL_SOFTWARE);
    }

    // ========== Sorting Operations ==========

    /**
     * Sorts devices by model.
     */
    public void sortByModel(AmieOptions.Sort order) {
        log.debug("Sorting devices by model: {}", order);
        getTable().sort(COL_MODEL, order);
        waitForTableLoad();
    }

    /**
     * Sorts devices by name.
     */
    public void sortByName(AmieOptions.Sort order) {
        log.debug("Sorting devices by name: {}", order);
        getTable().sort(COL_NAME, order);
        waitForTableLoad();
    }

    /**
     * Sorts devices by serial number.
     */
    public void sortBySerial(AmieOptions.Sort order) {
        log.debug("Sorting devices by serial: {}", order);
        getTable().sort(COL_SERIAL, order);
        waitForTableLoad();
    }

    /**
     * Sorts devices by software version.
     */
    public void sortBySoftware(AmieOptions.Sort order) {
        log.debug("Sorting devices by software: {}", order);
        getTable().sort(COL_SOFTWARE, order);
        waitForTableLoad();
    }

    /**
     * Sorts devices by SIP extension.
     */
    public void sortBySipExt(AmieOptions.Sort order) {
        log.debug("Sorting devices by SIP ext: {}", order);
        getTable().sort(COL_SIP_EXT, order);
        waitForTableLoad();
    }

    /**
     * Sorts devices by last check-in time.
     */
    public void sortByLastCheckIn(AmieOptions.Sort order) {
        log.debug("Sorting devices by last check-in: {}", order);
        getTable().sort(COL_LAST_CHECK_IN, order);
        waitForTableLoad();
    }

    // ========== Device Selection ==========

    /**
     * Clicks on a device by serial number to open details.
     */
    public void selectDevice(String serialNumber) {
        log.debug("Selecting device: {}", serialNumber);
        Locator cell = getTable().getCellWithValue(serialNumber, COL_SERIAL);
        if (cell != null) {
            cell.click();
            waitForPageLoad();
        } else {
            log.error("Device with serial {} not found", serialNumber);
        }
    }

    // ========== Export Operations ==========

    /**
     * Clicks the export button.
     */
    public void clickExport() {
        page.locator(EXPORT_BUTTON).click();
    }

    // ========== Filter Operations ==========

    /**
     * Clicks the filter button to open filter panel.
     */
    public void openFilters() {
        page.locator(FILTER_BUTTON).click();
    }
}
