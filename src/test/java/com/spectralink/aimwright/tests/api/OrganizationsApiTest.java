package com.spectralink.aimwright.tests.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spectralink.aimwright.api.AmieApiClient;
import com.spectralink.aimwright.api.ApiClient;
import com.spectralink.aimwright.api.ApiResponse;
import com.spectralink.aimwright.common.ApiTestWrapper;
import com.spectralink.aimwright.common.Assumptions;
import com.spectralink.aimwright.common.Session;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Organizations API Test Suite
 *
 * Tests for the Organizations API endpoints including:
 * - GET /api/organizations - Get all organizations
 * - GET /api/organizations/{id} - Get organization by ID
 * - POST /api/organizations - Create organization
 * - PUT /api/organizations/{id} - Update organization
 * - DELETE /api/organizations/{id} - Delete organization
 *
 * Test Categories:
 * - Positive Testing: Valid operations with expected inputs
 * - Negative Testing: Invalid inputs and error handling
 * - CRUD Operations: Create, Read, Update, Delete
 */
public class OrganizationsApiTest extends ApiTestWrapper {

    private AmieApiClient apiClient;
    private ObjectMapper objectMapper;
    private String testOrgId;
    private String testOrgName;

    @BeforeClass
    public void setup() {
        Session.setCredentials();
        Session.apiLogin();
        apiClient = Session.getAmieApiClient();
        objectMapper = new ObjectMapper();
        testOrgName = "TestOrg_" + UUID.randomUUID().toString().substring(0, 8);
        log.info("Starting Organizations API Test Suite");
        log.info("Test Organization Name: {}", testOrgName);
    }

    // ========================== POSITIVE TESTING ==========================

    @Test(priority = 1, groups = {"positive", "read"})
    public void testGetAllOrganizations() {
        log.info("TEST: GET /api/organizations - Get all organizations");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/organizations"
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Expected successful response for get all organizations");

        JsonNode responseBody = response.getJsonObjectBody();
        Assert.assertNotNull(responseBody.get("data"),
                "Response should contain data array");
        Assert.assertNotNull(responseBody.get("recordsTotal"),
                "Response should contain recordsTotal");

        // Store first org ID for subsequent tests
        JsonNode data = responseBody.get("data");
        if (data.isArray() && data.size() > 0) {
            JsonNode firstOrg = data.get(0);
            if (firstOrg.has("id")) {
                testOrgId = firstOrg.get("id").asText();
                log.info("Using organization ID for tests: {}", testOrgId);
            }
        }

        log.info("Successfully retrieved {} organizations",
                responseBody.get("recordsTotal").asInt());
    }

    @Test(priority = 2, groups = {"positive", "read", "pagination"})
    public void testGetOrganizationsWithPagination() {
        log.info("TEST: Testing pagination with start=0, length=5");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/organizations?start=0&length=5"
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Expected successful response with pagination");

        JsonNode responseBody = response.getJsonObjectBody();
        JsonNode data = responseBody.get("data");

        Assert.assertTrue(data.isArray(), "Data should be an array");
        Assert.assertTrue(data.size() <= 5,
                "Should return maximum 5 records as requested");

        log.info("Pagination test successful, returned {} records", data.size());
    }

    @Test(priority = 3, groups = {"positive", "read"}, dependsOnMethods = {"testGetAllOrganizations"})
    public void testGetOrganizationById() {
        Assumptions.assumeNotNull(testOrgId, "Test organization ID must be available");

        log.info("TEST: GET /api/organizations/{} - Get organization by ID", testOrgId);

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/organizations/" + testOrgId
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Expected successful response for get organization by ID");

        JsonNode responseBody = response.getJsonObjectBody();
        Assert.assertEquals(responseBody.get("id").asText(), testOrgId,
                "Returned organization ID should match requested ID");

        log.info("Successfully retrieved organization: {}",
                responseBody.has("name") ? responseBody.get("name").asText() : testOrgId);
    }

    @Test(priority = 4, groups = {"positive", "create"})
    public void testCreateOrganization() {
        log.info("TEST: POST /api/organizations - Create organization");

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("name", testOrgName);
        requestBody.put("description", "Test organization created by automated tests");
        requestBody.put("address", "123 Test Street");
        requestBody.put("city", "Denver");
        requestBody.put("state", "CO");
        requestBody.put("zip", "80202");
        requestBody.put("country", "USA");
        requestBody.put("phone", "303-555-0100");
        requestBody.put("email", "test@example.com");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.POST,
                "/api/organizations",
                requestBody
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Expected successful organization creation");

        JsonNode responseBody = response.getJsonObjectBody();
        Assert.assertNotNull(responseBody.get("id"),
                "Organization ID should be returned");

        String createdOrgId = responseBody.get("id").asText();
        log.info("Successfully created organization with ID: {}", createdOrgId);

        // Cleanup - delete the created organization
        ApiResponse deleteResponse = apiClient.sendRequest(
                ApiClient.Method.DELETE,
                "/api/organizations/" + createdOrgId
        );
        log.info("Cleanup: Deleted test organization");
    }

