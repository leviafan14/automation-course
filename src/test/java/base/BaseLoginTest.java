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
        browser = playwright.chromium().launch();
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
    @AfterClass
    public void generateReport() throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Test Report</title></head><body>");
        html.append("<h1>Test Results</h1>");
        html.append("<table border='1'>");
        html.append("<tr><th>Test Name</th><th>Status</th><th>Duration (ms)</th><th>Screenshot</th><th>Error</th></tr>");

        for (TestResult result : results) {
            html.append("<tr>");
            html.append("<td>" + result.testName + "</td>");
            html.append("<td style='color:" + (result.status.equals("PASSED") ? "green" : "red") + "'>" + result.status + "</td>");
            html.append("<td>" + result.duration + "</td>");

            if (result.screenshotPath != null) {
                html.append("<td><img src='" + result.screenshotPath + "' width='300'></td>");
            } else {
                html.append("<td>-</td>");
            }

            html.append("<td>" + (result.errorMessage != null ? result.errorMessage : "-") + "</td>");
            html.append("</tr>");
        }

        html.append("</table></body></html>");

        // Сохраняем HTML-файл
        Path reportFile = REPORT_DIR.resolve("report.html");
        Files.writeString(reportFile, html.toString());
        System.out.println("Отчёт сгенерирован: " + reportFile);
    }
}
