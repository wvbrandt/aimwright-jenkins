package com.spectralink.aimwright.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.spectralink.aimwright.common.PlayOptions;

public class LoginPage {
    private final Page page;

    // Locators
    private final String usernameInput = "#username";
    private final String passwordInput = "#password";
    private final String signInButton = "[type=submit]";

    // Constructor
    public LoginPage(Page page) {
        this.page = page;
    }

    public void loginWithCredentials(String email, String password) {
        page.waitForSelector(usernameInput);
        page.fill(usernameInput, email);
        page.fill(passwordInput, password);
        // Use JavaScript click for more reliable form submission
        page.evaluate("document.querySelector('[type=submit]').click()");
        // Wait for navigation to complete
        page.waitForLoadState(LoadState.NETWORKIDLE);
        // Additional wait to ensure page transition completes
        page.waitForTimeout(2000);
    }
}
