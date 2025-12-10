package com.spectralink.aimwright.common;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.playwright.Page;
import com.spectralink.aimwright.api.AmieApiClient;
import com.spectralink.aimwright.api.ApiResponse;
import com.spectralink.aimwright.api.DataLookup;
import com.spectralink.aimwright.pages.BasePage;
import com.spectralink.aimwright.pages.LoginPage;
import org.apache.hc.core5.http.Header;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.util.List;

import static com.spectralink.aimwright.api.ApiClient.Method.POST;
import static com.spectralink.aimwright.api.ApiClient.Method.PUT;

/**
 * Session management for authentication, credentials, and context.
 *
 * Handles:
 * - User credential storage and management
 * - UI login operations (via Playwright)
 * - API authentication (cookie-based)
 * - Organization and location context switching
 */
public class Session {
    private static AmieApiClient amieApiClient = new AmieApiClient();

    private static String apiAuthToken = "";
    private static String apiRefreshToken = "";
    private static String tokenExpiration = "";
    private static String accountName = "";
    private static String accountPassword = "";
    private static String organizationId = "";
    private static String locationIds = "";
    private static final Logger log = (Logger) LoggerFactory.getLogger(Session.class.getName());

    // ========== Credential Management ==========

    public static String getAccountName() {
        return accountName;
    }

    public static void setAccountName(String accountName) {
        Session.accountName = accountName;
    }

    public static String getAccountPassword() {
        return accountPassword;
    }

    public static void setAccountPassword(String accountPassword) {
        Session.accountPassword = accountPassword;
    }

    /**
     * Sets credentials to the default SpectraLink super user from configuration.
     */
    public static void setCredentials() {
        setCredentials(Settings.getUserSpectraLink(), Settings.getUserSpectraLinkPassword());
    }

    public static void setCredentials(String accountName, String accountPassword) {
        Session.accountName = accountName;
        Session.accountPassword = accountPassword;
    }

    public static boolean isCredentialsSet() {
        return (accountName != null && !accountName.isEmpty() &&
                accountPassword != null && !accountPassword.isEmpty());
    }

    // ========== Organization/Location Context ==========

    public static String getOrganizationId() {
        return organizationId;
    }

    public static void setOrganizationId() {
        Session.setOrganizationId(DataLookup.getOrganizationId(Settings.getOrgName()));
    }

    public static void setOrganizationId(String organizationId) {
        Session.organizationId = organizationId;
    }

    public static String getLocationIds() {
        return locationIds;
    }

    public static void setLocationIds(String locationIds) {
        Session.locationIds = locationIds;
    }

    public static void setLocationIds() {
        Session.locationIds = DataLookup.getLocationIds();
    }

    public static void setLocationIds(List<String> ids) {
        Session.locationIds = String.join(",", ids);
    }

    public static void setLocationIdsByName(List<String> locationNames) {
        Session.locationIds = DataLookup.getLocationIds(locationNames);
    }

    // ========== UI Login Methods ==========

    /**
     * Performs login using the provided page instance.
     */
    public static void withCredentials(Page page, String email, String password) {
        LoginPage loginPage = new LoginPage(page);
        loginPage.loginWithCredentials(email, password);
    }

    /**
     * Checks if user is logged in by looking for user avatar.
     */
    public static Boolean isLoggedIn(Page page) {
        BasePage basePage = new BasePage(page);
        return basePage.isLoggedIn();
    }

    /**
     * Logs in as SpectraLink super user.
     */
    public static void uiLoginSpectraLinkSuperUser(Page page) {
        setCredentials(Settings.getUserSpectraLink(), Settings.getUserSpectraLinkPassword());
        withCredentials(page, getAccountName(), getAccountPassword());
        Assert.assertTrue(isLoggedIn(page), "Login failed for SpectraLink super user");
    }

    /**
     * Logs in as read-only user.
     */
    public static void uiLoginReadOnlyUser(Page page) {
        setCredentials(Settings.getUserReadOnly(), Settings.getUserReadOnlyPassword());
        withCredentials(page, getAccountName(), getAccountPassword());
        Assert.assertTrue(isLoggedIn(page), "Login failed for read-only user");
    }

    /**
     * Logs in as admin user.
     */
    public static void uiLoginAdminUser(Page page) {
        setCredentials(Settings.getUserAdmin(), Settings.getUserAdminPassword());
        withCredentials(page, getAccountName(), getAccountPassword());
        Assert.assertTrue(isLoggedIn(page), "Login failed for admin user");
    }

