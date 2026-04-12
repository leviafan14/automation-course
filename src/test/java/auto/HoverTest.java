package auto;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.*;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class HoverTest {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;

    @BeforeAll
    static void setupClass() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @BeforeEach
    void setUp() {
        context = browser.newContext();
        page = context.newPage();
    }

    @Test
    void testHoverProfiles() {
        page.navigate("https://the-internet.herokuapp.com/hovers");

        Locator figures = page.locator(".figure");
        int count = figures.count();

        for (int i = 0; i < count; i++) {
            Locator figure = figures.nth(i);

            // Наводим курсор на элемент
            figure.hover();

            // Проверяем, что появилась ссылка View profile
            Locator profileLink = figure.locator("text=View profile");
            profileLink.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            assertTrue(profileLink.isVisible(), "Ссылка View profile должна появиться после наведения курсора");

            // Кликаем на ссылку
            profileLink.click();

            // Ждём загрузки страницы и проверяем, что URL содержит id
            page.waitForURL(Pattern.compile(".*/users/\\d+$"));
            String currentUrl = page.url();
            assertTrue(currentUrl.matches(".*/users/\\d+$"),
                    "URL должен содержать /users/{id}, но был: " + currentUrl);

            // Возвращаемся назад для проверки следующего элемента
            page.goBack();
            page.waitForLoadState(LoadState.NETWORKIDLE);
        }
    }

    @AfterEach
    void tearDown() {
        if (page != null) {
            page.close();
        }
        if (context != null) {
            context.close();
        }
    }

    @AfterAll
    static void tearDownClass() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}
