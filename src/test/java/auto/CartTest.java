package auto;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CartTest {
    private BrowserContext context;
    private Page page;
    private static Browser browser;
    private static Playwright playwright;
    private static final String VIDEOS_DIR = "videos/";
    private Path timestampDir; // Директория для артефактов с датой

    // Флаг для отслеживания падения теста
    private boolean testFailed = false;

    @BeforeEach
    void setup() throws IOException {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();

        // Создание директории с текущей датой/временем
        timestampDir = Paths.get(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")));
        Path videoPath = timestampDir.resolve(VIDEOS_DIR);
        Files.createDirectories(videoPath);

        // Видео сохраняется в ту же директорию
        context = browser.newContext(new Browser.NewContextOptions()
                .setRecordVideoDir(videoPath));
        page = context.newPage();
    }

    @AfterEach
    void teardown() {
        if (testFailed) {
            try {
                // Сохраняем скриншот на диск в директорию артефактов
                Path screenshotPath = getArtifactPath("failure_screenshot.png");
                page.screenshot(new Page.ScreenshotOptions().setPath(screenshotPath));

                // Прикрепляем к Allure
                byte[] screenshotBytes = Files.readAllBytes(screenshotPath);
                Allure.addAttachment(
                        "Screenshot on Failure",
                        "image/png",
                        new ByteArrayInputStream(screenshotBytes),
                        ".png"
                );
                System.out.println("Скриншот сохранён: " + screenshotPath.toAbsolutePath());
            } catch (Exception e) {
                System.err.println("Ошибка при сохранении скриншота: " + e.getMessage());
            }
        }

        // Закрытие ресурсов
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

    @Test
    void testCartActions() {
        try {
            page.navigate("https://the-internet.herokuapp.com/add_remove_elements/");
            int cartCountStart = 0;
            int cartCurrentCount = 0;

            // Флаг проверки добавления товара в корзину
            boolean isAdded = false;
            // Флаг проверки удаления товара из корзины
            boolean isDeleted = false;

            // Добавление товара в корзину
            page.getByText("Add Element").click();

            // Скриншот при добавлении в корзину
            page.locator("#content").screenshot(new Locator.ScreenshotOptions()
                    .setPath(getArtifactPath("cart_after_add.png")));

            Locator deleteButtons = page.getByText("Delete");
            // Ожидание появления хотя бы одной кнопки Delete
            deleteButtons.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

            // Получение количества кнопок Delete на экране
            cartCurrentCount = deleteButtons.count();

            // Проверка что кнопок Delete на экране больше чем при старте
            if (cartCurrentCount > cartCountStart) {
                isAdded = true;
                cartCountStart = cartCurrentCount;
            } else {
                isAdded = false;
                testFailed = true;
            }
            // Проверка, что товар добавлен в корзину
            assertTrue(isAdded, "Товар не добавлен в корзину");

            // Удаление товара из корзины
            deleteButtons.first().click();
            // Ожидание стабилизации страницы
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            // Скриншот страницы
            page.locator("#content").screenshot(new Locator.ScreenshotOptions()
                    .setPath(getArtifactPath("cart_after_remove.png")));

            // Получение количества кнопок Delete на экране после нажатия на одну из них
            cartCurrentCount = deleteButtons.count();

            if (cartCurrentCount < cartCountStart) {
                isDeleted = true;
            } else {
                isDeleted = false;
                testFailed = true;
            }
            assertTrue(isDeleted, "Товар не удалён из корзины");
        } catch (Throwable t) {
            // Устанавливаем флаг падения теста и перебрасываем исключение
            testFailed = true;
            System.out.println("Тест упал");
            throw t;
        }
    }



    private Path getArtifactPath(String filename) {
        return timestampDir.resolve(filename);
    }

}