    /**
     * Logs in as super user.
     */
    public static void uiLoginSuperUser(Page page) {
        setCredentials(Settings.getUserSuperUser(), Settings.getUserSuperUserPassword());
        withCredentials(page, getAccountName(), getAccountPassword());
        Assert.assertTrue(isLoggedIn(page), "Login failed for super user");
    }

    /**
     * Logs in as SP read-only user.
     */
    public static void uiLoginSpReadOnlyUser(Page page) {
        setCredentials(Settings.getUserSPReadOnly(), Settings.getUserSPReadOnlyPassword());
        withCredentials(page, getAccountName(), getAccountPassword());
        Assert.assertTrue(isLoggedIn(page), "Login failed for SP read-only user");
    }

    /**
     * Logs in as SP super user.
     */
    public static void uiLoginSpSuperUser(Page page) {
        setCredentials(Settings.getUserSPSuperUser(), Settings.getUserSPSuperUserPassword());
        withCredentials(page, getAccountName(), getAccountPassword());
        Assert.assertTrue(isLoggedIn(page), "Login failed for SP super user");
    }

    // ========== API Authentication ==========

    public static void apiLogin() {
        apiLogin(getAccountName(), getAccountPassword());
    }

    private static void apiLogin(String username, String password) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("username", username);
        requestBody.put("password", password);

        List<Header> responseHeader = List.of(amieApiClient.sendRequest(POST, "auth/login", requestBody).getResponseHeaders());
        for (Header header : responseHeader) {
            if (header.toString().toLowerCase().startsWith("set-cookie:")) {
                apiAuthToken = header.toString().split(":", 2)[1].trim();
                log.debug("API login successful, cookie obtained");
                break;
            }
        }
    }

    public static String getCookie() {
        return apiAuthToken;
    }

    public static AmieApiClient getAmieApiClient() {
        if (tokenExpiration.isEmpty() || Long.parseLong(tokenExpiration) <= System.currentTimeMillis() / 1000L) {
            if (!isCredentialsSet()) {
                setCredentials();
            }
        }
        return amieApiClient;
    }

    // ========== Context Switching ==========

    public static void setOrganization(String organizationId) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("organizationId", organizationId);
        try {
            ApiResponse response = Session.getAmieApiClient().sendRequest(PUT, "/api/identity/org/set-current", objectMapper.writeValueAsString(requestBody));
            if (response.getResponseCode() == 200) {
                log.debug("Switched organization to {}", organizationId);
            } else {
                log.error("Failed to switch organization due to error code {}: {}", response.getResponseCode(), response.getJsonObjectBody().get("message"));
            }
        } catch (JsonProcessingException jpe) {
            log.error("Could not convert payload to json");
        }
    }

    public static String returnLocationId(String location) {
        try {
            ApiResponse response = Session.getAmieApiClient().sendGetRequest("locations/?start=0&length=55&sortField=created&sortOrder=DESC");
            if (response.getResponseCode() == 200) {
                JsonNode responseData = response.getJsonObjectBody().get("data");
                for (int attributes = 0; attributes < responseData.size(); attributes++) {
                    String tenantName = responseData.get(attributes).findValues("tenant_name").get(0).toString();
                    if (location.equals(tenantName.substring(1, tenantName.length() - 1))) {
                        String idValues = String.valueOf(responseData.get(attributes).findValues("id").get(0));
                        return idValues.substring(1, idValues.length() - 1);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Could not convert payload to JSON");
        }
        return null;
    }

    public static void setLocation(String locationId) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("locationId", locationId);
        try {
            ApiResponse response = Session.getAmieApiClient().sendRequest(PUT, "/api/locations/set-current", objectMapper.writeValueAsString(requestBody));
            if (response.getResponseCode() == 200) {
                log.debug("Switched location to {}", locationId);
            } else {
                log.error("Failed to switch location due to error code {}: {}", response.getResponseCode(), response.getJsonObjectBody().get("message"));
            }
        } catch (JsonProcessingException jpe) {
            log.error("Could not convert payload to json");
        }
    }

    // ========== Utility Methods ==========

    public void reset() {
        setCredentials();
        setOrganizationId();
        setLocationIds();
    }

    public static void logPrettyJsonResponse(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Object jsonObject = objectMapper.readValue(json, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
            String[] lines = prettyJson.split("\\r?\\n");
            for (String eachLine : lines) {
                log.debug(eachLine);
            }
        } catch (JsonMappingException jme) {
            log.error("Could not log the json blob: {}", jme.getMessage());
        } catch (JsonProcessingException jpe) {
            log.error("Could not log the json blob: {}", jpe.getMessage());
        }
    }
}
