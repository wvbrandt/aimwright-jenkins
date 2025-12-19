package com.spectralink.aimwright.common;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import com.spectralink.aimwright.api.DataLookup;

public class Settings {

    private static Properties properties;
    private static String settingsPath = "src/main/resources/settings.properties";
    private static final Logger log = (Logger) LoggerFactory.getLogger(Settings.class.getName());

    // Directory management fields
    private static File screenshotDirectory = null;
    private static File traceDirectory = null;
    private static final String projectDirectory = System.getProperty("user.dir");

    // Lazy loading cache for API-computed values
    private static String orgId = "";
    private static String locationId = "";
    private static String accountId = "";

    public static void loadAll() {
        loadAll(settingsPath);
    }

    public static void loadAll(String filePath) {
        properties = new Properties();
        try (InputStream input = new FileInputStream(filePath)) {
            properties.load(input);
            log.info("Loaded run settings file at {}", settingsPath);
        } catch (IOException ioe) {
            log.error("Could not load settings file {}: {}", settingsPath, ioe.getMessage());
        }
    }

    public static String get(String key) {
        // Check system properties first (allows command-line override)
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            return systemValue;
        }

        // Fall back to properties file
        if (properties == null) {
            loadAll();
            if (properties == null) return null;
        }
        return properties.getProperty(key);
    }

    // Type conversion helpers
    private static Boolean getBoolean(String key, Boolean defaultValue) {
        String value = get(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    private static Integer getInteger(String key, Integer defaultValue) {
        String value = get(key);
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            log.warn("Invalid integer value for key {}: {}", key, value);
            return defaultValue;
        }
    }

    private static List<String> getList(String key) {
        String value = get(key);
        if (value == null || value.isEmpty()) {
            return Arrays.asList();
        }
        return Arrays.asList(value.split(","));
    }

    // Initialize test directories (called from BaseTest)
    public static void initializeTestDirectories() {
        Boolean takeScreenshot = getBoolean("browser.screenshot.on.failure", true);
        Boolean takeTrace = getBoolean("browser.trace.on.failure", true);

        // Setup screenshot directory
        if (takeScreenshot) {
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
        if (takeTrace) {
            traceDirectory = Paths.get(projectDirectory, "target", "logs", "run_traces").toFile();
            traceDirectory.mkdirs();
        }
    }

    // USB Host getters
    public static String getUsbHostAddress() {
        return get("usb.host.address");
    }

    public static String getUsbHostAccount() {
        return get("usb.host.account");
    }

    public static String getUsbHostPassword() {
        return get("usb.host.password");
    }

    // Browser getters
    public static String getBrowserApplication() {
        String app = get("browser.application");
        return app != null ? app : "chromium";
    }

    public static Boolean getBrowserHeadless() {
        return getBoolean("browser.headless", false);
    }

    public static Integer getBrowserDefaultTimeout() {
        return getInteger("browser.timeout", 30000);
    }

    public static Boolean getBrowserFailureScreenshot() {
        return getBoolean("browser.screenshot.on.failure", true);
    }

    public static Boolean getBrowserTraceOnFailure() {
        return getBoolean("browser.trace.on.failure", true);
    }

    public static File getScreenshotDirectory() {
        return screenshotDirectory;
    }

    public static File getTraceDirectory() {
        return traceDirectory;
    }

    public static List<String> getBrowserOptionsList() {
        return getList("browser.options");
    }

    // User credential getters
    public static String getUserReadOnly() {
        return get("user.readonly.name");
    }

    public static String getUserReadOnlyPassword() {
        return get("user.readonly.password");
    }

    public static String getUserAdmin() {
        return get("user.admin.name");
    }

    public static String getUserAdminPassword() {
        return get("user.admin.password");
    }

    public static String getUserSuperUser() {
        return get("user.superuser.name");
    }

    public static String getUserSuperUserPassword() {
        return get("user.superuser.password");
    }

    public static String getUserSPSuperUser() {
        return get("user.sp.superuser.name");
    }

    public static String getUserSPSuperUserPassword() {
        return get("user.sp.superuser.password");
    }

    public static String getUserSPReadOnly() {
        return get("user.sp.readonly.name");
    }

    public static String getUserSPReadOnlyPassword() {
        return get("user.sp.readonly.password");
    }

    public static String getUserSpectraLink() {
        return get("user.spectralink.name");
    }

    public static String getUserSpectraLinkPassword() {
        return get("user.spectralink.password");
    }

    // Instance URL getters
    public static String getUiInstance() {
        return get("instance.ui");
    }

    public static String getApiInstance() {
        return get("instance.api");
    }

    // Run settings getters
    public static String getAccountName() {
        return get("run.account.name");
    }

    public static String getOrgName() {
        return get("run.org.name");
    }

    public static String getOrgId() {
        if (orgId == null || orgId.isEmpty()) {
            String orgName = get("run.org.name");
            if (orgName != null && !orgName.isEmpty()) {
                orgId = DataLookup.getOrganizationId(orgName);
            } else {
                log.warn("Could not get the org id: no org name in config");
            }
        }
        return orgId;
    }

    public static String getLocationName() {
        return get("run.location.name");
    }

    public static String getLocationId() {
        if (locationId == null || locationId.isEmpty()) {
            String locationName = get("run.location.name");
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
            String accountName = get("run.account.name");
            if (accountName != null && !accountName.isEmpty()) {
                return DataLookup.getAccountId(accountName);
            } else {
                log.warn("Could not get the account id: no account name in config");
            }
        }
        return accountId;
    }

    public static String getGatewayAddress() {
        return get("run.gateway.address");
    }

    public static String getTargetBattery() {
        return get("run.target.battery");
    }

    public static String getTargetDevice() {
        return get("run.target.device");
    }

    public static String getTargetDevicePhoneA() {
        return get("run.target.device.phone.a");
    }

    public static String getTargetBatteryPhoneA() {
        return get("run.target.battery.phone.a");
    }

    public static String getTargetDevicePhoneB() {
        return get("run.target.device.phone.b");
    }

    public static String getTargetBatteryPhoneB() {
        return get("run.target.battery.phone.b");
    }

    public static String getTargetDevicePhoneC() {
        return get("run.target.device.phone.c");
    }

    public static String getTargetBatteryPhoneC() {
        return get("run.target.battery.phone.c");
    }

    public static String getTargetDevicePhoneD() {
        return get("run.target.device.phone.d");
    }

    public static String getTargetBatteryPhoneD() {
        return get("run.target.battery.phone.d");
    }

    public static Boolean getFailOnUnexpectedResponseCode() {
        return getBoolean("run.fail.on.unexpected.response", false);
    }

    public static String getDeleteLocation() {
        return get("run.delete.location");
    }

    public static String getDeleteOrg() {
        return get("run.delete.organization");
    }

    public static Boolean isDebugMode() {
        return getBoolean("run.debug.mode", false);
    }

    // Utility getter
    public static String getProjectDirectory() {
        return projectDirectory;
    }
}