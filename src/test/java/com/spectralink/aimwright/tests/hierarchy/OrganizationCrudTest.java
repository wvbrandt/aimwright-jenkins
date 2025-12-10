package com.spectralink.aimwright.tests.hierarchy;

import com.spectralink.aimwright.common.BaseTest;
import com.spectralink.aimwright.common.Session;
import com.spectralink.aimwright.pages.organizations.OrganizationsListPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Tests for Organization CRUD operations.
 */
public class OrganizationCrudTest extends BaseTest {

    private OrganizationsListPage orgPage;
    private String testOrgName;

    @BeforeMethod
    @Override
    public void testBeginDemarcation(Method method) {
        super.testBeginDemarcation(method);

        // Login and navigate to organizations
        Session.uiLoginSpectraLinkSuperUser(page);
        orgPage = new OrganizationsListPage(page);
        orgPage.navigateTo();

        // Generate unique org name for each test
        testOrgName = "Test Org " + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test(description = "Verify organization can be created")
    public void createOrganization() {
        int initialCount = orgPage.getOrganizationCount();

        orgPage.createOrganization(testOrgName, "Test organization description");

        Assert.assertTrue(orgPage.organizationExists(testOrgName),
                "New organization should appear in the list");
        Assert.assertEquals(orgPage.getOrganizationCount(), initialCount + 1,
                "Organization count should increase by 1");

        // Cleanup
        orgPage.deleteOrganization(testOrgName);
    }

    @Test(description = "Verify organization can be edited")
    public void editOrganization() {
        // Create org first
        orgPage.createOrganization(testOrgName);
        Assert.assertTrue(orgPage.organizationExists(testOrgName));

        // Edit the org
        String newName = testOrgName + " - Edited";
        orgPage.editOrganization(testOrgName, newName, "Updated description");

        Assert.assertTrue(orgPage.organizationExists(newName),
                "Edited organization name should appear");
        Assert.assertFalse(orgPage.organizationExists(testOrgName),
                "Old organization name should not appear");

        // Cleanup
        orgPage.deleteOrganization(newName);
    }

    @Test(description = "Verify organization can be deleted")
    public void deleteOrganization() {
        // Create org first
        orgPage.createOrganization(testOrgName);
        Assert.assertTrue(orgPage.organizationExists(testOrgName));

        int countBeforeDelete = orgPage.getOrganizationCount();

        // Delete the org
        orgPage.deleteOrganization(testOrgName);

        Assert.assertFalse(orgPage.organizationExists(testOrgName),
                "Deleted organization should not appear in the list");
        Assert.assertEquals(orgPage.getOrganizationCount(), countBeforeDelete - 1,
                "Organization count should decrease by 1");
    }

    @Test(description = "Verify organization search functionality")
    public void searchOrganization() {
        // Create org first
        orgPage.createOrganization(testOrgName);

        // Search for the org
        orgPage.search(testOrgName);

        Assert.assertTrue(orgPage.organizationExists(testOrgName),
                "Searched organization should appear");

        // Clear search
        orgPage.clearSearch();

        // Cleanup
        orgPage.deleteOrganization(testOrgName);
    }
}
