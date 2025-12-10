package com.spectralink.aimwright.tests.auth;

import com.spectralink.aimwright.common.BaseTest;
import com.spectralink.aimwright.common.Defaults;
import com.spectralink.aimwright.pages.LoginPage;
import org.testng.annotations.Test;

/**
 * Diagnostic test to debug login issues.
 */
public class LoginDiagnosticTest extends BaseTest {

    @Test(description = "Diagnostic: Check login page elements and attempt login")
    public void diagnosticLogin() {
        // Log the credentials being used (masked)
        String username = Defaults.getUserAdmin();
        String password = Defaults.getUserAdminPassword();
        log.info("Testing login with username: {}", username);
        log.info("Password length: {}", password.length());

        // Check initial page state
        log.info("Initial URL: {}", page.url());
        log.info("Initial Title: {}", page.title());

        // Check if login form exists
        boolean usernameExists = page.locator("#username").count() > 0;
        boolean passwordExists = page.locator("#password").count() > 0;
        boolean submitExists = page.locator("[type=submit]").count() > 0;

        log.info("Username field exists: {}", usernameExists);
        log.info("Password field exists: {}", passwordExists);
        log.info("Submit button exists: {}", submitExists);

        // Fill credentials
        page.fill("#username", username);
        page.fill("#password", password);

        log.info("Credentials filled, clicking Sign In...");

        // Take screenshot before click
        page.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
            .setPath(java.nio.file.Paths.get("target/failure-screenshots/diagnostic_before_click.png")));

        // Try using JavaScript click as fallback
        page.evaluate("document.querySelector('[type=submit]').click()");

        log.info("Clicked via JavaScript, waiting 5 seconds...");

        // Wait longer
        page.waitForTimeout(5000);

        log.info("Post-click URL: {}", page.url());
        log.info("Post-click Title: {}", page.title());

        // Take screenshot after wait
        page.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
            .setPath(java.nio.file.Paths.get("target/failure-screenshots/diagnostic_after_wait.png")));

        // Check for error messages - try multiple selectors
        String[] errorSelectors = {
            ".ant-alert-error",
            ".error",
            ".alert-danger",
            "[class*='error']",
            ".ant-form-item-explain-error",
            ".ant-message-error"
        };

        for (String selector : errorSelectors) {
            int count = page.locator(selector).count();
            if (count > 0) {
                String text = page.locator(selector).first().textContent();
                log.error("Found error with selector '{}': {}", selector, text);
            }
        }

        // Check if avatar is visible (login success indicator)
        boolean avatarVisible = page.locator("span.ant-avatar").count() > 0;
        log.info("Avatar visible (logged in): {}", avatarVisible);

        // Check if URL changed
        if (!page.url().contains("/login")) {
            log.info("SUCCESS: URL changed from login page to: {}", page.url());
        } else {
            log.warn("ISSUE: Still on login page after 5 second wait");
        }

        // Check all visible text for error indicators
        String pageText = page.locator("body").textContent().toLowerCase();
        if (pageText.contains("incorrect") || pageText.contains("invalid") ||
            pageText.contains("wrong") || pageText.contains("failed")) {
            log.error("Page contains error keywords - login likely failed");
        }

        // Final screenshot
        page.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
            .setPath(java.nio.file.Paths.get("target/failure-screenshots/diagnostic_final.png")));
        log.info("Final screenshot saved");
    }
}
