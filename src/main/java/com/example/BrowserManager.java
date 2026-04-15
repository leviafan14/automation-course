package com.example;

import com.example.config.EnvironmentConfig;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.BrowserType;

public class BrowserManager {
    private final EnvironmentConfig config;

    public BrowserManager(EnvironmentConfig config) {
        this.config = config;
    }

    public Browser createBrowser() {
        return Playwright.create().chromium().launch();
    }
}

