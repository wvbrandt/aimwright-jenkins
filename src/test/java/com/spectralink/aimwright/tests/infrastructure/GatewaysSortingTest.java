package com.spectralink.aimwright.tests.infrastructure;

import com.spectralink.aimwright.common.*;
import com.spectralink.aimwright.pages.BasePage;
import com.spectralink.aimwright.pages.gateways.GatewaysSummaryPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Tests for Gateways list sorting functionality.
 */
public class GatewaysSortingTest extends BaseTest {

    private GatewaysSummaryPage gatewaysPage;

    @BeforeMethod
    @Override
    public void testBeginDemarcation(Method method) {
        super.testBeginDemarcation(method);

        // Login and set org context
        Session.uiLoginAdminUser(page);
        BasePage basePage = new BasePage(page);
        basePage.selectOrganization(Defaults.getOrgName());

        // Navigate to gateways
        gatewaysPage = new GatewaysSummaryPage(page);
        gatewaysPage.navigateTo();

        // Ensure we have enough data for sorting tests
        Assumptions.assumeMinimumRows(gatewaysPage.getGatewayCount(), 2,
                "Need at least 2 gateways for sorting tests");
    }

    @Test(description = "Verify gateways can be sorted by name ascending")
    public void sortByNameAsc() {
        gatewaysPage.sortByName(AmieOptions.Sort.ASC);

        List<String> names = gatewaysPage.getGatewayNames();
        Asserts.isAlphabeticallySorted(names, AmieOptions.Sort.ASC, false);
    }

    @Test(description = "Verify gateways can be sorted by name descending")
    public void sortByNameDesc() {
        gatewaysPage.sortByName(AmieOptions.Sort.DESC);

        List<String> names = gatewaysPage.getGatewayNames();
        Asserts.isAlphabeticallySorted(names, AmieOptions.Sort.DESC, false);
    }

    @Test(description = "Verify gateways can be sorted by location ascending")
    public void sortByLocationAsc() {
        gatewaysPage.sortByLocation(AmieOptions.Sort.ASC);

        List<String> locations = gatewaysPage.getLocations();
        Asserts.isAlphabeticallySorted(locations, AmieOptions.Sort.ASC, false);
    }

    @Test(description = "Verify gateways can be sorted by location descending")
    public void sortByLocationDesc() {
        gatewaysPage.sortByLocation(AmieOptions.Sort.DESC);

        List<String> locations = gatewaysPage.getLocations();
        Asserts.isAlphabeticallySorted(locations, AmieOptions.Sort.DESC, false);
    }

    @Test(description = "Verify gateways can be sorted by health ascending")
    public void sortByHealthAsc() {
        gatewaysPage.sortByHealth(AmieOptions.Sort.ASC);

        List<String> health = gatewaysPage.getHealthValues();
        Asserts.isAlphabeticallySorted(health, AmieOptions.Sort.ASC, false);
    }

    @Test(description = "Verify gateways can be sorted by health descending")
    public void sortByHealthDesc() {
        gatewaysPage.sortByHealth(AmieOptions.Sort.DESC);

        List<String> health = gatewaysPage.getHealthValues();
        Asserts.isAlphabeticallySorted(health, AmieOptions.Sort.DESC, false);
    }
}
