package auto;
import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MobileDragAndDropTest {
    Playwright playwright;
    Browser browser;
    BrowserContext context;
    Page page;

    @BeforeEach
    void setup() {
        playwright = Playwright.create();

        // Ручная настройка параметров Samsung Galaxy S22 Ultra
        Browser.NewContextOptions deviceOptions = new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (Linux; Android 12; SM-S908B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.0.0 Mobile Safari/537.36")
                .setViewportSize(384, 873)  // Разрешение экрана
                .setDeviceScaleFactor(3.5)
                .setIsMobile(true)
                .setHasTouch(true);

        browser = playwright.chromium().launch());
        context = browser.newContext(deviceOptions);
        page = context.newPage();
    }

    @Test
    void testDragAndDropMobile() {
        page.navigate("https://the-internet.herokuapp.com/drag_and_drop");

        Locator columnA = page.locator("#column-a");
        Locator columnB = page.locator("#column-b");

        // Проверка начального состояния
        String initialTextA = columnA.textContent().trim();
        String initialTextB = columnB.textContent().trim();

        assertEquals("A", initialTextA, "Начальный текст в колонке A должен быть A");
        assertEquals("B", initialTextB, "Начальный текст в колонке B должен быть B");

        // Перетаскивание элемента A в зону B с использованием dragTo()
        columnA.dragTo(columnB);

        // Ожидание завершения анимации/обновления (небольшая пауза для надёжности)
        page.waitForTimeout(500);

        // Проверка результата после перетаскивания
        String finalTextA = columnA.textContent().trim();
        String finalTextB = columnB.textContent().trim();

        assertEquals("B", finalTextA, "После перетаскивания в колонке A должен быть текст B");
        assertEquals("A", finalTextB, "После перетаскивания в колонке B должен быть текст A");
    }

    @AfterEach
    void tearDown() {
        if (page != null) page.close();
        if (context != null) context.close();
        if (playwright != null) playwright.close();
    }
}

