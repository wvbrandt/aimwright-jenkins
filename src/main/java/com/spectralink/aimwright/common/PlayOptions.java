package com.spectralink.aimwright.common;

import com.microsoft.playwright.Page;

public class PlayOptions {

    public static Page.WaitForLoadStateOptions getLoadTimeout(int seconds) {
        return new Page.WaitForLoadStateOptions().setTimeout(seconds);
    }
}
