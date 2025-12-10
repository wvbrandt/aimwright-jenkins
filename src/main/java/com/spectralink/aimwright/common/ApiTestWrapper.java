package com.spectralink.aimwright.common;

import ch.qos.logback.classic.Logger;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.lang.reflect.Method;

/**
 * Base test class for API-only tests.
 *
 * Unlike BaseTest, this class does NOT initialize a browser.
 * Use this for tests that only make API calls via AmieApiClient.
 *
 * Provides:
 * - Configuration loading
 * - Session credential setup
 * - Test logging with clear demarcation
 */
public class ApiTestWrapper {
    protected final Logger log = (Logger) LoggerFactory.getLogger(this.getClass());

    // Logging formatting
    public static String scenarioSeparator = "=";
    public static String sectionSeparator = "-";
    public static int separatorLength = 60;

    @BeforeSuite(alwaysRun = true)
    public void loadConfig() {
        Settings.loadAll();
        log.info("Aimwright API test framework initialized");
    }

    @BeforeClass(alwaysRun = true)
    public void beforeScenarioProcedure() {
        // Override in subclass if needed
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeTestProcedure(Method method) {
        log.info(StringUtils.repeat(scenarioSeparator, separatorLength));
        log.info(" > Starting API test : {}", method.getName());
        log.info(StringUtils.repeat(sectionSeparator, separatorLength));

        // Set default credentials if not already set
        if (!Session.isCredentialsSet()) {
            Session.setCredentials();
        }
    }

    @AfterMethod(alwaysRun = true)
    public void afterTestProcedure(Method method, ITestResult result) {
        log.info(StringUtils.repeat(sectionSeparator, separatorLength));
        log.info(" > Ending API test : {} : {}", method.getName(), getResultStatus(result));
        log.info(StringUtils.repeat(scenarioSeparator, separatorLength));
    }

    @AfterClass(alwaysRun = true)
    public void afterScenarioProcedure() {
        // Override in subclass if needed
    }

    /**
     * Converts TestNG result status to string.
     */
    protected String getResultStatus(ITestResult result) {
        switch (result.getStatus()) {
            case ITestResult.SUCCESS:
                return "PASS";
            case ITestResult.FAILURE:
                return "FAIL";
            case ITestResult.SKIP:
                return "SKIP";
            default:
                return "UNKNOWN";
        }
    }
}
