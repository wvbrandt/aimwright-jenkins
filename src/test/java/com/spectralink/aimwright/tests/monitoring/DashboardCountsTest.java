package com.spectralink.aimwright.tests.monitoring;

import com.fasterxml.jackson.databind.JsonNode;
import com.spectralink.aimwright.api.AmieApiClient;
import com.spectralink.aimwright.common.BaseTest;
import com.spectralink.aimwright.common.Session;
import com.spectralink.aimwright.common.Settings;
import com.spectralink.aimwright.pages.BasePage;
import com.spectralink.aimwright.pages.DashboardPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static com.spectralink.aimwright.api.ApiClient.Method.GET;

/**
 * Tests for Dashboard card counts verification.
 * Compares UI dashboard counts with API data.
 */
public class DashboardCountsTest extends BaseTest {

    private DashboardPage dashboardPage;
    private AmieApiClient apiClient;

    @BeforeMethod
    @Override
    public void testBeginDemarcation(Method method) {
        super.testBeginDemarcation(method);

        // Login and set org context
        Session.uiLoginAdminUser(page);
        BasePage basePage = new BasePage(page);
        basePage.selectOrganization(Settings.getOrgName());

        // Get API client for data verification
        apiClient = Session.getAmieApiClient();

        // Navigate to dashboard
        dashboardPage = new DashboardPage(page);
        dashboardPage.navigateTo();
    }

    @Test(description = "Verify devices count on dashboard matches API")
    public void verifyDevicesCount() {
        // Get count from UI
        int uiCount = dashboardPage.getDevicesCount();

        // Get count from API
        JsonNode apiResponse = apiClient.sendRequest(GET, "/api/devices/total").getJsonObjectBody();
        int apiCount = apiResponse.has("total") ? apiResponse.get("total").asInt() : 0;

        Assert.assertEquals(uiCount, apiCount,
                "Dashboard devices count should match API count");
    }

    @Test(description = "Verify batteries count on dashboard matches API")
    public void verifyBatteriesCount() {
        // Get count from UI
        int uiCount = dashboardPage.getBatteriesCount();

        // Get count from API
        JsonNode apiResponse = apiClient.sendRequest(GET, "/api/batteries/total").getJsonObjectBody();
        int apiCount = apiResponse.has("total") ? apiResponse.get("total").asInt() : 0;

        Assert.assertEquals(uiCount, apiCount,
                "Dashboard batteries count should match API count");
    }

    @Test(description = "Verify locations count on dashboard matches API")
    public void verifyLocationsCount() {
        // Get count from UI
        int uiCount = dashboardPage.getLocationsCount();

        // Get count from API
        JsonNode apiResponse = apiClient.sendRequest(GET, "/api/locations/total").getJsonObjectBody();
        int apiCount = apiResponse.has("total") ? apiResponse.get("total").asInt() : 0;

        Assert.assertEquals(uiCount, apiCount,
                "Dashboard locations count should match API count");
    }

    @Test(description = "Verify dashboard card navigation to devices")
    public void verifyDevicesCardNavigation() {
        dashboardPage.clickDevicesCard();

        Assert.assertTrue(page.url().contains("/devices"),
                "Should navigate to devices page");
    }

    @Test(description = "Verify dashboard card navigation to batteries")
    public void verifyBatteriesCardNavigation() {
        dashboardPage.clickBatteriesCard();

        Assert.assertTrue(page.url().contains("/batteries"),
                "Should navigate to batteries page");
    }

    @Test(description = "Verify dashboard card navigation to locations")
    public void verifyLocationsCardNavigation() {
        dashboardPage.clickLocationsCard();

        Assert.assertTrue(page.url().contains("/locations"),
                "Should navigate to locations page");
    }
}
