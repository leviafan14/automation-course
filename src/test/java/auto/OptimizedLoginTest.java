package auto;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import org.junit.jupiter.api.*;

import java.util.List;

public class OptimizedLoginTest {
    static private Playwright playwright;
    static private Browser browser;
    private BrowserContext context;
    private Page page;
    static private List<Cookie> authCookies;

    @BeforeAll
    static void setUpClass() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();

        // Выполняем логин один раз и сохраняем cookies
        try (BrowserContext tempContext = browser.newContext()) {
            Page tempPage = tempContext.newPage();
            authCookies = performLogin(tempPage);
            tempPage.close();
        }
    }

    @BeforeEach
    void setUp() {
        // Создаём новый контекст и добавляем сохранённые cookies для каждого теста
        context = browser.newContext();
        if (authCookies != null && !authCookies.isEmpty()) {
            context.addCookies(authCookies);
        }
        page = context.newPage();
    }

    @Test
    void testSecureArea() {
        page.navigate("https://the-internet.herokuapp.com/secure");
        // Проверяем, что пользователь аутентифицирован
        Assertions.assertTrue(page.locator("h2").textContent().contains("Secure Area"));
    }

    private static List<Cookie> performLogin(Page page) {
        // Переходим на страницу логина
        page.navigate("https://the-internet.herokuapp.com/login");

        // Заполняем форму логина
        page.fill("#username", "tomsmith");
        page.fill("#password", "SuperSecretPassword!");

        // Нажимаем кнопку логина
        page.click("button[type='submit']");

        // Ждём загрузки защищённой области
        page.waitForSelector("h2");

        // Сохраняем cookies после успешной аутентификации
        return page.context().cookies();
    }

    @AfterEach
    void tearDown() {
        if (page != null) page.close();
        if (context != null) context.close();
    }

    @AfterAll
    static void tearDownClass() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}

