package com.spectralink.aimwright.tests.hierarchy;

import com.spectralink.aimwright.common.*;
import com.spectralink.aimwright.pages.BasePage;
import com.spectralink.aimwright.pages.locations.LocationsListPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Tests for Location list sorting functionality.
 */
public class LocationSortingTest extends BaseTest {

    private LocationsListPage locPage;

    @BeforeMethod
    @Override
    public void testBeginDemarcation(Method method) {
        super.testBeginDemarcation(method);

        // Login and set org context
        Session.uiLoginSpectraLinkSuperUser(page);
        BasePage basePage = new BasePage(page);
        basePage.selectOrganization(Settings.getOrgName());

        // Navigate to locations
        locPage = new LocationsListPage(page);
        locPage.navigateTo();

        // Ensure we have enough data for sorting tests
        Assumptions.assumeMinimumRows(locPage.getLocationCount(), 2,
                "Need at least 2 locations for sorting tests");
    }

    @Test(description = "Verify locations can be sorted by name ascending")
    public void sortLocationsByNameAsc() {
        locPage.sortByName(AmieOptions.Sort.ASC);

        List<String> names = locPage.getLocationNames();
        Asserts.isAlphabeticallySorted(names, AmieOptions.Sort.ASC, false);
    }

    @Test(description = "Verify locations can be sorted by name descending")
    public void sortLocationsByNameDesc() {
        locPage.sortByName(AmieOptions.Sort.DESC);

        List<String> names = locPage.getLocationNames();
        Asserts.isAlphabeticallySorted(names, AmieOptions.Sort.DESC, false);
    }

    @Test(description = "Verify locations can be sorted by timezone ascending")
    public void sortLocationsByTimezoneAsc() {
        locPage.sortByTimezone(AmieOptions.Sort.ASC);

        List<String> timezones = locPage.getTable().getColumnValues(LocationsListPage.COL_TIMEZONE);
        Asserts.isAlphabeticallySorted(timezones, AmieOptions.Sort.ASC, false);
    }

    @Test(description = "Verify locations can be sorted by timezone descending")
    public void sortLocationsByTimezoneDesc() {
        locPage.sortByTimezone(AmieOptions.Sort.DESC);

        List<String> timezones = locPage.getTable().getColumnValues(LocationsListPage.COL_TIMEZONE);
        Asserts.isAlphabeticallySorted(timezones, AmieOptions.Sort.DESC, false);
    }

    @Test(description = "Verify locations can be sorted by device count ascending")
    public void sortLocationsByDevicesAsc() {
        locPage.sortByDevices(AmieOptions.Sort.ASC);

        List<String> devices = locPage.getTable().getColumnValues(LocationsListPage.COL_DEVICES);
        Asserts.isNumericallySorted(devices, AmieOptions.Sort.ASC);
    }

    @Test(description = "Verify locations can be sorted by device count descending")
    public void sortLocationsByDevicesDesc() {
        locPage.sortByDevices(AmieOptions.Sort.DESC);

        List<String> devices = locPage.getTable().getColumnValues(LocationsListPage.COL_DEVICES);
        Asserts.isNumericallySorted(devices, AmieOptions.Sort.DESC);
    }
}
