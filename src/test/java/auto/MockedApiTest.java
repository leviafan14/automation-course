package auto;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MockedApiTest {
    static Playwright playwright;
    static Browser browser;
    private BrowserContext context;
    private Page page;
    private String url = "https://the-internet.herokuapp.com/dynamic_content";
    // Мок-сервис для имитации API
    private static ApiService apiService;

    @BeforeAll
    static void setUpClass() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false));

        // Создаем мок ApiService
        apiService = mock(ApiService.class);

        // Настраиваем поведение мока - возвращаем тестовые данные
        when(apiService.fetchUserData()).thenReturn("MocData");
    }

    @BeforeEach
    void setUp() {
        context = browser.newContext();
        page = context.newPage();
    }

    @Test
    void testUserProfileWithMockedApi() {
        // Используем мок вместо реального API
        String mockData = apiService.fetchUserData();

        page.route("**/dynamic_content*with_content*", route -> {
            Request request = route.request();

            try {
                APIResponse response = context.request().get(request.url());
                String originalHtml = response.text();

                String modifiedHtml = originalHtml.replace("static", mockData);

                Map<String, String> headers = new HashMap<>();
                headers.put("content-type", "text/html; charset=utf-8");

                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setHeaders(headers)
                        .setBody(modifiedHtml)
                );
            } catch (Exception e) {
                System.err.println("Ошибка при обработке запроса: " + e.getMessage());
                route.resume();
            }
        });

        page.navigate(url+"?with_content=static");

        Locator elementWithMockData = page.getByRole(AriaRole.CODE);
        String mockTextData = elementWithMockData.innerText();

        assertNotNull(mockData);

        Assertions.assertEquals("?with_content="+mockData, mockTextData, "Mock текст не отображается");

    }


    // Тестовый класс-заглушка для API сервиса
    static class ApiService {
        public String fetchUserData() {
            // Имитация медленного API-запроса
            try {
                Thread.sleep(3000); // 3 секунды задержки
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "{\"name\": \"Real User\", \"email\": \"real@example.com\"}";
        }
    }

    @AfterEach
    void tearDown() {
        if (context != null) context.close();
    }

    @AfterAll
    static void tearDownClass() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}
