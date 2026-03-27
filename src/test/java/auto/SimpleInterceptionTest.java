package auto;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.*;

import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleInterceptionTest {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;

    @BeforeAll
    static void setupAll(){
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @BeforeEach
    void setup(){
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterAll
    static void tearDownAll() {
        browser.close();
        playwright.close();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void simpleInterceptionTest() {
        AtomicBoolean requestIntercepted = new AtomicBoolean(false);
        AtomicBoolean dataModified = new AtomicBoolean(false);

        page.route("**/authenticate", route -> {
            System.out.println("Запрос перехвачен");
        });
        // 1. Настраиваем перехват
        page.route("**/authenticate", route -> {
            System.out.println("Запрос перехвачен!");

            // Получаем оригинальные данные запроса
            Request originalRequest = route.request();
            String postData = originalRequest.postData();

            // Выводим оригинальные данные в консоль
            System.out.println("Было: " + postData);

            // Замена username в данных запроса
            String modifiedPostData = postData.replace(
                    "\"username\":\"tomsmith\"",
                    "\"username\":\"HACKED_USER\""
            );
            // Вывод изменённых данных в консоль
            System.out.println("Стало: " + modifiedPostData);

            // Создаем ResumeOptions с новыми данными
            Route.ResumeOptions options = new Route.ResumeOptions();
            options.setPostData(modifiedPostData);

            // Отправляем измененный запрос
            route.resume(options);
        });

        // 2. Переходим на страницу
        page.navigate("https://the-internet.herokuapp.com/login");

        // 3. Заполняем форму
        page.waitForSelector("#username", new Page.WaitForSelectorOptions()
                .setTimeout(5000)
                .setState(WaitForSelectorState.VISIBLE));
        page.waitForSelector("#username", new Page.WaitForSelectorOptions()
                .setTimeout(5000)
                .setState(WaitForSelectorState.VISIBLE));
        page.waitForSelector("button[type='submit']", new Page.WaitForSelectorOptions()
                .setTimeout(5000)
                .setState(WaitForSelectorState.VISIBLE)
        );
        page.locator("#username").fill("tomsmith");
        page.locator("#password").fill("password");

        // 4. Нажимаем кнопку
        page.click("button[type='submit']");
        // Ожидание завершения сетевых запросов
        page.waitForLoadState(LoadState.NETWORKIDLE);

        Response response = page.waitForResponse(resp ->
                        resp.url().contains("/authenticate") && resp.request().method().equals("POST"),
                (Runnable) new Page.WaitForResponseOptions().setTimeout(5000)
        );

        String actualSentData = response.request().postData();
        Assertions.assertNotNull(actualSentData, "Тело запроса пустое");
        System.out.println("Отправленные данные: " + actualSentData);
        Assertions.assertTrue(
                actualSentData.contains("\"username\":\"HACKED_USER\""),
                "В отправленных данных не найден HACKED_USER. Фактически: " + actualSentData
        );

        // 5. Ждем и проверяем результат
        Assertions.assertTrue(requestIntercepted.get(),
                "Запрос не был перехвачен — сообщение 'Запрос перехвачен!' не появилось в логах");
        Assertions.assertTrue(dataModified.get(),
                "Данные запроса не были изменены — проверьте логику замены username");
    }
}

