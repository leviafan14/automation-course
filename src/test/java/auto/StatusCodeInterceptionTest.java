package auto;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

public class StatusCodeInterceptionTest {
    Playwright playwright;
    Browser browser;
    BrowserContext context;
    Page page;

    @BeforeEach
    public void setup(){
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    public void tearDown() {
        if (page != null) page.close();
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @Test
     void testMockedStatusCode(){
        AtomicBoolean checkStatus = new AtomicBoolean(false);

        // Настройка перехвата ответа на запрос
        context.route("**/status_codes/404", route -> {
            route.fulfill(new Route.FulfillOptions()
                    .setStatus(200)
                    .setHeaders(Collections.singletonMap("Content-Type", "text/html"))
                    .setBody("<h3>Mocked Success Response</h3>")
            );
        });

        // Подписка на события ответа
        context.onResponse(response -> {
            String url = response.request().url();
            int status = response.status();

            if (url.contains("/status_codes/404")) {
                System.out.println("Response for: /status_codes/404: статус " + status);
                if (status == 200) {
                    checkStatus.set(true);
                }
            }
        });

        //Переход на страницу
        page.navigate("https://the-internet.herokuapp.com/status_codes");

        Locator link404 = page.locator("a[href='status_codes/404']");
        page.waitForNavigation(() -> {
           // Кликаем по ссылке "404"
            link404.click();
        });

        // Ожидание загрузки страницы
        page.waitForLoadState(LoadState.NETWORKIDLE);

        // Ожидание появления элемента с текстом
        page.waitForSelector("h3", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));

        // 7. Проверяем текст
        String actualText = page.textContent("h3");
        Assertions.assertEquals("Mocked Success Response", actualText,
                "Текст на странице не совпадает с мок-ответом!");

        // 8. Проверяем, что статус был 200
        Assertions.assertTrue(checkStatus.get(),
                "Статус ответа для /status_codes/404 не стал 200!");
    }
}
