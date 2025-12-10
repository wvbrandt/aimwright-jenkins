package com.spectralink.aimwright.tests.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
 * Devices API Test Suite
 *
 * Tests for the Devices Management API endpoints including:
 * - GET /api/devices/ - Get all devices
 * - GET /api/devices/total - Get device count
 * - GET /api/devices/models - Get device models
 * - GET /api/devices/summary - Get device summary
 * - GET /api/devices/detail/{id} - Get device details
 * - GET /api/devices/call/history/{serial} - Get call history
 *
 * Test Categories:
 * - Positive Testing: Valid operations with expected inputs
 * - Negative Testing: Invalid inputs and error handling
 */
public class DevicesApiTest extends ApiTestWrapper {

    private AmieApiClient apiClient;
    private ObjectMapper objectMapper;
    private String testDeviceSerial;
    private String testDeviceId;

    @BeforeClass
    public void setup() {
        Session.setCredentials();
        Session.apiLogin();
        apiClient = Session.getAmieApiClient();
        objectMapper = new ObjectMapper();
        log.info("Starting Devices API Test Suite");
    }

    // ========================== POSITIVE TESTING ==========================

    @Test(priority = 1, groups = {"positive", "read"})
    public void testGetAllDevices() {
        log.info("TEST: GET /api/devices/ - Get all devices");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/devices/"
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Expected successful response for get all devices");

        JsonNode responseBody = response.getJsonObjectBody();
        Assert.assertNotNull(responseBody, "Response should contain data");

        // Store first device for subsequent tests
        if (responseBody.has("data") && responseBody.get("data").isArray()) {
            JsonNode data = responseBody.get("data");
            if (data.size() > 0) {
                JsonNode firstDevice = data.get(0);
                if (firstDevice.has("serial")) {
                    testDeviceSerial = firstDevice.get("serial").asText();
                    testDeviceId = testDeviceSerial;
                    log.info("Using device serial for tests: {}", testDeviceSerial);
                }
            }
        }

        log.info("Successfully retrieved devices list");
    }

    @Test(priority = 2, groups = {"positive", "read", "count"})
    public void testGetDeviceTotal() {
        log.info("TEST: GET /api/devices/total - Get total device count");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/devices/total"
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Expected successful response for device total count");

        JsonNode responseBody = response.getJsonObjectBody();
        Assert.assertNotNull(responseBody, "Total count response should be present");

        log.info("Successfully retrieved device total count");
    }

    @Test(priority = 3, groups = {"positive", "read", "models"})
    public void testGetDeviceModels() {
        log.info("TEST: GET /api/devices/models - Get device models");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/devices/models"
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Expected successful response for device models");

        JsonNode responseBody = response.getJsonObjectBody();
        Assert.assertNotNull(responseBody, "Models response should be present");

        log.info("Successfully retrieved device models");
    }

    @Test(priority = 4, groups = {"positive", "read", "summary"})
    public void testGetDeviceSummary() {
        log.info("TEST: GET /api/devices/summary - Get device summary");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/devices/summary"
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Expected successful response for device summary");

        JsonNode responseBody = response.getJsonObjectBody();
        Assert.assertNotNull(responseBody, "Summary response should be present");

        log.info("Successfully retrieved device summary");
    }

    @Test(priority = 5, groups = {"positive", "read", "details"}, dependsOnMethods = {"testGetAllDevices"})
    public void testGetDeviceDetails() {
        Assumptions.assumeNotNull(testDeviceId, "Test device ID must be available");

        log.info("TEST: GET /api/devices/detail/{} - Get device details", testDeviceId);

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/devices/detail/" + testDeviceId
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Expected successful response for device details");

        JsonNode responseBody = response.getJsonObjectBody();
        Assert.assertNotNull(responseBody, "Device details should be present");

        log.info("Successfully retrieved device details");
    }

    @Test(priority = 6, groups = {"positive", "read", "history"}, dependsOnMethods = {"testGetAllDevices"})
    public void testGetCallHistory() {
        Assumptions.assumeNotNull(testDeviceSerial, "Test device serial must be available");

        log.info("TEST: GET /api/devices/call/history/{} - Get call history", testDeviceSerial);

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/devices/call/history/" + testDeviceSerial
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Expected successful response for call history");

        JsonNode responseBody = response.getJsonObjectBody();
        Assert.assertNotNull(responseBody, "Call history should be present");

        log.info("Successfully retrieved call history");
    }

    @Test(priority = 7, groups = {"positive", "read", "checkin"})
    public void testGetCheckInSummary() {
        log.info("TEST: GET /api/devices/summary/check-in - Get check-in summary");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/devices/summary/check-in"
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Expected successful response for check-in summary");

        JsonNode responseBody = response.getJsonObjectBody();
        Assert.assertNotNull(responseBody, "Check-in summary should be present");

        log.info("Successfully retrieved check-in summary");
    }

    // ========================== NEGATIVE TESTING ==========================

    @Test(priority = 20, groups = {"negative", "notfound"})
    public void testGetDeviceDetailsInvalidId() {
        String invalidId = "invalid-device-id-12345";
        log.info("TEST: GET /api/devices/detail/{} - Get details for invalid device", invalidId);

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/devices/detail/" + invalidId
        );

        Assert.assertEquals(response.getResponseCode(), 404,
                "Expected 404 for non-existent device");

        log.info("Invalid device details correctly returned 404");
    }

    @Test(priority = 21, groups = {"negative", "notfound"})
    public void testGetCallHistoryInvalidSerial() {
        String invalidSerial = "INVALID-SERIAL-12345";
        log.info("TEST: GET /api/devices/call/history/{} - Get history for invalid device", invalidSerial);

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/devices/call/history/" + invalidSerial
        );

        // May return 404 or empty result
        Assert.assertTrue(response.getResponseCode() == 200 || response.getResponseCode() == 404,
                "Expected 200 with empty result or 404 for invalid serial");

        log.info("Invalid device history handled correctly");
    }
}
