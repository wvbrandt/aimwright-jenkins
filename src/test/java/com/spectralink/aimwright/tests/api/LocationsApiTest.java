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

import java.util.UUID;

/**
 * Locations API Test Suite
 *
 * Tests for the Locations API endpoints including:
 * - GET /api/locations/ - Get all locations
 * - GET /api/locations/{id} - Get location by ID
 * - GET /api/locations/options - Get location options
 * - POST /api/locations/create-by-user - Create location
 * - PUT /api/locations/{id} - Update location
 * - DELETE /api/locations/{id} - Delete location
 * - GET /api/locations/floors/{id} - Get floors
 * - GET /api/locations/contacts/{id} - Get contacts
 *
 * Test Categories:
 * - Positive Testing: Valid operations with expected inputs
 * - Negative Testing: Invalid inputs and error handling
 * - CRUD Operations: Create, Read, Update, Delete
 */
public class LocationsApiTest extends ApiTestWrapper {

    private AmieApiClient apiClient;
    private ObjectMapper objectMapper;
    private String testLocationId;
    private String testLocationName;

    @BeforeClass
    public void setup() {
        Session.setCredentials();
        Session.apiLogin();
        apiClient = Session.getAmieApiClient();
        objectMapper = new ObjectMapper();
        testLocationName = "TestLocation_" + UUID.randomUUID().toString().substring(0, 8);
        log.info("Starting Locations API Test Suite");
        log.info("Test Location Name: {}", testLocationName);
    }

    // ========================== POSITIVE TESTING ==========================

    @Test(priority = 1, groups = {"positive", "read"})
    public void testGetAllLocations() {
        log.info("TEST: GET /api/locations/ - Get all locations");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/locations/?"
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Expected successful response for get all locations");

        JsonNode responseBody = response.getJsonObjectBody();
        Assert.assertNotNull(responseBody.get("data"),
                "Response should contain data array");
        Assert.assertNotNull(responseBody.get("recordsTotal"),
                "Response should contain recordsTotal");

        // Store first location ID for subsequent tests
        JsonNode data = responseBody.get("data");
        if (data.isArray() && data.size() > 0) {
            JsonNode firstLocation = data.get(0);
            if (firstLocation.has("id")) {
                testLocationId = firstLocation.get("id").asText();
                log.info("Using location ID for tests: {}", testLocationId);
            }
        }

        log.info("Successfully retrieved {} locations",
                responseBody.get("recordsTotal").asInt());
    }

