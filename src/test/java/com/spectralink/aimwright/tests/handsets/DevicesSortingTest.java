package com.spectralink.aimwright.tests.handsets;

import com.spectralink.aimwright.common.*;
import com.spectralink.aimwright.pages.BasePage;
import com.spectralink.aimwright.pages.devices.DevicesSummaryPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Tests for Devices list sorting functionality.
 */
public class DevicesSortingTest extends BaseTest {

    private DevicesSummaryPage devicesPage;

    @BeforeMethod
    @Override
    public void testBeginDemarcation(Method method) {
        super.testBeginDemarcation(method);

        // Login and set org context
        Session.uiLoginAdminUser(page);
        BasePage basePage = new BasePage(page);
        basePage.selectOrganization(Settings.getOrgName());

        // Navigate to devices
        devicesPage = new DevicesSummaryPage(page);
        devicesPage.navigateTo();

        // Ensure we have enough data for sorting tests
        Assumptions.assumeMinimumRows(devicesPage.getDeviceCount(), 2,
                "Need at least 2 devices for sorting tests");
    }

    @Test(description = "Verify devices can be sorted by model ascending")
    public void sortByModelAsc() {
        devicesPage.sortByModel(AmieOptions.Sort.ASC);

        List<String> models = devicesPage.getDeviceModels();
        Asserts.isAlphabeticallySorted(models, AmieOptions.Sort.ASC, false);
    }

    @Test(description = "Verify devices can be sorted by model descending")
    public void sortByModelDesc() {
        devicesPage.sortByModel(AmieOptions.Sort.DESC);

        List<String> models = devicesPage.getDeviceModels();
        Asserts.isAlphabeticallySorted(models, AmieOptions.Sort.DESC, false);
    }

    @Test(description = "Verify devices can be sorted by name ascending")
    public void sortByNameAsc() {
        devicesPage.sortByName(AmieOptions.Sort.ASC);

        List<String> names = devicesPage.getDeviceNames();
        Asserts.isAlphabeticallySorted(names, AmieOptions.Sort.ASC, false);
    }

    @Test(description = "Verify devices can be sorted by name descending")
    public void sortByNameDesc() {
        devicesPage.sortByName(AmieOptions.Sort.DESC);

        List<String> names = devicesPage.getDeviceNames();
        Asserts.isAlphabeticallySorted(names, AmieOptions.Sort.DESC, false);
    }

    @Test(description = "Verify devices can be sorted by serial number ascending")
    public void sortBySerialAsc() {
        devicesPage.sortBySerial(AmieOptions.Sort.ASC);

        List<String> serials = devicesPage.getSerialNumbers();
        Asserts.isAlphabeticallySorted(serials, AmieOptions.Sort.ASC, false);
    }

    @Test(description = "Verify devices can be sorted by serial number descending")
    public void sortBySerialDesc() {
        devicesPage.sortBySerial(AmieOptions.Sort.DESC);

        List<String> serials = devicesPage.getSerialNumbers();
        Asserts.isAlphabeticallySorted(serials, AmieOptions.Sort.DESC, false);
    }

    @Test(description = "Verify devices can be sorted by software version ascending")
    public void sortBySoftwareAsc() {
        devicesPage.sortBySoftware(AmieOptions.Sort.ASC);

        List<String> versions = devicesPage.getSoftwareVersions();
        Asserts.isAlphabeticallySorted(versions, AmieOptions.Sort.ASC, false);
    }

    @Test(description = "Verify devices can be sorted by software version descending")
    public void sortBySoftwareDesc() {
        devicesPage.sortBySoftware(AmieOptions.Sort.DESC);

        List<String> versions = devicesPage.getSoftwareVersions();
        Asserts.isAlphabeticallySorted(versions, AmieOptions.Sort.DESC, false);
    }

    @Test(description = "Verify devices can be sorted by last check-in ascending")
    public void sortByLastCheckInAsc() {
        devicesPage.sortByLastCheckIn(AmieOptions.Sort.ASC);

        List<String> checkIns = devicesPage.getTable().getColumnValues(DevicesSummaryPage.COL_LAST_CHECK_IN);
        Asserts.isTimeSorted(checkIns, AmieOptions.Sort.ASC);
    }

    @Test(description = "Verify devices can be sorted by last check-in descending")
    public void sortByLastCheckInDesc() {
        devicesPage.sortByLastCheckIn(AmieOptions.Sort.DESC);

        List<String> checkIns = devicesPage.getTable().getColumnValues(DevicesSummaryPage.COL_LAST_CHECK_IN);
        Asserts.isTimeSorted(checkIns, AmieOptions.Sort.DESC);
    }
}
