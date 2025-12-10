package com.spectralink.aimwright.common;

import com.spectralink.aimwright.api.DataLookup;
import org.apache.commons.io.FileUtils;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Configuration loader for the Aimwright Playwright test framework.
 * Loads settings from RunConfiguration.json and provides static accessors.
 */
public class Defaults {

    private static final String projectDirectory = System.getProperty("user.dir");
    private static final String configFileName = "RunConfiguration.json";
    private static final Logger log = (Logger) LoggerFactory.getLogger(Defaults.class.getName());
    private static JsonNode jsonRoot;

    // USB Host settings
    private static String usbHostAddress;
    private static String usbHostAccount;
    private static String usbHostPassword;

    // Browser settings (Playwright-specific)
    private static String browserApplication;
    private static Boolean browserHeadless;
    private static Integer browserDefaultTimeout;
    private static Boolean browserFailureScreenshot;
    private static Boolean browserTraceOnFailure;
    private static File screenshotDirectory = null;
    private static File traceDirectory = null;
    private static List<String> browserOptionsList = new ArrayList<>();

    // User credentials
    private static String userReadOnly;
    private static String userReadOnlyPassword;
    private static String userAdmin;
    private static String userAdminPassword;
    private static String userSuperUser;
    private static String userSuperUserPassword;
    private static String userSPSuperUser;
    private static String userSPSuperUserPassword;
    private static String userSPReadOnly;
    private static String userSPReadOnlyPassword;
    private static String userSpectraLink;
    private static String userSpectraLinkPassword;

    // Instance URLs
    private static String uiInstance;
    private static String apiInstance;

    // Run settings
    private static String accountName;
    private static String orgName;
    private static String orgId;
    private static String locationName;
    private static String locationId;
    private static String accountId;
    private static String gatewayAddress;
    private static String targetBattery;
    private static String targetDevice;
    private static String targetDevicePhoneA;
    private static String targetBatteryPhoneA;
    private static String targetDevicePhoneB;
    private static String targetBatteryPhoneB;
    private static String targetDevicePhoneC;
    private static String targetBatteryPhoneC;
    private static String targetDevicePhoneD;
    private static String targetBatteryPhoneD;
    private static Boolean failOnUnexpectedResponseCode;
    private static String locationDelete;
    private static String orgDelete;
    private static Boolean debugMode;