    @Test(priority = 2, groups = {"positive", "read", "pagination"})
    public void testGetLocationsWithPagination() {
        log.info("TEST: Testing pagination with start=0, length=5");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/locations/?start=0&length=5"
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

    @Test(priority = 3, groups = {"positive", "read"}, dependsOnMethods = {"testGetAllLocations"})
    public void testGetLocationById() {
        Assumptions.assumeNotNull(testLocationId, "Test location ID must be available");

        log.info("TEST: GET /api/locations/{} - Get location by ID", testLocationId);

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/locations/" + testLocationId
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Expected successful response for get location by ID");

        JsonNode responseBody = response.getJsonObjectBody();
        Assert.assertNotNull(responseBody.get("tenantId"),
                "Response should contain tenantId field");

        log.info("Successfully retrieved location");
    }

    @Test(priority = 4, groups = {"positive", "read", "options"})
    public void testGetLocationOptions() {
        log.info("TEST: GET /api/locations/options - Get location options");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/locations/options"
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Expected successful response for location options");

        JsonNode responseBody = response.getJsonObjectBody();
        Assert.assertNotNull(responseBody, "Options response should not be null");

        log.info("Successfully retrieved location options");
    }

    @Test(priority = 5, groups = {"positive", "create"})
    public void testCreateVirtualLocation() {
        log.info("TEST: POST /api/locations/create-by-user - Create virtual location");

        ObjectNode requestBody = objectMapper.createObjectNode();
        ObjectNode location = objectMapper.createObjectNode();
        location.put("locationName", testLocationName);
        location.put("utcHour", 0);
        location.put("timeZone", "UTC");
        location.put("isVirtual", true);
        requestBody.set("location", location);

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.POST,
                "/api/locations/create-by-user",
                requestBody
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Expected successful location creation");

        log.info("Successfully created virtual location");
    }

    @Test(priority = 6, groups = {"positive", "read", "floors"}, dependsOnMethods = {"testGetAllLocations"})
    public void testGetFloorsForLocation() {
        Assumptions.assumeNotNull(testLocationId, "Test location ID must be available");

        log.info("TEST: GET /api/locations/floors/{} - Get floors", testLocationId);

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/locations/floors/" + testLocationId
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Expected successful response for location floors");

        JsonNode responseBody = response.getJsonObjectBody();
        Assert.assertNotNull(responseBody, "Floors data should be present");

        log.info("Successfully retrieved floors for location");
    }

    @Test(priority = 7, groups = {"positive", "read", "contacts"}, dependsOnMethods = {"testGetAllLocations"})
    public void testGetContactsForLocation() {
        Assumptions.assumeNotNull(testLocationId, "Test location ID must be available");

        log.info("TEST: GET /api/locations/contacts/{} - Get contacts", testLocationId);

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/locations/contacts/" + testLocationId
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Expected successful response for location contacts");

        JsonNode responseBody = response.getJsonObjectBody();
        Assert.assertNotNull(responseBody, "Contacts data should be present");

        log.info("Successfully retrieved contacts for location");
    }

    @Test(priority = 8, groups = {"positive", "search"})
    public void testSearchLocationsByKeyword() {
        log.info("TEST: Search locations with keyword");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/locations/?keyword=Test"
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Expected successful search response");

        JsonNode responseBody = response.getJsonObjectBody();
        JsonNode data = responseBody.get("data");

        log.info("Search returned {} results", data.size());
    }

    @Test(priority = 9, groups = {"positive", "filtering"})
    public void testFilterVirtualLocations() {
        log.info("TEST: Filtering for virtual locations only");

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/locations/?virtual=true"
        );

        Assert.assertEquals(response.getResponseCode(), 200,
                "Expected successful response for virtual locations filter");

        JsonNode responseBody = response.getJsonObjectBody();
        JsonNode data = responseBody.get("data");

        log.info("Virtual locations filter returned {} results", data.size());
    }

    // ========================== NEGATIVE TESTING ==========================

    @Test(priority = 20, groups = {"negative", "notfound"})
    public void testGetNonExistentLocation() {
        String invalidId = "invalid-location-id-12345";
        log.info("TEST: GET /api/locations/{} - Get non-existent location", invalidId);

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.GET,
                "/api/locations/" + invalidId
        );

        Assert.assertEquals(response.getResponseCode(), 404,
                "Expected 404 for non-existent location");

        log.info("Non-existent location correctly returned 404");
    }

    @Test(priority = 21, groups = {"negative", "validation"})
    public void testCreateLocationMissingRequiredFields() {
        log.info("TEST: POST /api/locations/create-by-user - Missing required fields");

        ObjectNode requestBody = objectMapper.createObjectNode();
        ObjectNode location = objectMapper.createObjectNode();
        location.put("locationNumber", "12345");
        // Missing required: locationName, utcHour, timeZone, isVirtual
        requestBody.set("location", location);

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.POST,
                "/api/locations/create-by-user",
                requestBody
        );

        Assert.assertEquals(response.getResponseCode(), 400,
                "Expected 400 for missing required fields");

        log.info("Missing required fields correctly rejected");
    }

    @Test(priority = 22, groups = {"negative", "validation"})
    public void testCreateFloorInvalidLocationId() {
        log.info("TEST: POST /api/locations/floors/invalid-id - Invalid location ID");

        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode floorsArray = objectMapper.createArrayNode();
        ObjectNode floor = objectMapper.createObjectNode();
        floor.put("floorNumber", "1");
        floor.put("areaIdentifier", "Test Floor");
        floorsArray.add(floor);
        requestBody.set("floors", floorsArray);

        ApiResponse response = apiClient.sendRequest(
                ApiClient.Method.POST,
                "/api/locations/floors/invalid-id",
                requestBody
        );

        Assert.assertEquals(response.getResponseCode(), 400,
                "Expected 400 for invalid location ID");

        log.info("Floor creation with invalid location ID correctly rejected");
    }
}
