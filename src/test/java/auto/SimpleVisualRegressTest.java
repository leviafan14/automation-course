package auto;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SimpleVisualRegressTest {
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeEach
    void setup(){
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        context = browser.newContext();
        page = context.newPage();
    }

    @Test
    void testHomePageVisual() throws IOException{
        page.navigate("https://the-internet.herokuapp.com");

        // Создание директории для артефактов
        Path artifactsDir = Paths.get("visual-regress");
        Files.createDirectories((artifactsDir));

        //Сохранение скриншота с актуальным состоянием браузера
        Path actualPath = artifactsDir.resolve("actual.png");
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(actualPath)
                .setFullPage(true)
        );
        // Путь к эталонному скриншоту
        Path expectedPath = Paths.get("expected.png");

        // Сравнение скриншотов через Files.mismatch()
        long mismatch = Files.mismatch(actualPath, expectedPath);

        // Проверка наличия эталонного скриншота
        if (mismatch == -1) {
            System.out.println("Скриншоты идентичны");
        }
        else {
            // При различиях сохраняем копию актуального скриншота как diff
            Path diffPath = artifactsDir.resolve("diff.png");
            Files.copy(actualPath, diffPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Скриншоты не идентичны. Обнаружена регрессия: " + diffPath);
            assertThat(mismatch).isEqualTo(-1);
        }
    }
}

