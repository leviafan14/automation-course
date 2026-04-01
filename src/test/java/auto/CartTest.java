package auto;

import com.microsoft.playwright.*;

import com.microsoft.playwright.options.ScreenshotAnimations;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CartTest {
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeEach
    void setup(){
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        context = browser.newContext();
        page = context.newPage();
    }

    @Test
    void testHomePageVisual() throws IOException {
        // 1. Переход на страницу и ожидание загрузки
        page.navigate("https://the-internet.herokuapp.com");
        page.waitForTimeout(1000); // Дополнительная задержка для стабильности

        // 2. Создаём директорию для артефактов (исправлено название)
        Path artifactsDir = Paths.get("visual-regression");
        Files.createDirectories(artifactsDir);

        // 3. Устанавливаем фиксированный размер окна для стабильности рендеринга
        page.setViewportSize(1280, 720);

        // 4. Сохраняем актуальный скриншот
        Path actualPath = artifactsDir.resolve("actual.png");
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(actualPath)
                .setFullPage(true)
                .setAnimations(ScreenshotAnimations.DISABLED)); // Отключаем анимации

        // 5. Путь к эталонному скриншоту (в той же директории)
        Path expectedPath = artifactsDir.resolve("expected.png");

        // 6. Проверяем существование эталонного скриншота
        if (!Files.exists(expectedPath)) {
            System.out.println("Эталонный скриншот не найден: " + expectedPath);
            System.out.println("Создайте эталон, сохранив скриншот как 'visual-regression/expected.png'");
            // Создаём эталон из актуального скриншота при первом запуске
            Files.copy(actualPath, expectedPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Создан эталонный скриншот: " + expectedPath);
            return; // Завершаем тест — это первый запуск
        }

        // 7. Сравниваем файлы через Files.mismatch()
        long mismatch;
        try {
            mismatch = Files.mismatch(actualPath, expectedPath);
        } catch (IOException e) {
            throw new IOException("Ошибка при сравнении скриншотов: " + e.getMessage(), e);
        }

        if (mismatch == -1) {
            System.out.println("Скриншоты идентичны. Визуальная регрессия не обнаружена.");
        } else {
            // При различиях сохраняем копию актуального скриншота как diff
            Path diffPath = artifactsDir.resolve("diff.png");
            try {
                Files.copy(actualPath, diffPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("⚠Скриншоты не идентичны. Обнаружена регрессия: " + diffPath);
                System.out.println("Позиция первого различия: " + mismatch);
            } catch (IOException e) {
                throw new IOException("Ошибка при создании diff-файла: " + e.getMessage(), e);
            }

            assertThat(mismatch)
                    .as("Обнаружены различия между скриншотами. Diff-файл: " + diffPath)
                    .isEqualTo(-1);
        }
    }

}
