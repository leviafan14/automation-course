package auto;

import com.github.javafaker.Faker;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.Map;


public class FakerGenerTest {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            // Генерация случайного имени пользователя с помощью Faker
            Faker faker = new Faker();
            String generatedUserName = faker.name().fullName();
            System.out.println("Сгенерированное имя пользователя: " + generatedUserName);

            page.route("**/dynamic_content*with_content*", route -> {
                Request request = route.request();

                try {
                    APIResponse response = context.request().get(request.url());
                    String originalHtml = response.text();

                    String modifiedHtml = originalHtml.replace("static", generatedUserName);

                    Map<String, String> headers = new HashMap<>();
                    headers.put("content-type", "text/html; charset=utf-8");

                    route.fulfill(new Route.FulfillOptions()
                            .setStatus(200)
                            .setHeaders(headers)
                            .setBody(modifiedHtml)
                    );
                } catch (Exception e) {
                    // В случае ошибки — пропускаем оригинальный запрос
                    System.err.println("Ошибка при обработке запроса: " + e.getMessage());
                    route.resume();
                }
            });

            page.navigate("https://the-internet.herokuapp.com/dynamic_content?with_content=static");

            Locator elementWithGenFakeName = page.getByRole(AriaRole.CODE);
            String genFakeName = elementWithGenFakeName.innerText();

            Assertions.assertEquals("?with_content="+generatedUserName, genFakeName, "Текст не отображается");

            page.waitForTimeout(3000);
        } catch (Exception e) {
            System.err.println("Произошла ошибка во время выполнения теста:");
            e.printStackTrace();
        }
    }
}





