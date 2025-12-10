package com.spectralink.aimwright.tests.auth;

import com.spectralink.aimwright.common.BaseTest;
import com.spectralink.aimwright.common.Defaults;
import com.spectralink.aimwright.common.Session;
import com.spectralink.aimwright.pages.BasePage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for authentication and login functionality.
 */
public class LoginTest extends BaseTest {

    @Test(description = "Verify SpectraLink super user can login successfully")
    public void loginAsSpectraLinkSuperUser() {
        Session.uiLoginSpectraLinkSuperUser(page);

        BasePage basePage = new BasePage(page);
        Assert.assertTrue(basePage.isLoggedIn(), "User should be logged in");
    }

    @Test(description = "Verify admin user can login successfully")
    public void loginAsAdminUser() {
        Session.uiLoginAdminUser(page);

        BasePage basePage = new BasePage(page);
        Assert.assertTrue(basePage.isLoggedIn(), "Admin user should be logged in");
    }

    @Test(description = "Verify super user can login successfully")
    public void loginAsSuperUser() {
        Session.uiLoginSuperUser(page);

        BasePage basePage = new BasePage(page);
        Assert.assertTrue(basePage.isLoggedIn(), "Super user should be logged in");
    }

    @Test(description = "Verify read-only user can login successfully")
    public void loginAsReadOnlyUser() {
        Session.uiLoginReadOnlyUser(page);

        BasePage basePage = new BasePage(page);
        Assert.assertTrue(basePage.isLoggedIn(), "Read-only user should be logged in");
    }

    @Test(description = "Verify user can logout successfully")
    public void logoutTest() {
        Session.uiLoginAdminUser(page);

        BasePage basePage = new BasePage(page);
        Assert.assertTrue(basePage.isLoggedIn(), "User should be logged in initially");

        basePage.logout();

        // After logout, should be back to login page
        Assert.assertTrue(page.url().contains("login") || !basePage.isLoggedIn(),
                "User should be logged out");
    }
}
