package com.spectralink.aimwright.tests.monitoring;

import com.spectralink.aimwright.common.*;
import com.spectralink.aimwright.pages.BasePage;
import com.spectralink.aimwright.pages.alerts.AlertsPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Tests for Alerts list sorting functionality.
 */
public class AlertsSortingTest extends BaseTest {

    private AlertsPage alertsPage;

    @BeforeMethod
    @Override
    public void testBeginDemarcation(Method method) {
        super.testBeginDemarcation(method);

        // Login and set org context
        Session.uiLoginAdminUser(page);
        BasePage basePage = new BasePage(page);
        basePage.selectOrganization(Defaults.getOrgName());

        // Navigate to alerts
        alertsPage = new AlertsPage(page);
        alertsPage.navigateTo();

        // Ensure we have enough data for sorting tests
        Assumptions.assumeMinimumRows(alertsPage.getAlertCount(), 2,
                "Need at least 2 alerts for sorting tests");
    }

    @Test(description = "Verify alerts can be sorted by alert type ascending")
    public void sortByAlertTypeAsc() {
        alertsPage.sortByAlertType(AmieOptions.Sort.ASC);

        List<String> types = alertsPage.getAlertTypes();
        Asserts.isAlphabeticallySorted(types, AmieOptions.Sort.ASC, false);
    }

    @Test(description = "Verify alerts can be sorted by alert type descending")
    public void sortByAlertTypeDesc() {
        alertsPage.sortByAlertType(AmieOptions.Sort.DESC);

        List<String> types = alertsPage.getAlertTypes();
        Asserts.isAlphabeticallySorted(types, AmieOptions.Sort.DESC, false);
    }

    @Test(description = "Verify alerts can be sorted by severity ascending")
    public void sortBySeverityAsc() {
        alertsPage.sortBySeverity(AmieOptions.Sort.ASC);

        List<String> severities = alertsPage.getSeverities();
        Asserts.isAlphabeticallySorted(severities, AmieOptions.Sort.ASC, false);
    }

    @Test(description = "Verify alerts can be sorted by severity descending")
    public void sortBySeverityDesc() {
        alertsPage.sortBySeverity(AmieOptions.Sort.DESC);

        List<String> severities = alertsPage.getSeverities();
        Asserts.isAlphabeticallySorted(severities, AmieOptions.Sort.DESC, false);
    }

    @Test(description = "Verify alerts can be sorted by status ascending")
    public void sortByStatusAsc() {
        alertsPage.sortByStatus(AmieOptions.Sort.ASC);

        List<String> statuses = alertsPage.getStatuses();
        Asserts.isAlphabeticallySorted(statuses, AmieOptions.Sort.ASC, false);
    }

    @Test(description = "Verify alerts can be sorted by status descending")
    public void sortByStatusDesc() {
        alertsPage.sortByStatus(AmieOptions.Sort.DESC);

        List<String> statuses = alertsPage.getStatuses();
        Asserts.isAlphabeticallySorted(statuses, AmieOptions.Sort.DESC, false);
    }

    @Test(description = "Verify alerts can be sorted by timestamp ascending")
    public void sortByTimestampAsc() {
        alertsPage.sortByTimestamp(AmieOptions.Sort.ASC);

        List<String> timestamps = alertsPage.getTable().getColumnValues(AlertsPage.COL_TIMESTAMP);
        Asserts.isTimeSorted(timestamps, AmieOptions.Sort.ASC);
    }

    @Test(description = "Verify alerts can be sorted by timestamp descending")
    public void sortByTimestampDesc() {
        alertsPage.sortByTimestamp(AmieOptions.Sort.DESC);

        List<String> timestamps = alertsPage.getTable().getColumnValues(AlertsPage.COL_TIMESTAMP);
        Asserts.isTimeSorted(timestamps, AmieOptions.Sort.DESC);
    }
}