    public static void loadDefaults() {
        if (jsonRoot == null) {
            Path defaultConfigPath = Paths.get(projectDirectory, "src", "main", "resources", configFileName);
            try {
                byte[] jsonData = Files.readAllBytes(defaultConfigPath);
                ObjectMapper objectMapper = new ObjectMapper();
                jsonRoot = objectMapper.readTree(jsonData);

                // USB Host settings
                JsonNode usbhostSettings = jsonRoot.path("usbhost");
                usbHostAddress = usbhostSettings.path("usbHostAddress").asText();
                usbHostAccount = usbhostSettings.path("usbHostAccount").asText();
                usbHostPassword = usbhostSettings.path("usbHostPassword").asText();

                // Browser settings (Playwright-adapted)
                JsonNode browserSettings = jsonRoot.path("browser");
                browserApplication = browserSettings.path("application").asText("chromium");
                browserHeadless = browserSettings.path("headless").asBoolean(false);
                browserDefaultTimeout = browserSettings.path("defaultTimeoutMs").asInt(30000);
                browserFailureScreenshot = browserSettings.path("takeFailureScreenshot").asBoolean(true);
                browserTraceOnFailure = browserSettings.path("traceOnFailure").asBoolean(true);

                // Setup screenshot directory
                if (browserFailureScreenshot) {
                    screenshotDirectory = Paths.get(projectDirectory, "target", "failure-screenshots").toFile();
                    screenshotDirectory.mkdirs();
                    if (screenshotDirectory.exists()) {
                        try {
                            FileUtils.cleanDirectory(screenshotDirectory);
                        } catch (IOException ioe) {
                            log.error("Could not remove old screenshot files from {}", screenshotDirectory);
                        }
                    }
                }

                // Setup trace directory
                if (browserTraceOnFailure) {
                    traceDirectory = Paths.get(projectDirectory, "target", "logs", "run_traces").toFile();
                    traceDirectory.mkdirs();
                }

                // Browser options (if any)
                JsonNode optionsBlob = jsonRoot.path("browser").path("options");
                Iterator<JsonNode> elements = optionsBlob.elements();
                while (elements.hasNext()) {
                    JsonNode option = elements.next();
                    browserOptionsList.add(option.asText());
                }

                // User credentials
                JsonNode userSettings = jsonRoot.path("user");
                userReadOnly = userSettings.path("readOnly").asText();
                userReadOnlyPassword = userSettings.path("readOnlyPassword").asText();
                userAdmin = userSettings.path("admin").asText();
                userAdminPassword = userSettings.path("adminPassword").asText();
                userSuperUser = userSettings.path("superUser").asText();
                userSuperUserPassword = userSettings.path("superUserPassword").asText();
                userSPSuperUser = userSettings.path("SPSuperUser").asText();
                userSPSuperUserPassword = userSettings.path("SPSuperUserPassword").asText();
                userSPReadOnly = userSettings.path("SPReadOnly").asText();
                userSPReadOnlyPassword = userSettings.path("SPReadOnlyPassword").asText();
                userSpectraLink = userSettings.path("SpectraLinkSuperUser").asText();
                userSpectraLinkPassword = userSettings.path("SpectraLinkSuperUserPassword").asText();

                // Run settings
                JsonNode runSettings = jsonRoot.path("run-settings");
                uiInstance = runSettings.path("uiInstance").asText();
                apiInstance = runSettings.path("apiInstance").asText();
                accountName = runSettings.path("accountName").asText();
                orgName = runSettings.path("orgName").asText();
                orgId = "";
                locationName = runSettings.path("locationName").asText();
                locationId = "";
                accountId = "";
                gatewayAddress = runSettings.path("gatewayAddress").asText();
                targetBattery = runSettings.path("targetBattery").asText();
                targetDevice = runSettings.path("targetDevice").asText();
                targetDevicePhoneA = runSettings.path("targetDevicePhoneA").asText();
                targetBatteryPhoneA = runSettings.path("targetBatteryPhoneA").asText();
                targetDevicePhoneB = runSettings.path("targetDevicePhoneB").asText();
                targetBatteryPhoneB = runSettings.path("targetBatteryPhoneB").asText();
                targetDevicePhoneC = runSettings.path("targetDevicePhoneC").asText();
                targetBatteryPhoneC = runSettings.path("targetBatteryPhoneC").asText();
                targetDevicePhoneD = runSettings.path("targetDevicePhoneD").asText();
                targetBatteryPhoneD = runSettings.path("targetBatteryPhoneD").asText();
                failOnUnexpectedResponseCode = runSettings.path("failOnUnexpectedResponseCode").asBoolean();
                locationDelete = runSettings.path("deleteLocation").asText();
                orgDelete = runSettings.path("deleteOrganization").asText();
                debugMode = runSettings.path("debugMode").asBoolean();

                log.debug("Loaded the configuration file from {}", defaultConfigPath);
            } catch (IOException ioe) {
                log.error("Could not read the configuration file: {}", ioe.getMessage());
            }
        }
    }

    public Boolean isJsonLoaded() {
        return jsonRoot != null;
    }

    public static JsonNode getJsonRoot() {
        return jsonRoot;
    }

    public static String getProjectDirectory() {
        return projectDirectory;
    }

    public String getConfigFileName() {
        return configFileName;
    }

    // USB Host getters
    public static String getUsbHostAddress() {
        return usbHostAddress;
    }

    public static String getUsbHostAccount() {
        return usbHostAccount;
    }

    public static String getUsbHostPassword() {
        return usbHostPassword;
    }

    // Browser getters (Playwright-specific)
    public static String getBrowserApplication() {
        return browserApplication;
    }

    public static Boolean getBrowserHeadless() {
        return browserHeadless;
    }

