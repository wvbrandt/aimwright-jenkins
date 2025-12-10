package com.spectralink.aimwright.pages.batteries;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.spectralink.aimwright.common.AmieOptions;
import com.spectralink.aimwright.helpers.TableHelper;
import com.spectralink.aimwright.pages.BasePage;

import java.util.List;

/**
 * Page object for the Batteries Summary page.
 *
 * Provides access to battery list table operations including sorting and filtering.
 */
public class BatteriesSummaryPage extends BasePage {

    // Page selectors
    private static final String BATTERIES_TABLE = ".ant-table";
    private static final String EXPORT_BUTTON = "button:has-text('Export')";
    private static final String FILTER_BUTTON = "button:has-text('Filter')";

    // Column names
    public static final String COL_BATTERY_SERIAL = "BATTERY SERIAL";
    public static final String COL_DEVICE_SERIAL = "DEVICE SERIAL";
    public static final String COL_HEALTH = "HEALTH";
    public static final String COL_CHARGE = "CHARGE";
    public static final String COL_TEMPERATURE = "TEMPERATURE";
    public static final String COL_CYCLE_COUNT = "CYCLE COUNT";
    public static final String COL_CAPACITY = "CAPACITY";
    public static final String COL_LOCATION = "LOCATION";

    private TableHelper table;

    public BatteriesSummaryPage(Page page) {
        super(page);
    }

    /**
     * Navigates to the Batteries Summary page.
     */
    public void navigateTo() {
        navigation.toBatteriesSummary();
        waitForTableLoad();
        initTable();
    }

    private void initTable() {
        table = new TableHelper(page, BATTERIES_TABLE);
    }

    @Override
    public TableHelper getTable() {
        if (table == null) {
            initTable();
        }
        return table;
    }

    // ========== Battery List Operations ==========

    /**
     * Gets the total number of batteries in the table.
     */
    public int getBatteryCount() {
        return getTable().getRowCount();
    }

    /**
     * Gets the list of battery serial numbers.
     */
    public List<String> getBatterySerials() {
        return getTable().getColumnValues(COL_BATTERY_SERIAL);
    }

    /**
     * Gets the list of device serial numbers.
     */
    public List<String> getDeviceSerials() {
        return getTable().getColumnValues(COL_DEVICE_SERIAL);
    }

    /**
     * Gets the health values for all batteries.
     */
    public List<String> getHealthValues() {
        return getTable().getColumnValues(COL_HEALTH);
    }

    /**
     * Gets the charge values for all batteries.
     */
    public List<String> getChargeValues() {
        return getTable().getColumnValues(COL_CHARGE);
    }

    // ========== Sorting Operations ==========

    /**
     * Sorts batteries by battery serial.
     */
    public void sortByBatterySerial(AmieOptions.Sort order) {
        log.debug("Sorting batteries by serial: {}", order);
        getTable().sort(COL_BATTERY_SERIAL, order);
        waitForTableLoad();
    }

    /**
     * Sorts batteries by device serial.
     */
    public void sortByDeviceSerial(AmieOptions.Sort order) {
        log.debug("Sorting batteries by device serial: {}", order);
        getTable().sort(COL_DEVICE_SERIAL, order);
        waitForTableLoad();
    }

    /**
     * Sorts batteries by health.
     */
    public void sortByHealth(AmieOptions.Sort order) {
        log.debug("Sorting batteries by health: {}", order);
        getTable().sort(COL_HEALTH, order);
        waitForTableLoad();
    }

    /**
     * Sorts batteries by charge level.
     */
    public void sortByCharge(AmieOptions.Sort order) {
        log.debug("Sorting batteries by charge: {}", order);
        getTable().sort(COL_CHARGE, order);
        waitForTableLoad();
    }

    /**
     * Sorts batteries by temperature.
     */
    public void sortByTemperature(AmieOptions.Sort order) {
        log.debug("Sorting batteries by temperature: {}", order);
        getTable().sort(COL_TEMPERATURE, order);
        waitForTableLoad();
    }

    /**
     * Sorts batteries by cycle count.
     */
    public void sortByCycleCount(AmieOptions.Sort order) {
        log.debug("Sorting batteries by cycle count: {}", order);
        getTable().sort(COL_CYCLE_COUNT, order);
        waitForTableLoad();
    }

    // ========== Battery Selection ==========

    /**
     * Clicks on a battery by serial number to open details.
     */
    public void selectBattery(String batterySerial) {
        log.debug("Selecting battery: {}", batterySerial);
        Locator cell = getTable().getCellWithValue(batterySerial, COL_BATTERY_SERIAL);
        if (cell != null) {
            cell.click();
            waitForPageLoad();
        } else {
            log.error("Battery with serial {} not found", batterySerial);
        }
    }

    // ========== Export/Filter Operations ==========

    public void clickExport() {
        page.locator(EXPORT_BUTTON).click();
    }

    public void openFilters() {
        page.locator(FILTER_BUTTON).click();
    }
}
