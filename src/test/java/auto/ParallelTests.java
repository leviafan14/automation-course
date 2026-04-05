package auto;
import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
class ParallelTests {

    private static Playwright playwright;

    @BeforeAll
    static void createPlaywright() {
        playwright = Playwright.create();
        System.out.println("Playwright initialized (Thread: " + Thread.currentThread().getId() + ")");
    }
    // Для каждого теста создается отдельный playwright
    @Test
    void testLoginPage() {
        try (Playwright localPlaywright = Playwright.create()) {
            try (Browser browser = localPlaywright.chromium().launch()) {
                try (BrowserContext context = browser.newContext()) {
                    Page page = context.newPage();

                    System.out.println("Тест 1 (Login Page) запущен в потоке: " + Thread.currentThread().getId());

                    page.navigate("https://the-internet.herokuapp.com/login");
                    assertEquals("The Internet", page.title(),
                            "Заголовок страницы не соответствует ожидаемому");

                }
            }
        }
    }

    @Test
    void testAddRemoveElements() {
        try (Playwright localPlaywright = Playwright.create()) {
            try (Browser browser = localPlaywright.chromium().launch()) {
                try (BrowserContext context = browser.newContext()) {
                    Page page = context.newPage();

                    System.out.println("Тест 2 (Add/Remove Elements) запущен в потоке: " + Thread.currentThread().getId());

                    page.navigate("https://the-internet.herokuapp.com/add_remove_elements/");
                    page.click("button:text('Add Element')");
                    page.waitForSelector("button.added-manually",
                            new Page.WaitForSelectorOptions().setTimeout(5000));
                    assertTrue(page.isVisible("button.added-manually"),
                            "Элемент не появился после клика по кнопке 'Add Element'");

                }
            }
        }
    }

    @AfterAll
    static void closePlaywright() {
        if (playwright != null) {
            playwright.close();
        }
    }
}



