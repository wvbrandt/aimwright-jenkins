package com.spectralink.aimwright.tests;

import ch.qos.logback.classic.Logger;
import com.spectralink.aimwright.common.BaseTest;
import com.spectralink.aimwright.common.Settings;
import com.spectralink.aimwright.pages.BasePage;
import com.spectralink.aimwright.pages.LoginPage;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

public class ExampleTest extends BaseTest {
    private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass());

    @Test
    public void loginToAmie() throws InterruptedException {
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(page.title(), "AMiE", "Beginning page title was not correct");

        LoginPage loginPage = new LoginPage(page);
        loginPage.loginWithCredentials(Settings.get("target.user"), Settings.get("target.password"));

        BasePage basePage = new BasePage(page);
        softAssert.assertEquals(basePage.isLoggedIn(), true, "Landing page was not correct");
        if (page.title().contentEquals("Accounts")) log.debug("Logged into AMiE successfully");

        basePage.clickSider();
        softAssert.assertEquals(basePage.isMenuFolded(), true, "Page navigation did not collapse");
        basePage.clickSider();
        softAssert.assertEquals(basePage.isMenuFolded(), false, "Page navigation did not expand");

        basePage.logout();

        softAssert.assertEquals(page.title(), "AMiE", "Beginning page title was not correct");
        page.waitForTimeout(3000);
        softAssert.assertAll();
    }
}