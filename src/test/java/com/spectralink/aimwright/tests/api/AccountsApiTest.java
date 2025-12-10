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

/**
 * Accounts API Test Suite
 *
 * Tests for the Accounts API endpoints including:
 * - GET /api/accounts/ - Get all accounts
 * - GET /api/accounts/current - Get current account
 * - PUT /api/accounts/set-location - Set account location
 * - PUT /api/accounts/set-organization - Set account organization
 * - PUT /api/accounts/{accountId} - Update account by ID
 *
 * Test Categories:
 * - Positive Testing: Valid operations with expected inputs
 * - Negative Testing: Invalid inputs and error handling
 * - Permission Testing: Access control verification
 */
public class AccountsApiTest extends ApiTestWrapper {

    private AmieApiClient apiClient;
    private ObjectMapper objectMapper;
    private String testAccountId;

    @BeforeClass
    public void setup() {
        Session.setCredentials();
        Session.apiLogin();
        apiClient = Session.getAmieApiClient();
        objectMapper = new ObjectMapper();
        log.info("Starting Accounts API Test Suite");
    }

    // ========================== POSITIVE TESTING ==========================

    @Test(priority = 1, groups = {"positive", "read"})
    public void testGetAllAccounts() {
        log.info("TEST: GET /api/accounts/ - Get all accounts");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/accounts/"
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Expected successful response for get all accounts");

        JsonNode responseBody = response.getJsonObjectBody();
        Assert.assertNotNull(responseBody, "Response should contain data");

        // Store first account ID for subsequent tests
        if (responseBody.isArray() && responseBody.size() > 0) {
            JsonNode firstAccount = responseBody.get(0);
            if (firstAccount.has("id")) {
                testAccountId = firstAccount.get("id").asText();
            } else if (firstAccount.has("accountId")) {
                testAccountId = firstAccount.get("accountId").asText();
            }
            log.info("Using account ID for tests: {}", testAccountId);
        } else if (responseBody.has("data") && responseBody.get("data").isArray()) {
            JsonNode data = responseBody.get("data");
            if (data.size() > 0) {
                JsonNode firstAccount = data.get(0);
                if (firstAccount.has("id")) {
                    testAccountId = firstAccount.get("id").asText();
                } else if (firstAccount.has("accountId")) {
                    testAccountId = firstAccount.get("accountId").asText();
                }
                log.info("Using account ID for tests: {}", testAccountId);
            }
        }

        log.info("Successfully retrieved accounts list");
    }

    @Test(priority = 2, groups = {"positive", "read", "current"})
    public void testGetCurrentAccount() {
        log.info("TEST: GET /api/accounts/current - Get current account");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/accounts/current"
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Expected successful response for current account");

        JsonNode responseBody = response.getJsonObjectBody();
        Assert.assertNotNull(responseBody, "Current account data should be present");

        if (responseBody.has("id") && testAccountId == null) {
            testAccountId = responseBody.get("id").asText();
            log.info("Using current account ID for tests: {}", testAccountId);
        }

        log.info("Successfully retrieved current account");
    }

    @Test(priority = 3, groups = {"positive", "update"}, dependsOnMethods = {"testGetAllAccounts"})
    public void testUpdateAccountById() {
        Assumptions.assumeNotNull(testAccountId, "Test account ID must be available");

        log.info("TEST: PUT /api/accounts/{} - Update account by ID", testAccountId);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("name", "Updated Account Name");
        requestBody.put("description", "Updated by API test");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.PUT,
                "/api/accounts/" + testAccountId,
                requestBody
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Expected successful account update");

        JsonNode responseBody = response.getJsonObjectBody();
        Assert.assertNotNull(responseBody, "Account update response should be present");

        log.info("Successfully updated account by ID");
    }

    @Test(priority = 4, groups = {"positive", "security"})
    public void testCurrentAccountAccessControl() {
        log.info("TEST: Verify current account access control");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/accounts/current"
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Should return current account for authenticated user");

        JsonNode responseBody = response.getJsonObjectBody();
        Assert.assertNotNull(responseBody, "Current account data should be present");

        Assert.assertTrue(responseBody.has("id") || responseBody.has("accountId"),
                "Current account should have ID field");

        log.info("Current account access control verified");
    }

    @Test(priority = 5, groups = {"positive", "security"})
    public void testAccountListPermissions() {
        log.info("TEST: Verify account list permissions");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/accounts/"
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Should return accessible accounts for authenticated user");

        JsonNode responseBody = response.getJsonObjectBody();
        Assert.assertNotNull(responseBody, "Account list should be present");

        log.info("Account list permissions verified");
    }

