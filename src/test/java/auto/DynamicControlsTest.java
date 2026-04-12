package auto;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DynamicControlsTest {
    Playwright playwright;
    Browser browser;
    Page page;

    @BeforeEach
    void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        page = browser.newPage();
    }

    @Test
    void testDynamicCheckbox() {
        page.navigate("https://the-internet.herokuapp.com/dynamic_controls");

        // Поиск чекбокса с атрибутом type=checkbox
        Locator checkbox = page.locator("input[type='checkbox']");
        checkbox.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        assertTrue(checkbox.isVisible(), "Чекбокс должен быть изначально виден");

        // Нажатие на кнопку Remove
        Locator removeButton = page.locator("button:has-text('Remove')");
        removeButton.click();

        // Ожидание скрытия чекбокса
        checkbox.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN)
                .setTimeout(5000));
        checkbox.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));

        // Текст "It's gone!" появился
        assertTrue(checkbox.isHidden(), "Чекбокс должен быть скрыт");


        // Нажатие на кнопку Add
        Locator addButton = page.locator("button:has-text('Add')");
        addButton.click();

        // Чекбокс снова отображается
        checkbox.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(5000));

        assertTrue(checkbox.isVisible(), "Чекбокс должен снова отображаться после нажатия кнопки 'Add'");

    }

    @AfterEach
    void tearDown() {
        if (page != null) {
            page.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}

