package auto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class TodoApiTest {
    Playwright playwright;
    APIRequestContext requestContext;
    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        playwright = Playwright.create();
        requestContext = playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL("https://jsonplaceholder.typicode.com")
        );
    }

    @Test
    void testTodoApi() throws Exception {
        // 1. Выполнение запроса
        var response = requestContext.get("/posts");

        // 2. Проверка статуса ответа
        assertEquals(200, response.status(), "Статус ответа должен быть 200 OK");

        // 3. Парсинг JSON
        String responseBody = response.text();
        JsonNode jsonResponse = objectMapper.readTree(responseBody);

        // Проверка, что ответ — JSON‑массив
        assertTrue(jsonResponse.isArray(), "Ответ должен иметь формат JSON‑массивом");

        // Проверяем, что массив не пустой
        assertFalse(jsonResponse.isEmpty(), "Массив пуст");

        // Берём первый элемент массива для проверки структуры
        JsonNode firstItem = jsonResponse.get(0);

        // 4. Проверка структуры JSON
        assertNotNull(firstItem.get("id"), "Поле id отсутствует");
        assertNotNull(firstItem.get("userId"), "Поле userId отсутствует");
        assertNotNull(firstItem.get("title"), "Поле title отсутствует");
        assertNotNull(firstItem.get("body"), "Поле body отсутствует");

        // Дополнительно: проверяем типы данных
        assertTrue(firstItem.get("id").isNumber(), "Поле id не число");
        assertTrue(firstItem.get("userId").isNumber(), "Поле userId не число");
        assertTrue(firstItem.get("title").isTextual(), "Поле title не строка");
        assertTrue(firstItem.get("body").isTextual(), "Поле body не строка");
    }

    @AfterEach
    void tearDown() {
        requestContext.dispose();
        playwright.close();
    }
}

