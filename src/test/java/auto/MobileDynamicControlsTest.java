package auto;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MobileDynamicControlsTest {
    Playwright playwright;
    Browser browser;
    BrowserContext context;
    Page page;

    @BeforeEach
    void setUp(){
        playwright = Playwright.create();

        //Настройка параметров iPad Pro 11
        Browser.NewContextOptions deviceOptions = new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (iPad; CPU OS 15_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko)")
                .setViewportSize(834, 1194)
                .setDeviceScaleFactor(2)
                .setIsMobile(true)
                .setHasTouch(true);

        browser = playwright.chromium().launch();
        context = browser.newContext();
        page = context.newPage();
    }

    @Test
    void testInputEnabling(){
        page.navigate("https://the-internet.herokuapp.com/dynamic_controls");
        Locator enableButton = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Enable"));
        // Локатор поля ввода
        Locator inputField = page.locator("#input-example").locator("input[type='text']");
        // Локатор текста который появляется на экране после того как кнопка стала активной
        Locator filedIsEnabledText = page.locator("#message");
        // Нажатие на кнопку
        enableButton.click();
        // Ожидание появления текста после нажатия на кнопку
        filedIsEnabledText.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10000));

        // Проверка, что поле стало активным после нажатия на кнопку
        Assertions.assertTrue(inputField.isEnabled(), "Поле не стало активным");
    }

    @AfterEach
    void tearDown(){
        context.close();
        browser.close();
        playwright.close();
    }
}