    @Test(priority = 5, groups = {"positive", "update"}, dependsOnMethods = {"testGetAllOrganizations"})
    public void testUpdateOrganization() {
        Assumptions.assumeNotNull(testOrgId, "Test organization ID must be available");

        log.info("TEST: PUT /api/organizations/{} - Update organization", testOrgId);

        // First get current org data
        ApiResponse getResponse = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/organizations/" + testOrgId
        );
        JsonNode currentData = getResponse.getJsonObjectBody();

        ObjectNode updateBody = objectMapper.createObjectNode();
        updateBody.put("name", currentData.has("name") ?
                currentData.get("name").asText() : "Updated Name");
        updateBody.put("description", "Updated by API test");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.PUT,
                "/api/organizations/" + testOrgId,
                updateBody
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Expected successful organization update");

        log.info("Successfully updated organization");
    }

    @Test(priority = 6, groups = {"positive", "search"})
    public void testSearchOrganizations() {
        log.info("TEST: Search organizations with keyword");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/organizations?keyword=Test"
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Expected successful search response");

        JsonNode responseBody = response.getJsonObjectBody();
        JsonNode data = responseBody.get("data");

        log.info("Search returned {} results", data.size());
    }

    @Test(priority = 7, groups = {"positive", "sorting"})
    public void testSortOrganizations() {
        log.info("TEST: Testing organization sorting by name ASC");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/organizations?sortField=name&sortOrder=ASC&length=10"
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Expected successful response with sorting");

        JsonNode data = response.getJsonObjectBody().get("data");

        if (data.size() >= 2) {
            String firstName = data.get(0).get("name").asText();
            String secondName = data.get(1).get("name").asText();

            Assert.assertTrue(firstName.compareToIgnoreCase(secondName) <= 0,
                    "Organizations should be sorted by name in ascending order");

            log.info("Sorting validation successful");
        } else {
            log.info("Not enough data to validate sorting");
        }
    }

    // ========================== NEGATIVE TESTING ==========================

    @Test(priority = 20, groups = {"negative", "notfound"})
    public void testGetNonExistentOrganization() {
        String invalidId = "invalid-org-id-12345";
        log.info("TEST: GET /api/organizations/{} - Get non-existent organization", invalidId);

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/organizations/" + invalidId
        );

        Assert.assertEquals(response.getResponseCode(), 404,
                "Expected 404 for non-existent organization");

        log.info("Non-existent organization correctly returned 404");
    }

    @Test(priority = 21, groups = {"negative", "validation"})
    public void testCreateOrganizationMissingName() {
        log.info("TEST: POST /api/organizations - Create without name");

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("description", "Organization without name");
        requestBody.put("city", "Denver");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.POST,
                "/api/organizations",
                requestBody
        );

        Assert.assertEquals(response.getResponseCode(), 400,
                "Expected validation error for missing name");

        log.info("Validation correctly rejected organization without name");
    }

    @Test(priority = 22, groups = {"negative", "notfound"})
    public void testUpdateNonExistentOrganization() {
        String invalidId = "invalid-org-id-67890";
        log.info("TEST: PUT /api/organizations/{} - Update non-existent organization", invalidId);

        ObjectNode updateBody = objectMapper.createObjectNode();
        updateBody.put("name", "Updated Name");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.PUT,
                "/api/organizations/" + invalidId,
                updateBody
        );

        Assert.assertEquals(response.getResponseCode(), 404,
                "Expected 404 for updating non-existent organization");

        log.info("Update non-existent organization correctly returned 404");
    }

    @Test(priority = 23, groups = {"negative", "notfound"})
    public void testDeleteNonExistentOrganization() {
        String invalidId = "invalid-org-id-99999";
        log.info("TEST: DELETE /api/organizations/{} - Delete non-existent organization", invalidId);

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.DELETE,
                "/api/organizations/" + invalidId
        );

        Assert.assertEquals(response.getResponseCode(), 404,
                "Expected 404 for deleting non-existent organization");

        log.info("Delete non-existent organization correctly returned 404");
    }
}
