package auto;

import com.example.config.EnvConfig;
import com.microsoft.playwright.*;
import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StatusCodeCombinedTest {
    private Playwright playwright;
    private APIRequestContext apiRequest;
    private Browser browser;
    private Page page;
    private static EnvConfig config;

    @BeforeAll
    static void loadConfig() {
        config = ConfigFactory.create(EnvConfig.class, System.getenv());
    }

    @BeforeEach
    void setup() {
        playwright = Playwright.create();

        // API контекст с базовым URL из конфига
        apiRequest = playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL(config.baseUrl())
        );

        // Настройка браузера
        browser = playwright.chromium().launch();

        page = browser.newPage();
        page.setDefaultTimeout(40000);
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 404})
    void testStatusCodeCombined(int statusCode) {
        System.out.println("Проверка статус-кода: " + statusCode);

        int apiStatus = getApiStatusCode(statusCode);
        System.out.println("API статус-код: " + apiStatus);

        int uiStatus = getUiStatusCode(statusCode);
        System.out.println("UI статус-код: " + uiStatus);

        // Сравнение результатов
        assertEquals(apiStatus, uiStatus,
                "Статус-коды через API и UI не совпадают для /status_codes/" + statusCode);

        page.goBack();
    }

    private int getApiStatusCode(int code) {
        APIResponse response = apiRequest.get("/status_codes/" + code);
        assertEquals(code, response.status(),
                "API: Неверный статус код для " + code + ". Ожидается: " + code + ", фактически: " + response.status());
        return response.status();
    }

    private int getUiStatusCode(int code) {
        try {
            // Переход на страницу
            page.navigate(config.baseUrl() + "/status_codes");
            page.waitForSelector("div.example");

            // Локатор ссылки с нужным кодом
            Locator link = page.locator(
                    String.format("a[href*='status_codes/%d']", code)
            ).first();

            // Перехват ответа перед кликом
            Response response = page.waitForResponse(
                    res -> res.url().endsWith("/status_codes/" + code),
                    () -> link.click(new Locator.ClickOptions().setTimeout(10000))
            );

            return response.status();

        } catch (Exception e) {
            // Делаем скриншот при ошибке
            String screenshotPath = String.format("target/screenshots/error-%d.png", code);
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get(screenshotPath))
                    .setFullPage(true)
            );
            throw new RuntimeException("UI test FALL " + code, e);
        }
    }

    @AfterEach
    void teardown() {
        if (page != null) {
            page.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (apiRequest != null) {
            apiRequest.dispose();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}