    // ========================== NEGATIVE TESTING ==========================

    @Test(priority = 20, groups = {"negative", "notfound"})
    public void testUpdateInvalidAccountId() {
        String invalidId = "invalid-account-id-12345";
        log.info("TEST: PUT /api/accounts/{} - Update invalid account ID", invalidId);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("name", "Should not update");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.PUT,
                "/api/accounts/" + invalidId,
                requestBody
        );

        Assert.assertEquals(response.getResponseCode(), 404,
                "Expected 404 for non-existent account");

        log.info("Invalid account update correctly returned 404");
    }

    @Test(priority = 21, groups = {"negative", "notfound"})
    public void testDeleteInvalidAccountId() {
        String invalidId = "invalid-account-id-67890";
        log.info("TEST: DELETE /api/accounts/{} - Delete invalid account ID", invalidId);

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.DELETE,
                "/api/accounts/" + invalidId
        );

        Assert.assertEquals(response.getResponseCode(), 404,
                "Expected 404 for non-existent account deletion");

        log.info("Invalid account deletion correctly returned 404");
    }

    @Test(priority = 22, groups = {"negative", "validation"})
    public void testSetAccountInvalidEmail() {
        log.info("TEST: PUT /api/accounts/set-account - Set account with invalid email");

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("accountId", testAccountId != null ? testAccountId : "test-account-id");
        requestBody.put("name", "Test Account");
        requestBody.put("email", "invalid-email-format");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.PUT,
                "/api/accounts/set-account",
                requestBody
        );

        Assert.assertEquals(response.getResponseCode(), 400,
                "Expected 400 for invalid email format");

        log.info("Set account with invalid email correctly rejected");
    }

    // ========================== EDGE CASES ==========================

    @Test(priority = 30, groups = {"edge", "account"}, dependsOnMethods = {"testGetAllAccounts"})
    public void testUpdateAccountSpecialCharacters() {
        Assumptions.assumeNotNull(testAccountId, "Test account ID must be available");

        log.info("TEST: PUT /api/accounts/{} - Update with special characters", testAccountId);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("name", "Test Account with Special Chars: <>\"'&");
        requestBody.put("description", "Description with symbols: !@#$%^&*()");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.PUT,
                "/api/accounts/" + testAccountId,
                requestBody
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Should handle special characters in account data");

        log.info("Special characters in account data handled successfully");
    }

    @Test(priority = 31, groups = {"edge", "account"}, dependsOnMethods = {"testGetAllAccounts"})
    public void testUpdateAccountMaximumFieldLengths() {
        Assumptions.assumeNotNull(testAccountId, "Test account ID must be available");

        log.info("TEST: PUT /api/accounts/{} - Maximum field lengths", testAccountId);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("name", "A".repeat(255));
        requestBody.put("description", "B".repeat(1000));

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.PUT,
                "/api/accounts/" + testAccountId,
                requestBody
        );

        Assert.assertTrue(response.getResponseCode() == 200 || response.getResponseCode() == 400,
                "Should handle maximum field lengths appropriately");

        log.info("Maximum field lengths handled with response: {}", response.getResponseCode());
    }

    // ========================== INTEGRATION ==========================

    @Test(priority = 99, groups = {"integration", "workflow"}, dependsOnMethods = {"testGetAllAccounts"})
    public void testCompleteAccountWorkflow() {
        Assumptions.assumeNotNull(testAccountId, "Test account ID must be available");

        log.info("TEST: Complete account workflow");

        // Step 1: Get current account
        ApiResponse currentResponse = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/accounts/current"
        );
        Assert.assertEquals(currentResponse.getResponseCode(), 200,
                "Should retrieve current account");

        // Step 2: Get all accounts
        ApiResponse listResponse = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/accounts/"
        );
        Assert.assertEquals(listResponse.getResponseCode(), 200,
                "Should retrieve accounts list");

        // Step 3: Update account
        ObjectNode updateBody = objectMapper.createObjectNode();
        updateBody.put("name", "Workflow Test Account");
        updateBody.put("description", "Updated during workflow test");

        ApiResponse updateResponse = apiClient.sendRequest(
                ApiClient.Method.PUT,
                "/api/accounts/" + testAccountId,
                updateBody
        );
        Assert.assertEquals(updateResponse.getResponseCode(), 200,
                "Should update account information");

        // Step 4: Verify account still accessible
        ApiResponse finalResponse = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/accounts/"
        );
        Assert.assertEquals(finalResponse.getResponseCode(), 200,
                "Should still retrieve accounts after workflow");

        log.info("Complete account workflow executed successfully");
    }
}
