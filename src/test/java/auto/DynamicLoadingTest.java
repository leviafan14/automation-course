package auto;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;


public class DynamicLoadingTest {
    Playwright playwright;
    Browser browser;
    Page page;

    @Test
    void testDynamicLoading() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        BrowserContext context = browser.newContext();

        // Включаем трассировку
        context.tracing().start(new Tracing
                .StartOptions()
                .setScreenshots(true)
                .setSnapshots(true));

        page = context.newPage();
        page.navigate("https://the-internet.herokuapp.com/dynamic_loading/1");

        // Перехват сетевых запросов
        page.onResponse(response -> {
            String url = response.url();
            int status = response.status();
            if (url.contains("/dynamic_loading") && status == 200) {
                System.out.println("Успешный запрос к /dynamic_loading: " + url + ", статус: " + status);
            }
        });

        // Клик по кнопке запуска загрузки
        page.click("button");

        // Ожидание появления текста с таймаутом 10 секунд
        Locator finishText = page.locator("#finish:has-text('Hello World!')");
        finishText.waitFor(new Locator.WaitForOptions().setTimeout(10000));

        // Тест соответствия текста
        String textContent = finishText.innerText();
        Assertions.assertEquals("Hello World!", textContent, "Текст не совпадает с ожидаемым!");

        // Сохранение трассировки выполнения
        context.tracing().stop(new Tracing.StopOptions().setPath(Paths.get("trace/trace-success.zip")));
        System.out.println("Трассировка сохранена в trace/trace-success.zip");
    }

    @AfterEach
    void tearDown() {
        page.close();
        browser.close();
        playwright.close();
    }
}

