package com.spectralink.aimwright.pages.organizations;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.spectralink.aimwright.common.AmieOptions;
import com.spectralink.aimwright.helpers.TableHelper;
import com.spectralink.aimwright.pages.BasePage;

import java.util.List;

/**
 * Page object for the Organizations List page.
 *
 * Provides access to organization list operations including CRUD and sorting.
 */
public class OrganizationsListPage extends BasePage {

    // Page selectors
    private static final String ORGANIZATIONS_TABLE = ".ant-table";
    private static final String ADD_ORG_BUTTON = "button:has-text('Add Organization')";
    private static final String EXPORT_BUTTON = "button:has-text('Export')";
    private static final String SEARCH_INPUT = "input[placeholder*='Search']";

    // Organization form selectors
    private static final String ORG_NAME_INPUT = "#organization_name";
    private static final String ORG_DESCRIPTION_INPUT = "#description";
    private static final String SAVE_BUTTON = "button:has-text('Save')";
    private static final String CANCEL_BUTTON = "button:has-text('Cancel')";
    private static final String DELETE_BUTTON = "button:has-text('Delete')";
    private static final String EDIT_BUTTON = "button:has-text('Edit')";
    private static final String CONFIRM_DELETE_BUTTON = ".ant-popconfirm-buttons button:has-text('Yes')";

    // Column names
    public static final String COL_ORG_NAME = "Organization Name";
    public static final String COL_DESCRIPTION = "Description";
    public static final String COL_LOCATIONS = "Locations";
    public static final String COL_DEVICES = "Devices";
    public static final String COL_BATTERIES = "Batteries";

    private TableHelper table;

    public OrganizationsListPage(Page page) {
        super(page);
    }

    /**
     * Navigates to the Organizations List page.
     */
    public void navigateTo() {
        navigation.toOrganizations();
        waitForTableLoad();
        initTable();
    }

    private void initTable() {
        table = new TableHelper(page, ORGANIZATIONS_TABLE);
    }

    @Override
    public TableHelper getTable() {
        if (table == null) {
            initTable();
        }
        return table;
    }

    // ========== Organization List Operations ==========

    /**
     * Gets the total number of organizations in the table.
     */
    public int getOrganizationCount() {
        return getTable().getRowCount();
    }

    /**
     * Gets the list of organization names.
     */
    public List<String> getOrganizationNames() {
        return getTable().getColumnValues(COL_ORG_NAME);
    }

    /**
     * Checks if an organization exists in the table.
     */
    public boolean organizationExists(String orgName) {
        return getOrganizationNames().contains(orgName);
    }

    // ========== CRUD Operations ==========

    /**
     * Opens the Add Organization form.
     */
    public void clickAddOrganization() {
        log.debug("Clicking Add Organization button");
        page.locator(ADD_ORG_BUTTON).click();
        waitForPageLoad();
    }

    /**
     * Creates a new organization with the specified name.
     */
    public void createOrganization(String name) {
        createOrganization(name, null);
    }

    /**
     * Creates a new organization with name and description.
     */
    public void createOrganization(String name, String description) {
        clickAddOrganization();
        fillOrganizationForm(name, description);
        clickSave();
        waitForTableLoad();
    }

    /**
     * Fills the organization form fields.
     */
    private void fillOrganizationForm(String name, String description) {
        if (name != null) {
            page.locator(ORG_NAME_INPUT).fill(name);
        }
        if (description != null) {
            page.locator(ORG_DESCRIPTION_INPUT).fill(description);
        }
    }

    /**
     * Selects an organization from the table.
     */
    public void selectOrganization(String orgName) {
        log.debug("Selecting organization: {}", orgName);
        Locator cell = getTable().getCellWithValue(orgName, COL_ORG_NAME);
        if (cell != null) {
            cell.click();
            waitForPageLoad();
        } else {
            log.error("Organization {} not found", orgName);
        }
    }

    /**
     * Edits an existing organization.
     */
    public void editOrganization(String orgName, String newName, String newDescription) {
        selectOrganization(orgName);
        page.locator(EDIT_BUTTON).click();
        if (newName != null) {
            page.locator(ORG_NAME_INPUT).clear();
            page.locator(ORG_NAME_INPUT).fill(newName);
        }
        if (newDescription != null) {
            page.locator(ORG_DESCRIPTION_INPUT).clear();
            page.locator(ORG_DESCRIPTION_INPUT).fill(newDescription);
        }
        clickSave();
        waitForTableLoad();
    }

    /**
     * Deletes an organization.
     */
    public void deleteOrganization(String orgName) {
        selectOrganization(orgName);
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

    // ========== Search ==========

    /**
     * Searches for organizations by keyword.
     */
    public void search(String keyword) {
        log.debug("Searching for: {}", keyword);
        page.locator(SEARCH_INPUT).fill(keyword);
        page.keyboard().press("Enter");
        waitForTableLoad();
    }

    /**
     * Clears the search input.
     */
    public void clearSearch() {
        page.locator(SEARCH_INPUT).clear();
        page.keyboard().press("Enter");
        waitForTableLoad();
    }

    // ========== Sorting Operations ==========

    /**
     * Sorts organizations by name.
     */
    public void sortByName(AmieOptions.Sort order) {
        log.debug("Sorting organizations by name: {}", order);
        getTable().sort(COL_ORG_NAME, order);
        waitForTableLoad();
    }

    /**
     * Sorts organizations by description.
     */
    public void sortByDescription(AmieOptions.Sort order) {
        log.debug("Sorting organizations by description: {}", order);
        getTable().sort(COL_DESCRIPTION, order);
        waitForTableLoad();
    }

    /**
     * Sorts organizations by location count.
     */
    public void sortByLocations(AmieOptions.Sort order) {
        log.debug("Sorting organizations by locations: {}", order);
        getTable().sort(COL_LOCATIONS, order);
        waitForTableLoad();
    }

    // ========== Export ==========

    public void clickExport() {
        page.locator(EXPORT_BUTTON).click();
    }
}
