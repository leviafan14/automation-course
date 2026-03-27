package auto;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

public class SimpleInterceptionTest {

    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;

    @BeforeAll
    static void setupAll() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @BeforeEach
    void setup() {
        context = browser.newContext();
        page = context.newPage();
    }

    @Test
    void simpleInterceptionTest() {
        // Переменная для хранения логов
        StringBuilder consoleLogs = new StringBuilder();

        // 1. Настраиваем перехват
        page.route("**/authenticate", route -> {
            System.out.println("Запрос перехвачен!");
            consoleLogs.append("Запрос перехвачен!").append("\n");

            // Получаем оригинальные данные запроса
            Request request = route.request();
            String originalBody = request.postData();
            System.out.println("Было: " + originalBody);
            consoleLogs.append("Было: ").append(originalBody).append("\n");

            // Парсим и изменяем username
            Map<String, String> params = parseFormData(originalBody);
            params.put("username", "HACKED_USER");

            // Формируем новую строку данных
            String modifiedBody = buildFormData(params);
            System.out.println("Стало: " + modifiedBody);
            consoleLogs.append("Стало: ").append(modifiedBody).append("\n");

            // Создаём ResumeOptions с новыми данными
            Route.ResumeOptions options = new Route.ResumeOptions()
                    .setMethod(request.method())
                    .setPostData(modifiedBody)
                    .setHeaders(request.headers());

            // Отправляем изменённый запрос
            route.resume(options);
        });

        // 2. Переходим на страницу
        page.navigate("https://the-internet.herokuapp.com/login");

        // 3. Заполняем форму
        page.fill("#username", "tomsmith");
        page.fill("#password", "SuperSecretPassword!");

        // 4. Нажимаем кнопку
        page.click("button[type='submit']");

        // 5. Ждём и проверяем результат
        String logs = consoleLogs.toString();
        System.out.println("Логи: " + logs);
        // Проверяем, что все ожидаемые сообщения появились в логах
        Assertions.assertTrue(logs.contains("Запрос перехвачен!"),
                "В логах отсутствует сообщение о перехвате запроса");

        Assertions.assertTrue(logs.contains("Было: username=tomsmith&password=SuperSecretPassword%21"),
                "В логах отсутствуют оригинальные данные запроса");

        Assertions.assertTrue(logs.contains("Стало: password=SuperSecretPassword%21&username=HACKED_USER"),
                "В логах отсутствуют изменённые данные запроса");    }

    // Вспомогательный метод для парсинга данных формы
    private Map<String, String> parseFormData(String body) {
        Map<String, String> result = new HashMap<>();
        if (body == null || body.isEmpty()) return result;

        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                result.put(keyValue[0], keyValue[1]);
            }
        }
        return result;
    }

    // Вспомогательный метод для сборки данных формы
    private String buildFormData(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) sb.append("&");
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
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
}
