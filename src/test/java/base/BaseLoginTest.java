package base;

import auto.TestResult;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.ScreenshotType;
import org.testng.annotations.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BaseLoginTest {
    protected static Playwright playwright;
    protected Browser browser;
    protected List<TestResult> results = new ArrayList<>();

    private static final Path REPORT_DIR = Paths.get("test-reports");
    private static final Path SCREENSHOTS_DIR = REPORT_DIR.resolve("screenshots");

    @BeforeClass
    public static void setupPlaywright() {
        playwright = Playwright.create();
    }

    @AfterClass
    public static void closePlaywright() {
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeMethod
    public void setup() {
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        try {
            Files.createDirectories(REPORT_DIR);
            Files.createDirectories(SCREENSHOTS_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать директории для отчётов", e);
        }
    }

    protected String saveScreenshot(Page page, String testName) throws IOException {
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String fileName = testName + "_" + timestamp + ".png";
        Path screenshotPath = SCREENSHOTS_DIR.resolve(fileName);

        page.screenshot(new Page.ScreenshotOptions()
                .setPath(screenshotPath)
                .setType(ScreenshotType.PNG));

        System.out.println("Скриншот сохранён: " + screenshotPath);
        return screenshotPath.toString();

    }
}
