package auto;
import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatusCodeApiUiTest {
    private Playwright playwright;
    private APIRequestContext apiRequest;
    private Browser browser;
    private Page page;

    @BeforeEach
    void setup() {
        playwright = Playwright.create();

        // Настройка API контекста
        apiRequest = playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL("https://the-internet.herokuapp.com")
        );

        browser = playwright.chromium().launch();

        page = browser.newPage();
        // Переход на страницу статус кодов один раз
        page.navigate("https://the-internet.herokuapp.com/status_codes");
        page.waitForSelector("div.example");
    }

    @Test
    void testStatusCodesCombined() {
        int[] statusCodes = {200, 404};

        for (int code : statusCodes) {
            System.out.println("Проверка кода ответа: " + code);

            // Статус через API
            int apiStatus = getApiStatusCode(code);
            System.out.println("API status: " + apiStatus);

            // Статус через UI
            int uiStatus = getUiStatusCode(code);
            System.out.println("UI status: " + uiStatus);

            // Сравниваем результаты
            assertEquals(apiStatus, uiStatus,
                    "Статус-коды полученные через API и UI не совпадают для url: /status_codes/" + code);

            page.goBack();
        }
    }

    private int getApiStatusCode(int code) {
        try {
            APIResponse response = apiRequest.get("/status_codes/" + code);
            return response.status();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при выполнении API-запроса для кода " + code, e);
        }
    }

    private int getUiStatusCode(int code) {
        try {
            Locator link = page.locator("text=" + code).first();

            Response response = page.waitForResponse(
                    res -> res.url().endsWith("/status_codes/" + code),
                    () -> link.click(new Locator.ClickOptions().setTimeout(15000))
            );

            return response.status();

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при выполнении UI для кода " + code, e);
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

