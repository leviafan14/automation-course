package auto;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.*;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;


public class LoginDbTest {
    private Connection connection;
    private Page page;
    private Browser browser;
    private static DbConfig dbConfig;

    @BeforeAll
    static void loadConfig() {
        dbConfig = ConfigFactory.create(DbConfig.class, System.getProperties());
    }

    @BeforeEach
    void setup() throws SQLException {
        // Создание пользователя в БД
        connection = DriverManager.getConnection(
                dbConfig.dbUrl(),
                dbConfig.dbUser(),
                dbConfig.dbPassword()
        );

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(
                    "INSERT INTO users (username, password) VALUES ('test_user', 'test_pass')"
            );
        }

        // Инициализация Playwright
        Playwright playwright = Playwright.create();
        browser = playwright.chromium().launch();
        page = browser.newPage();
    }

    @Test
    void testLoginWithDbUser() throws SQLException {
        // Ожидаемый в блоке текст
        String expectedText = " Your username is invalid!\n" +
                "×";

        // Получение данных из БД
        String username = null;
        String password = null;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT username, password FROM users WHERE username = 'test_user'")) {

            if (rs.next()) {
                username = rs.getString("username");
                password = rs.getString("password");
            }
        }

        assertNotNull(username, "Username not found in DB");
        assertNotNull(password, "Password not found in DB");

        Locator invaliUserFlash = page.locator(".flash.error");

        // Выполнение логина
        page.navigate("https://the-internet.herokuapp.com/login");
        page.locator("#username").fill(username);
        page.locator("#password").fill(password);
        page.locator("button[type='submit']").click();

        // Ожидание появления элемента с таймаутом 5 секунд
        invaliUserFlash.waitFor(new Locator.WaitForOptions()
                .setTimeout(5000)
                .setState(WaitForSelectorState.VISIBLE));


        // Проверка НЕУДАЧНОЙ авторизации
        assertTrue(invaliUserFlash.isVisible(), "Сообщение об ошибке авторизации НЕ появилось на экране");
        assertEquals(invaliUserFlash.innerText(),expectedText, "Текст в блоке НЕ совпадает с ожидаемым");
    }

    @AfterEach
    void teardown() throws SQLException {
        // Удаление тестового пользователя
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(
                    "DELETE FROM users WHERE username = 'test_user'"
            );
        }

        // Закрытие ресурсов
        if (connection != null) connection.close();
        if (page != null) page.close();
        if (browser != null) browser.close();
    }
}
