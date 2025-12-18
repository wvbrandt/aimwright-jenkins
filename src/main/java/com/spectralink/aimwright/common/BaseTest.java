package com.spectralink.aimwright.common;

import ch.qos.logback.classic.Logger;
import com.microsoft.playwright.*;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.LoadState;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Base test class for UI tests using Playwright.
 *
 * Provides:
 * - Browser lifecycle management (per-suite)
 * - Context isolation per test (parallel-safe)
 * - Tracing on failure for debugging
 * - Screenshot capture on failure
 * - Test logging with clear demarcation
 */
public class BaseTest {
    protected Playwright playwright;
    protected BrowserType browserType;
    public static Browser browser;
    public static BrowserContext context;
    public static Page page;

    protected final Logger log = (Logger) LoggerFactory.getLogger(this.getClass());

    // UI instance URL - loaded from configuration
    protected static String url;

    // Logging formatting
    public static String scenarioSeparator = "=";
    public static String sectionSeparator = "-";
    public static int separatorLength = 60;

    @BeforeSuite(alwaysRun = true)
    public void initializeFramework() {
        // Load configuration from settings.properties
        Settings.loadAll();
        Settings.initializeTestDirectories();

        // Get URL from configuration system
        url = Settings.getUiInstance();

        // Create Playwright instance
        playwright = Playwright.create();

        // Select browser type from configuration
        String browserName = Settings.getBrowserApplication();
        switch (browserName.toLowerCase()) {
            case "chromium":
            case "chrome":
                browserType = playwright.chromium();
                break;
            case "firefox":
                browserType = playwright.firefox();
                break;
            case "webkit":
            case "safari":
                browserType = playwright.webkit();
                break;
            default:
                log.warn("Unknown browser type '{}', defaulting to chromium", browserName);
                browserType = playwright.chromium();
        }

        // Launch browser with configuration options
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(Settings.getBrowserHeadless());

        browser = browserType.launch(launchOptions);
        log.debug("Started {} browser version: {}", browserName, browser.version());

        // Set default assertion timeout
        PlaywrightAssertions.setDefaultAssertionTimeout(Settings.getBrowserDefaultTimeout());

        log.info("Aimwright Playwright framework initialized");
    }

    @BeforeMethod(alwaysRun = true)
    public void testBeginDemarcation(Method method) {
        log.info(StringUtils.repeat(scenarioSeparator, separatorLength));
        log.info(" > Starting test : {}", method.getName());
        log.info(StringUtils.repeat(sectionSeparator, separatorLength));

        // Create isolated context for this test (parallel-safe)
        context = browser.newContext();

        // Start tracing for debugging (captures screenshots, snapshots, sources)
        if (Settings.getBrowserTraceOnFailure()) {
            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(true));
        }

        // Create new page and navigate to application
        page = context.newPage();
        page.setDefaultTimeout(Settings.getBrowserDefaultTimeout());
        page.navigate(url);
        page.waitForLoadState(LoadState.LOAD);
    }

    @AfterMethod(alwaysRun = true)
    public void testEndDemarcation(Method method, ITestResult result) {
        String resultLiteral = getResultStatus(result);

        // Handle failure artifacts
        if (result.getStatus() == ITestResult.FAILURE) {
            saveFailureArtifacts(method.getName());
        }

        // Stop tracing
        if (Settings.getBrowserTraceOnFailure()) {
            if (result.getStatus() == ITestResult.SUCCESS) {
                context.tracing().stop();
            } else {
                // Save trace on failure
                File traceDir = Settings.getTraceDirectory();
                if (traceDir != null) {
                    Path tracePath = Paths.get(traceDir.getPath(), method.getName() + ".zip");
                    context.tracing().stop(new Tracing.StopOptions().setPath(tracePath));
                    log.info(" > Saved trace to: {}", tracePath);
                }
            }
        }

        // Close context (automatically closes page)
        if (context != null) {
            context.close();
        }

        log.info(StringUtils.repeat(sectionSeparator, separatorLength));
        log.info(" > Ending test : {} : {}", method.getName(), resultLiteral);
        log.info(StringUtils.repeat(scenarioSeparator, separatorLength));
    }

    @AfterSuite(alwaysRun = true)
    public void shutdownFramework() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
        log.info("Aimwright Playwright framework shutdown complete");
    }

    /**
     * Saves failure artifacts (screenshot and trace).
     */
    private void saveFailureArtifacts(String testName) {
        // Save screenshot on failure
        if (Settings.getBrowserFailureScreenshot() && page != null) {
            try {
                File screenshotDir = Settings.getScreenshotDirectory();
                if (screenshotDir != null) {
                    Path screenshotPath = Paths.get(screenshotDir.getPath(), testName + ".png");
                    page.screenshot(new Page.ScreenshotOptions()
                            .setPath(screenshotPath)
                            .setFullPage(true));
                    log.info(" > Saved screenshot to: {}", screenshotPath);
                }
            } catch (Exception e) {
                log.error("Failed to save screenshot: {}", e.getMessage());
            }
        }
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
