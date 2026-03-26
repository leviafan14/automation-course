package courseplayw;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

public class DynamicContentTest {

    Playwright playwright;
    Browser browser;
    BrowserContext context;
    Page page;

    @BeforeEach
    void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium()
                .launch(new BrowserType.LaunchOptions().setHeadless(false));
        context = browser.newContext();
        page = context.newPage();
    }


    @Test
    void testDynamicLoading() {
        try {
            page.navigate("https://the-internet.herokuapp.com/dynamic_loading/1");

            Locator startButton = page.locator("button:has-text('Start')");
            startButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            startButton.click();

            Locator helloWorldText = page.locator("#finish >> text=Hello World!");
            helloWorldText.waitFor(new Locator.WaitForOptions().setTimeout(45000));

            Locator seleniumLink = page.locator("text=Elemental Selenium");
            seleniumLink.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            Assertions.assertTrue(seleniumLink.isVisible(), "Ссылка не отображается");

            Page newPage = context.waitForPage(() -> {
                seleniumLink.click();
            });

            newPage.waitForLoadState(LoadState.LOAD);

            Assertions.assertTrue(
                    newPage.url().startsWith("https://elementalselenium.com/"), "Фактический URL: " + newPage.url()
            );
            Assertions.assertTrue(newPage.isVisible("h1 >> text='Elemental Selenium'"), "Заголовок не найден"
            );

            newPage.close();

        } catch (Exception e) {

            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get("dynamic-loading-error.png")));
            throw e;
        }
    }

    @AfterEach
    void tearDown() {
        playwright.close();
    }
}