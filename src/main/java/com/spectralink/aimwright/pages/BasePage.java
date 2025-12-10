package com.spectralink.aimwright.pages;

import ch.qos.logback.classic.Logger;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.spectralink.aimwright.helpers.MenuHelper;
import com.spectralink.aimwright.helpers.NavigationHelper;
import com.spectralink.aimwright.helpers.TableHelper;
import org.slf4j.LoggerFactory;

/**
 * Base page class providing common functionality for all page objects.
 *
 * Provides:
 * - Common UI element interactions (avatar, sidebar, logout)
 * - Access to helper classes (Navigation, Table, Menu)
 * - Organization and location context management
 */
public class BasePage {
    protected final Page page;
    protected final Logger log = (Logger) LoggerFactory.getLogger(this.getClass());

    // Navigation helper for this page
    protected NavigationHelper navigation;

    // Common selectors
    private static final String USER_AVATAR = "span.ant-avatar";
    private static final String LOGO_IMAGE = "logo";
    private static final String COLLAPSE_ICON = "[aria-label^='icon: menu-']";
    private static final String SIDEBAR = "aside.ant-layout-sider";
    private static final String SIGN_OUT_TEXT = "Sign Out";

    // Header selectors
    private static final String HEADER_ORG_SELECT = "#header-organization";
    private static final String HEADER_LOCATION_SELECT = "#header-location";
    private static final String ORG_PLACEHOLDER = "#organization .ant-select-selection__placeholder";
    private static final String ORG_SELECTED = "#organization .ant-select-selected-value";

    // Spinner/loading indicators
    private static final String SPINNER = ".ant-spin-spinning";
    private static final String TABLE_LOADING = ".ant-table-loading";

    public BasePage(Page page) {
        this.page = page;
        this.navigation = new NavigationHelper(page);
    }

    // ========== Navigation Access ==========

    /**
     * Returns the navigation helper for this page.
     */
    public NavigationHelper getNavigation() {
        return navigation;
    }

    /**
     * Creates a new TableHelper for the default table on this page.
     */
    public TableHelper getTable() {
        return new TableHelper(page);
    }

    /**
     * Creates a new TableHelper for a specific table.
     */
    public TableHelper getTable(String tableSelector) {
        return new TableHelper(page, tableSelector);
    }

    // ========== Common UI Interactions ==========

    /**
     * Clicks the Spectralink logo to return to dashboard.
     */
    public void clickLogo() {
        log.debug("Clicking Spectralink logo");
        page.getByAltText(LOGO_IMAGE).click();
        waitForPageLoad();
    }

    /**
     * Toggles the sidebar collapse state.
     */
    public void toggleSidebar() {
        log.debug("Toggling sidebar");
        page.locator(COLLAPSE_ICON).click();
    }

    /**
     * Checks if the sidebar menu is collapsed.
     */
    public boolean isSidebarCollapsed() {
        String className = page.locator(SIDEBAR).getAttribute("class");
        boolean collapsed = className != null && className.contains("ant-layout-sider-collapsed");
        log.debug("Sidebar is {}", collapsed ? "collapsed" : "expanded");
        return collapsed;
    }

    /**
     * Checks if user is logged in by looking for avatar.
     */
    public boolean isLoggedIn() {
        try {
            return page.locator(USER_AVATAR).isVisible();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Logs out of the application.
     */
    public void logout() {
        log.debug("Logging out");
        page.locator(USER_AVATAR).click();
        page.getByText(SIGN_OUT_TEXT).click();
        waitForPageLoad();
    }

    // ========== Organization/Location Context ==========

    /**
     * Gets the currently selected organization name.
     */
    public String getSelectedOrganization() {
        Locator placeholder = page.locator(ORG_PLACEHOLDER);
        Locator selected = page.locator(ORG_SELECTED);

        if (selected.isVisible()) {
            return selected.innerText();
        } else if (placeholder.isVisible()) {
            return placeholder.innerText();
        }
        return "Unknown";
    }

    /**
     * Selects an organization from the header dropdown.
     */
    public void selectOrganization(String orgName) {
        log.debug("Selecting organization: {}", orgName);
        MenuHelper menu = new MenuHelper(page, HEADER_ORG_SELECT, true);
        menu.select(orgName);
        waitForPageLoad();
    }

    /**
     * Selects a location from the header dropdown.
     */
    public void selectLocation(String locationName) {
        log.debug("Selecting location: {}", locationName);
        MenuHelper menu = new MenuHelper(page, HEADER_LOCATION_SELECT, true);
        menu.select(locationName);
        waitForPageLoad();
    }

    // ========== Wait Utilities ==========

    /**
     * Waits for the page to finish loading (network idle).
     */
    public void waitForPageLoad() {
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    /**
     * Waits for any spinners to disappear.
     */
    public void waitForSpinner() {
        Locator spinner = page.locator(SPINNER);
        if (spinner.count() > 0) {
            spinner.first().waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN));
        }
    }

    /**
     * Waits for table loading to complete.
     */
    public void waitForTableLoad() {
        Locator loading = page.locator(TABLE_LOADING);
        if (loading.count() > 0) {
            loading.first().waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN));
        }
    }

    // ========== Page State ==========

    /**
     * Returns the current page URL.
     */
    public String getCurrentUrl() {
        return page.url();
    }

    /**
     * Returns the page title.
     */
    public String getPageTitle() {
        return page.title();
    }

    /**
     * Takes a screenshot of the current page state.
     */
    public byte[] takeScreenshot() {
        return page.screenshot();
    }

    // ========== Sidebar/Sider Controls ==========

    /**
     * Clicks the sidebar collapse/expand button.
     */
    public void clickSider() {
        Locator collapseButton = page.locator(COLLAPSE_ICON);
        if (collapseButton.count() > 0) {
            collapseButton.first().click();
            waitForPageLoad();
        }
    }

    /**
     * Checks if the sidebar menu is collapsed/folded.
     * @return true if the sidebar is collapsed, false otherwise
     */
    public boolean isMenuFolded() {
        Locator sidebar = page.locator(SIDEBAR);
        if (sidebar.count() > 0) {
            String classAttr = sidebar.first().getAttribute("class");
            return classAttr != null && classAttr.contains("collapsed");
        }
        return false;
    }
}
