package auto;
import base.BaseLoginTest;
import com.microsoft.playwright.Page;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
// Практическое задание №31 - тест до рефакторинга
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;

public class LoginTest extends BaseLoginTest {
    Page page;
    TestResult currentResult;

    @BeforeMethod
    public void setup(ITestResult result) {
        super.setup();
        page = browser.newPage();

        // Получаем имя теста из ITestResult
        currentResult = new TestResult(result.getName());
        currentResult.startTime = System.currentTimeMillis();
    }

    @Test
    public void testLogin() {
        try {
            page.navigate("https://the-internet.herokuapp.com/login");
            page.locator("#username").fill("tomsmith");
            page.locator("#password").fill("SuperSecretPassword!");
            page.locator("button[type='submit']").click();
            assertTrue(page.locator(".flash.success").isVisible());
            currentResult.status = "PASSED";
        } catch (Exception e) {
            // Если тест упал — сохраняем скриншот и ошибку
            try {
                currentResult.screenshotPath = saveScreenshot(page, currentResult.testName);
            } catch (IOException ioe) {
                System.err.println("Ошибка при сохранении скриншота: " + ioe.getMessage());
            }
            currentResult.status = "FAILED";
            currentResult.errorMessage = e.getMessage();
            throw e;
        }
    }

    @AfterMethod
    public void teardown(ITestResult result) {
        page.close();

        // Считаем длительность и добавляем в общий список
        currentResult.endTime = System.currentTimeMillis();
        currentResult.duration = currentResult.endTime - currentResult.startTime;
        results.add(currentResult);
    }
}


