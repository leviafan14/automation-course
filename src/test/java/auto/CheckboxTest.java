package auto;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;

import com.microsoft.playwright.options.ScreenshotType;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.nio.file.Paths;
import static org.assertj.core.api.Assertions.assertThat;

@Epic("Тест веб-интерфейсов")
@Feature("Операции с чекбоксами")
public class CheckboxTest {

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeEach
    @Step("Инициализация браузера и контекста")
    void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        context = browser.newContext();
        page = context.newPage();
    }

    @Test
    @Story("Проверка работы чекбоксов")
    @DisplayName("Тестирование выбора/снятия чекбоксов")
    @Severity(SeverityLevel.CRITICAL)
    void testCheckboxes() {
        navigateToCheckboxesPage();
        verifyInitialState();
        toggleCheckboxes();
        verifyToggledState();
    }

    @Step("Переход на страницу /checkboxes")
    private void navigateToCheckboxesPage() {
        page.navigate("https://the-internet.herokuapp.com/checkboxes");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Allure.step("Переход на страницу 'checkboxses' выполнен");
    }

    @Step("Проверка начального состояния чекбоксов")
    private void verifyInitialState() {
        boolean firstCheckboxChecked = page.locator("input[type='checkbox']:first-child").isChecked();
        boolean secondCheckboxChecked = page.locator("input[type='checkbox']:last-child").isChecked();

        assertThat(firstCheckboxChecked).as("Первый чекбокс должен быть не отмечен").isFalse();
        assertThat(secondCheckboxChecked).as("Второй чекбокс должен быть отмечен").isTrue();

        Allure.step("Начальное состояние чекбоксов проверено: первый — не отмечен, второй — отмечен");
    }

    @Step("Изменение состояния чекбоксов")
    private void toggleCheckboxes() {
        // Отмечаем первый чекбокс
        page.locator("input[type='checkbox']:first-child").check();
        Allure.step("Первый чекбокс отмечен");

        // Снимаем отметку со второго чекбокса
        page.locator("input[type='checkbox']:last-child").uncheck();
        Allure.step("Второй чекбокс снят");
    }

    @Step("Проверка изменённого состояния чекбоксов")
    private void verifyToggledState() {
        boolean firstCheckboxChecked = page.locator("input[type='checkbox']:first-child").isChecked();
        boolean secondCheckboxChecked = page.locator("input[type='checkbox']:last-child").isChecked();

        assertThat(firstCheckboxChecked).as("Первый чекбокс должен быть отмечен").isTrue();
        assertThat(secondCheckboxChecked).as("Второй чекбокс НЕ должен быть отмечен").isFalse();

        Allure.step("Изменённое состояние чекбоксов проверено: первый — отмечен, второй — не отмечен");
    }

    @AfterEach
    @Step("Закрытие ресурсов")
    void tearDown() {
        try {
            // Делаем скриншот при любом исходе теста для отчёта Allure
            byte[] screenshotBytes = page.screenshot(
                    new Page.ScreenshotOptions()
                            .setFullPage(true)
                            .setType(ScreenshotType.JPEG)
            );
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(screenshotBytes);

            Allure.addAttachment(
                    "Скриншот после теста",
                    "image/png",
                    byteArrayInputStream,
                    "png"
            );
        } catch (Exception ignored) {
            // Игнорируем ошибки при создании скриншота
        } finally {
            context.close();
            browser.close();
            playwright.close();
        }
    }
}
