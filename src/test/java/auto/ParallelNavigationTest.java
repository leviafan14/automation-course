package auto;
import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

@Execution(ExecutionMode.CONCURRENT)
public class ParallelNavigationTest {

    @ParameterizedTest(name = "Test page load in {0}: {1}")
    @MethodSource("browserAndPathProvider")
    void testPageLoad(String browserType, String path) {
        Playwright playwright = null;
        Browser browser = null;

        try {
            // Создаём отдельный Playwright и браузер для каждого теста
            playwright = Playwright.create();

            switch (browserType.toLowerCase()) {
                case "chromium":
                    browser = playwright.chromium().launch();
                    break;
                case "firefox":
                    browser = playwright.firefox().launch();
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported browser: " + browserType);
            }

            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            // Таймаут для навигации 30 сек
            page.setDefaultNavigationTimeout(30000);

            page.navigate("https://the-internet.herokuapp.com" + path);

            assertThat(page).hasTitle(Pattern.compile(".*"));

            context.close();

        } catch (TimeoutError e) {
            throw new RuntimeException("Timeout loading page " + path + " in " + browserType + ": " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Test failed for browser " + browserType + " and path " + path, e);
        } finally {
            // Закрытие ресурсов
            if (browser != null) browser.close();
            if (playwright != null) playwright.close();
        }
    }

    // Поставщик данных для параметризации тестов
    static Stream<Arguments> browserAndPathProvider() {
        String[] browsers = {"chromium", "firefox"};
        String[] paths = {"/", "/login", "/dropdown", "/javascript_alerts",
                "/checkboxes", "/hover", "/status_codes"};

        return Arrays.stream(browsers)
                .flatMap(browser -> Arrays.stream(paths)
                        .map(path -> Arguments.of(browser, path)));
    }
}


