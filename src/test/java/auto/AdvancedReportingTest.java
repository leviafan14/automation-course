package auto;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
//import org.allureframework.allure.*;
import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.*;
import java.nio.file.*;
import java.io.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@Epic("Тесты для the-internet.herokuapp.com")
@Feature("Работа с JavaScript-алертами")
public class AdvancedReportingTest  {
    private static ExtentReports extent;
    private Playwright playwright;
    private Browser browser;
    private Page page;
    private ExtentTest test;


    @BeforeAll
    static void setupExtent() {
        ExtentSparkReporter reporter = new ExtentSparkReporter("target/extent-reports/extent-report.html");
        reporter.config().setDocumentTitle("Playwright Extent Report");
        extent = new ExtentReports();
        extent.attachReporter(reporter);
    }

    @BeforeEach
    void setUp(TestInfo testInfo) {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        page = browser.newPage();
        test = extent.createTest(testInfo.getDisplayName());
        logExtent(Status.INFO, "Тест начат: " + testInfo.getDisplayName());
    }

    @Test
    @Story("Проверка JS Alert")
    @Description("Тест взаимодействия с JS Alert и проверка результата")
    @Severity(SeverityLevel.NORMAL)
    void testJavaScriptAlert() throws ExecutionException, InterruptedException, TimeoutException {
        try {
            navigateToAlertsPage();
            String alertMessage = handleJsAlert();
            verifyResultText();
            captureSuccessScreenshot();
            logExtent(Status.PASS, "Тест успешно завершён с сообщением: " + alertMessage);
        } catch (Exception e) {
            handleTestFailure(e);
            throw e;
        }
    }

    @Step("Открыть страницу с алертами")
    private void navigateToAlertsPage() {
        page.navigate("https://the-internet.herokuapp.com/javascript_alerts",
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        String actualText = page.locator("h3").textContent();
        Assertions.assertEquals("JavaScript Alerts", actualText, "Текст заголовка не совпадает");

        logExtent(Status.INFO, "Страница с алертами загружена");
    }

    @Step("Обработать JS Alert (сообщение: {0})")
    private String handleJsAlert() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<String> alertMessageFuture = new CompletableFuture<>();
        page.onDialog(dialog -> {
            String message = dialog.message();
            alertMessageFuture.complete(message);
            dialog.accept();
        });
        page.click("button[onclick='jsAlert()']");
        logExtent(Status.INFO, "Клик по кнопке JS Alert выполнен");
        String alertMessage = alertMessageFuture.get(5000, java.util.concurrent.TimeUnit.MILLISECONDS);
        logExtent(Status.INFO, "Получено сообщение алерта: " + alertMessage);
        return alertMessage;
    }

    @Step("Проверить текст результата")
    private void verifyResultText() {
        page.waitForSelector("#result", new Page.WaitForSelectorOptions().setTimeout(5000));
        String resultText = page.locator("#result").textContent();
        assertThat(resultText).contains("You successfully clicked an alert");
        logExtent(Status.INFO, "Результирующий текст проверен: " + resultText);
    }

    @Attachment(value = "Успешный скриншот", type = "image/png")
    private byte[] captureSuccessScreenshot() {
        byte[] screenshot = page.screenshot();
        try (InputStream screenshotStream = new ByteArrayInputStream(screenshot)) {
            Allure.addAttachment("Успешное выполнение", "image/png", screenshotStream, ".png");
        } catch (IOException e) {
            logExtent(Status.WARNING, "Не удалось добавить скриншот в Allure: " + e.getMessage());
        }
        test.addScreenCaptureFromPath("success-screenshot.png");
        return screenshot;
    }

    private void logExtent(Status status, String message) {
        test.log(status, message);
    }

    private void handleTestFailure(Exception e) {
        byte[] failureScreenshot = page.screenshot();
        try (InputStream failureStream = new ByteArrayInputStream(failureScreenshot)) {
            Allure.addAttachment("Ошибка теста", "image/png", failureStream, ".png");
        } catch (IOException ex) {
            logExtent(Status.WARNING, "Не удалось добавить скриншот ошибки в Allure: " + ex.getMessage());
        }
        test.fail("Тест завершился с ошибкой: " + e.getMessage())
                .addScreenCaptureFromPath("error-screenshot.png");
        logExtent(Status.FAIL, "Тест завершился с ошибкой: " + e.getMessage());
    }

    @AfterEach
    void tearDownEach() {
        if (page != null) page.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @AfterAll
    static void tearDown() {
        extent.flush();
    }
}




