package com.spectralink.aimwright.tests.hierarchy;

import com.spectralink.aimwright.common.BaseTest;
import com.spectralink.aimwright.common.Settings;
import com.spectralink.aimwright.common.Session;
import com.spectralink.aimwright.pages.BasePage;
import com.spectralink.aimwright.pages.locations.LocationsListPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Tests for Location CRUD operations.
 */
public class LocationCrudTest extends BaseTest {

    private LocationsListPage locPage;
    private BasePage basePage;
    private String testLocationName;

    @BeforeMethod
    @Override
    public void testBeginDemarcation(Method method) {
        super.testBeginDemarcation(method);

        // Login and set org context
        Session.uiLoginSpectraLinkSuperUser(page);
        basePage = new BasePage(page);
        basePage.selectOrganization(Settings.getOrgName());

        // Navigate to locations
        locPage = new LocationsListPage(page);
        locPage.navigateTo();

        // Generate unique location name for each test
        testLocationName = "Test Loc " + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test(description = "Verify location can be created")
    public void createLocation() {
        int initialCount = locPage.getLocationCount();

        locPage.createLocation(testLocationName, "America/Denver");

        Assert.assertTrue(locPage.locationExists(testLocationName),
                "New location should appear in the list");
        Assert.assertEquals(locPage.getLocationCount(), initialCount + 1,
                "Location count should increase by 1");

        // Cleanup
        locPage.deleteLocation(testLocationName);
    }

    @Test(description = "Verify location can be edited")
    public void editLocation() {
        // Create location first
        locPage.createLocation(testLocationName, "America/Denver");
        Assert.assertTrue(locPage.locationExists(testLocationName));

        // Edit the location
        String newName = testLocationName + " - Edited";
        locPage.editLocation(testLocationName, newName, "America/New_York");

        Assert.assertTrue(locPage.locationExists(newName),
                "Edited location name should appear");
        Assert.assertFalse(locPage.locationExists(testLocationName),
                "Old location name should not appear");

        // Cleanup
        locPage.deleteLocation(newName);
    }

    @Test(description = "Verify location can be deleted")
    public void deleteLocation() {
        // Create location first
        locPage.createLocation(testLocationName, "America/Denver");
        Assert.assertTrue(locPage.locationExists(testLocationName));

        int countBeforeDelete = locPage.getLocationCount();

        // Delete the location
        locPage.deleteLocation(testLocationName);

        Assert.assertFalse(locPage.locationExists(testLocationName),
                "Deleted location should not appear in the list");
        Assert.assertEquals(locPage.getLocationCount(), countBeforeDelete - 1,
                "Location count should decrease by 1");
    }
}
