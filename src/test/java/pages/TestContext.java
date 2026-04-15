package pages;

import com.microsoft.playwright.*;

public class TestContext {
    private Playwright playwright;
    private Browser browser;
    private Page page;

    public TestContext() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        page = browser.newPage();
    }

    public Page getPage() {
        return page;
    }

    public void close() {
        if (page != null) page.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}
