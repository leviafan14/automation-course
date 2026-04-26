package auto;
import com.example.config.AuthTestConfig;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;

public class LoginTest {
    Page page;
    AuthHelper authHelper;
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;

    @BeforeAll
    public static void setupPlaywright() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();

    }

    @BeforeEach
    public void setup() {
        context =browser.newContext();
        page = context.newPage();
    }

    @Test
    @DisplayName("Авторизация с валидным логином и валидным паролем")
    @Tag("Positive")
    public void testValidLoginInSite() throws IOException {
        boolean isContainSecure = false;
        try {
            String login = AuthTestConfig.getLoginProperty();
            String password = AuthTestConfig.getPasswordProperty();
            System.out.println(login + " " + password);
            authHelper = new AuthHelper(page);

            authHelper.loginInSite(login, password);
            Locator successBlock = authHelper.getSuccessDiv();
            successBlock.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

            assertTrue(successBlock.isVisible());

            page.waitForLoadState(LoadState.NETWORKIDLE);
            String pageUrl = page.url();
            isContainSecure = pageUrl.contains("/secure");
            assertTrue("Пользователь не перенаправленв в личный кабинет", isContainSecure);

        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @CsvSource({"tomsmith, SuperSecretPassword", "tomsmtit, SuperSecretPassword!", "tom,SuperSecretPassword"})
    @DisplayName("Негативный тест. Авторизация с невалидными данными")
    @Tag("Negative")
    public void testInvalidLoginInSite(String login, String password) throws IOException {
        try {
            boolean isContainLogin = false;
            authHelper = new AuthHelper(page);

            authHelper.loginInSite(login, password);
            System.out.println(login + " " + password);
            Locator errorBlock = authHelper.getErrorDiv();
            errorBlock.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

            assertTrue(errorBlock.isVisible());

            page.waitForLoadState(LoadState.NETWORKIDLE);

            String pageUrl = page.url();
            isContainLogin = pageUrl.contains("/login");

            assertTrue("Произошла переадресация на другую страницу", isContainLogin);
            context.close();
            page.close();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            throw e;
        }
    }

    @AfterEach
    public void teardown() {
        if (context != null){
            context.close();
        }
        if (page != null){
            page.close();
        }
        if (authHelper != null){
            authHelper = null;

        }
    }

    @AfterAll
    public static void closeResources(){
        if (browser != null){
            browser.close();
        }
        if (playwright != null){
            playwright.close();
        }
    }
}


