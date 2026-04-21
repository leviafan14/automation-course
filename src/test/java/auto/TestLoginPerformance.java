package auto;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import org.junit.jupiter.api.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestLoginPerformance {
    static private Playwright playwright;
    static private Browser browser;
    private BrowserContext context;
    private Page page;
    static private List<Cookie> authCookies;
    // 3 секунды на выполнение теста
    private static final long MAX_LOGIN_TIME_MS = 3000;
    // 10% от всех запусков
    private static final double TRACE_SAMPLING_RATE = 0.1;
    private static final Random random = new Random();

    @BeforeAll
    static void setUpClass() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();

        // Выполняем логин один раз и сохраняем cookies
        try (BrowserContext tempContext = browser.newContext()) {
            Page tempPage = tempContext.newPage();
            authCookies = performLoginWithPerformanceCheck(tempPage);
            tempPage.close();
        }
    }

    @BeforeEach
    void setUp() {
        // Включаем трассировку для 10% запусков
        boolean shouldTrace = random.nextDouble() < TRACE_SAMPLING_RATE;
        context = browser.newContext();

        if (shouldTrace) {
            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true));
        }

        // Добавляем сохранённые cookies для каждого теста
        if (authCookies != null && !authCookies.isEmpty()) {
            context.addCookies(authCookies);
        }
        page = context.newPage();
    }

    @Test
    void testSecureArea() {
        long startTime = System.currentTimeMillis();

        try {
            page.navigate("https://the-internet.herokuapp.com/secure");
            // Проверяем, что пользователь аутентифицирован
            assertTrue(page.locator("h2").textContent().contains("Secure Area"));

            long duration = System.currentTimeMillis() - startTime;

            // Проверка производительности: время выполнения не должно превышать трех секунд
            assertTrue(duration < MAX_LOGIN_TIME_MS,
                    String.format("Secure area access took %d ms (exceeds %d ms limit)", duration, MAX_LOGIN_TIME_MS));

        } catch (AssertionError e) {
            // В случае ошибки трассировка сохраняется
            saveTraceIfNeeded("failed-secure-area-trace.zip");
            throw e;
        }
        finally {
            // Трассировка сохраняется, если время выполнения превысило лимит
            long duration = System.currentTimeMillis() - startTime;
            if (duration > MAX_LOGIN_TIME_MS) {
                saveTraceIfNeeded(String.format("slow-secure-area-%dms-trace.zip", duration));
            }
        }
    }

    private static List<Cookie> performLoginWithPerformanceCheck(Page page) {
        long startTime = System.currentTimeMillis();
        String traceFileName = null;

        try {
            // Включение трассировки для проверки производительности авторизации
            page.context().tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true));

            // Переходим на страницу логина
            page.navigate("https://the-internet.herokuapp.com/login");
            page.fill("#username", "tomsmith");
            page.fill("#password", "SuperSecretPassword!");
            page.click("button[type='submit']");

            // Ждём загрузки защищённой области
            page.waitForSelector("h2");

            long duration = System.currentTimeMillis() - startTime;

            // Проверка производительности авторизации
            assertTrue(duration < MAX_LOGIN_TIME_MS,
                    String.format("Login took %d ms (exceeds %d ms limit)", duration, MAX_LOGIN_TIME_MS));

            traceFileName = String.format("login-performance-%dms-trace.zip", duration);
            return page.context().cookies();

        }
        catch (Exception e) {
            traceFileName = "failed-login-trace.zip";
            throw new RuntimeException("Login failed during performance check", e);
        }
        finally {
            if (traceFileName != null) {
                try {
                    page.context().tracing().stop(new Tracing.StopOptions()
                            .setPath(Paths.get(traceFileName)));
                    System.out.println("Trace saved: " + traceFileName);
                } catch (Exception ex) {
                    System.err.println("Failed to save trace: " + ex.getMessage());
                }
            }
        }
    }

    private void saveTraceIfNeeded(String traceFileName) {
        try {
            context.tracing().stop(new Tracing.StopOptions()
                    .setPath(Paths.get(traceFileName)));
            System.out.println("Trace saved: " + traceFileName);
        } catch (Exception e) {
            System.err.println("Failed to save trace: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        if (page != null) page.close();
        if (context != null) context.close();
    }

    @AfterAll
    static void tearDownClass() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}
