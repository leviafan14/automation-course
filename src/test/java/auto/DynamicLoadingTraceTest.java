package auto;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DynamicLoadingTraceTest {
    Playwright playwright;
    Browser browser;
    BrowserContext context;
    Page page;

    @BeforeEach
    void setup(){
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        context = browser.newContext();
    }

    @Test
    void testDynamicLoadingTrace(){
        // Настройка трассировки
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));
        page = context.newPage();
        // Шаги теста
        page.navigate("https://the-internet.herokuapp.com/dynamic_loading/1");
        page.click("button");
        // Получение элемента по селектору
        Locator finishBlock = page.locator("#finish");

        // Ожидание появления элемента
        finishBlock.waitFor();
        String finishBlockText = finishBlock.innerText();

        assertEquals("Hello World!", finishBlockText);

        // Остановка трассировки
        context.tracing().stop(new Tracing.StopOptions().setPath(Paths.get("trace-dynamic-loading.zip")));

    }

    @AfterEach
    void teardown(){
        if (page != null) {
            page.close();
        }
        if (context != null) {
            context.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}
