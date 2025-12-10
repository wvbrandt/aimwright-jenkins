package com.spectralink.aimwright.tests.handsets;

import com.spectralink.aimwright.common.*;
import com.spectralink.aimwright.pages.BasePage;
import com.spectralink.aimwright.pages.batteries.BatteriesSummaryPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Tests for Batteries list sorting functionality.
 */
public class BatteriesSortingTest extends BaseTest {

    private BatteriesSummaryPage batteriesPage;

    @BeforeMethod
    @Override
    public void testBeginDemarcation(Method method) {
        super.testBeginDemarcation(method);

        // Login and set org context
        Session.uiLoginAdminUser(page);
        BasePage basePage = new BasePage(page);
        basePage.selectOrganization(Settings.getOrgName());

        // Navigate to batteries
        batteriesPage = new BatteriesSummaryPage(page);
        batteriesPage.navigateTo();

        // Ensure we have enough data for sorting tests
        Assumptions.assumeMinimumRows(batteriesPage.getBatteryCount(), 2,
                "Need at least 2 batteries for sorting tests");
    }

    @Test(description = "Verify batteries can be sorted by battery serial ascending")
    public void sortByBatterySerialAsc() {
        batteriesPage.sortByBatterySerial(AmieOptions.Sort.ASC);

        List<String> serials = batteriesPage.getBatterySerials();
        Asserts.isAlphabeticallySorted(serials, AmieOptions.Sort.ASC, false);
    }

    @Test(description = "Verify batteries can be sorted by battery serial descending")
    public void sortByBatterySerialDesc() {
        batteriesPage.sortByBatterySerial(AmieOptions.Sort.DESC);

        List<String> serials = batteriesPage.getBatterySerials();
        Asserts.isAlphabeticallySorted(serials, AmieOptions.Sort.DESC, false);
    }

    @Test(description = "Verify batteries can be sorted by device serial ascending")
    public void sortByDeviceSerialAsc() {
        batteriesPage.sortByDeviceSerial(AmieOptions.Sort.ASC);

        List<String> serials = batteriesPage.getDeviceSerials();
        Asserts.isAlphabeticallySorted(serials, AmieOptions.Sort.ASC, false);
    }

    @Test(description = "Verify batteries can be sorted by device serial descending")
    public void sortByDeviceSerialDesc() {
        batteriesPage.sortByDeviceSerial(AmieOptions.Sort.DESC);

        List<String> serials = batteriesPage.getDeviceSerials();
        Asserts.isAlphabeticallySorted(serials, AmieOptions.Sort.DESC, false);
    }

    @Test(description = "Verify batteries can be sorted by health ascending")
    public void sortByHealthAsc() {
        batteriesPage.sortByHealth(AmieOptions.Sort.ASC);

        List<String> health = batteriesPage.getHealthValues();
        Asserts.isAlphabeticallySorted(health, AmieOptions.Sort.ASC, false);
    }

    @Test(description = "Verify batteries can be sorted by health descending")
    public void sortByHealthDesc() {
        batteriesPage.sortByHealth(AmieOptions.Sort.DESC);

        List<String> health = batteriesPage.getHealthValues();
        Asserts.isAlphabeticallySorted(health, AmieOptions.Sort.DESC, false);
    }

    @Test(description = "Verify batteries can be sorted by charge ascending")
    public void sortByChargeAsc() {
        batteriesPage.sortByCharge(AmieOptions.Sort.ASC);

        List<String> charges = batteriesPage.getChargeValues();
        Asserts.isNumericallySorted(charges, AmieOptions.Sort.ASC);
    }

    @Test(description = "Verify batteries can be sorted by charge descending")
    public void sortByChargeDesc() {
        batteriesPage.sortByCharge(AmieOptions.Sort.DESC);

        List<String> charges = batteriesPage.getChargeValues();
        Asserts.isNumericallySorted(charges, AmieOptions.Sort.DESC);
    }
}