    public static Integer getBrowserDefaultTimeout() {
        return browserDefaultTimeout;
    }

    public static Boolean getBrowserFailureScreenshot() {
        return browserFailureScreenshot;
    }

    public static Boolean getBrowserTraceOnFailure() {
        return browserTraceOnFailure;
    }

    public static File getScreenshotDirectory() {
        return screenshotDirectory;
    }

    public static File getTraceDirectory() {
        return traceDirectory;
    }

    public static List<String> getBrowserOptionsList() {
        return browserOptionsList;
    }

    // User credential getters
    public static String getUserReadOnly() {
        return userReadOnly;
    }

    public static String getUserReadOnlyPassword() {
        return userReadOnlyPassword;
    }

    public static String getUserAdmin() {
        return userAdmin;
    }

    public static String getUserAdminPassword() {
        return userAdminPassword;
    }

    public static String getUserSuperUser() {
        return userSuperUser;
    }

    public static String getUserSuperUserPassword() {
        return userSuperUserPassword;
    }

    public static String getUserSPSuperUser() {
        return userSPSuperUser;
    }

    public static String getUserSPSuperUserPassword() {
        return userSPSuperUserPassword;
    }

    public static String getUserSPReadOnly() {
        return userSPReadOnly;
    }

    public static String getUserSPReadOnlyPassword() {
        return userSPReadOnlyPassword;
    }

    public static String getUserSpectraLink() {
        return userSpectraLink;
    }

    public static String getUserSpectraLinkPassword() {
        return userSpectraLinkPassword;
    }

    // Instance URL getters
    public static String getUiInstance() {
        return uiInstance;
    }

    public static String getApiInstance() {
        return apiInstance;
    }

    // Run settings getters
    public static String getAccountName() {
        return accountName;
    }

    public static String getOrgName() {
        return orgName;
    }

    public static String getOrgId() {
        if (orgId == null || orgId.isEmpty()) {
            if (orgName != null && !orgName.isEmpty()) {
                orgId = DataLookup.getOrganizationId(orgName);
            } else {
                log.warn("Could not get the org id: no org name in config");
            }
        }
        return orgId;
    }

    public static String getLocationName() {
        return locationName;
    }

    public static String getLocationId() {
        if (locationId == null || locationId.isEmpty()) {
            if (locationName != null && !locationName.isEmpty()) {
                locationId = DataLookup.getLocationId(locationName);
            } else {
                log.warn("Could not get the location id: no location name in config");
            }
        }
        return locationId;
    }

    public static String getAccountId() {
        if (accountId == null || accountId.isEmpty()) {
            if (accountName != null && !accountName.isEmpty()) {
                return DataLookup.getAccountId(accountName);
            } else {
                log.warn("Could not get the account id: no account name in config");
            }
        }
        return accountId;
    }

    public static String getGatewayAddress() {
        return gatewayAddress;
    }

    public static String getTargetBattery() {
        return targetBattery;
    }

    public static String getTargetDevice() {
        return targetDevice;
    }

    public static String getTargetDevicePhoneA() {
        return targetDevicePhoneA;
    }

    public static String getTargetBatteryPhoneA() {
        return targetBatteryPhoneA;
    }

    public static String getTargetDevicePhoneB() {
        return targetDevicePhoneB;
    }

    public static String getTargetBatteryPhoneB() {
        return targetBatteryPhoneB;
    }

    public static String getTargetDevicePhoneC() {
        return targetDevicePhoneC;
    }

    public static String getTargetBatteryPhoneC() {
        return targetBatteryPhoneC;
    }

    public static String getTargetDevicePhoneD() {
        return targetDevicePhoneD;
    }

    public static String getTargetBatteryPhoneD() {
        return targetBatteryPhoneD;
    }

    public static Boolean getFailOnUnexpectedResponseCode() {
        return failOnUnexpectedResponseCode;
    }

    public static String getDeleteLocation() {
        return locationDelete;
    }

    public static String getDeleteOrg() {
        return orgDelete;
    }

    public static Boolean isDebugMode() {
        return debugMode;
    }
}
