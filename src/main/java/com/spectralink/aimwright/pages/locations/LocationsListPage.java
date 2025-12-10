package com.spectralink.aimwright.pages.locations;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.spectralink.aimwright.common.AmieOptions;
import com.spectralink.aimwright.helpers.MenuHelper;
import com.spectralink.aimwright.helpers.TableHelper;
import com.spectralink.aimwright.pages.BasePage;

import java.util.List;

/**
 * Page object for the Locations List page.
 *
 * Provides access to location list operations including CRUD and sorting.
 */
public class LocationsListPage extends BasePage {

    // Page selectors
    private static final String LOCATIONS_TABLE = ".ant-table";
    private static final String ADD_LOCATION_BUTTON = "button:has-text('Add Location')";
    private static final String EXPORT_BUTTON = "button:has-text('Export')";

    // Location form selectors
    private static final String LOCATION_NAME_INPUT = "#location_name";
    private static final String TIMEZONE_SELECT = "#timezone";
    private static final String GATEWAY_SELECT = "#gateway";
    private static final String SAVE_BUTTON = "button:has-text('Save')";
    private static final String CANCEL_BUTTON = "button:has-text('Cancel')";
    private static final String DELETE_BUTTON = "button:has-text('Delete')";
    private static final String EDIT_BUTTON = "button:has-text('Edit')";
    private static final String CONFIRM_DELETE_BUTTON = ".ant-popconfirm-buttons button:has-text('Yes')";

    // Column names
    public static final String COL_LOCATION_NAME = "Location Name";
    public static final String COL_TIMEZONE = "Timezone";
    public static final String COL_GATEWAY = "Gateway";
    public static final String COL_DEVICES = "Devices";
    public static final String COL_BATTERIES = "Batteries";

    private TableHelper table;

    public LocationsListPage(Page page) {
        super(page);
    }

    /**
     * Navigates to the Locations List page.
     */
    public void navigateTo() {
        navigation.toLocationList();
        waitForTableLoad();
        initTable();
    }

    private void initTable() {
        table = new TableHelper(page, LOCATIONS_TABLE);
    }

    @Override
    public TableHelper getTable() {
        if (table == null) {
            initTable();
        }
        return table;
    }

    // ========== Location List Operations ==========

    /**
     * Gets the total number of locations in the table.
     */
    public int getLocationCount() {
        return getTable().getRowCount();
    }

    /**
     * Gets the list of location names.
     */
    public List<String> getLocationNames() {
        return getTable().getColumnValues(COL_LOCATION_NAME);
    }

    /**
     * Checks if a location exists in the table.
     */
    public boolean locationExists(String locationName) {
        return getLocationNames().contains(locationName);
    }

    // ========== CRUD Operations ==========

    /**
     * Opens the Add Location form.
     */
    public void clickAddLocation() {
        log.debug("Clicking Add Location button");
        page.locator(ADD_LOCATION_BUTTON).click();
        waitForPageLoad();
    }

    /**
     * Creates a new location with the specified name and timezone.
     */
    public void createLocation(String name, String timezone) {
        clickAddLocation();
        fillLocationForm(name, timezone, null);
        clickSave();
        waitForTableLoad();
    }

    /**
     * Creates a new location with name, timezone, and gateway.
     */
    public void createLocation(String name, String timezone, String gateway) {
        clickAddLocation();
        fillLocationForm(name, timezone, gateway);
        clickSave();
        waitForTableLoad();
    }

    /**
     * Fills the location form fields.
     */
    private void fillLocationForm(String name, String timezone, String gateway) {
        if (name != null) {
            page.locator(LOCATION_NAME_INPUT).fill(name);
        }
        if (timezone != null) {
            new MenuHelper(page, TIMEZONE_SELECT, true).select(timezone);
        }
        if (gateway != null) {
            new MenuHelper(page, GATEWAY_SELECT, true).select(gateway);
        }
    }

    /**
     * Selects a location from the table.
     */
    public void selectLocation(String locationName) {
        log.debug("Selecting location: {}", locationName);
        Locator cell = getTable().getCellWithValue(locationName, COL_LOCATION_NAME);
        if (cell != null) {
            cell.click();
            waitForPageLoad();
        } else {
            log.error("Location {} not found", locationName);
        }
    }

    /**
     * Edits an existing location.
     */
    public void editLocation(String locationName, String newName, String newTimezone) {
        selectLocation(locationName);
        page.locator(EDIT_BUTTON).click();
        if (newName != null) {
            page.locator(LOCATION_NAME_INPUT).clear();
            page.locator(LOCATION_NAME_INPUT).fill(newName);
        }
        if (newTimezone != null) {
            new MenuHelper(page, TIMEZONE_SELECT, true).select(newTimezone);
        }
        clickSave();
        waitForTableLoad();
    }

    /**
     * Deletes a location.
     */
    public void deleteLocation(String locationName) {
        selectLocation(locationName);
        page.locator(DELETE_BUTTON).click();
        page.locator(CONFIRM_DELETE_BUTTON).click();
        waitForTableLoad();
    }

    // ========== Form Actions ==========

    public void clickSave() {
        page.locator(SAVE_BUTTON).click();
    }

    public void clickCancel() {
        page.locator(CANCEL_BUTTON).click();
    }

    // ========== Sorting Operations ==========

    /**
     * Sorts locations by name.
     */
    public void sortByName(AmieOptions.Sort order) {
        log.debug("Sorting locations by name: {}", order);
        getTable().sort(COL_LOCATION_NAME, order);
        waitForTableLoad();
    }

    /**
     * Sorts locations by timezone.
     */
    public void sortByTimezone(AmieOptions.Sort order) {
        log.debug("Sorting locations by timezone: {}", order);
        getTable().sort(COL_TIMEZONE, order);
        waitForTableLoad();
    }

    /**
     * Sorts locations by device count.
     */
    public void sortByDevices(AmieOptions.Sort order) {
        log.debug("Sorting locations by devices: {}", order);
        getTable().sort(COL_DEVICES, order);
        waitForTableLoad();
    }

    // ========== Export ==========

    public void clickExport() {
        page.locator(EXPORT_BUTTON).click();
    }
}
