package auto;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;


import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class ParallelTests {

    private static Playwright playwright;

    @BeforeAll
    static void createPlaywright() {
        playwright = Playwright.create();
        System.out.println("Playwright initialized");
    }

    @Test
    void testLoginPage() {
        try (Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
             BrowserContext context = browser.newContext();
             Page page = context.newPage()) {

            page.navigate("https://the-internet.herokuapp.com/login");
            assertEquals("The Internet", page.title(), "Заголовок страницы не соответствует ожидаемому");
            System.out.println("Тест 1 (Login Page) пройден успешно");
        }
    }

    @Test
    void testAddRemoveElements() {
        try (Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
             BrowserContext context = browser.newContext();
             Page page = context.newPage()) {

            page.navigate("https://the-internet.herokuapp.com/add_remove_elements/");
            page.click("button:text('Add Element')");
            page.waitForSelector("button.added-manually");
            assertTrue(page.isVisible("button.added-manually"), "Элемент не появился на странице после клика");
            System.out.println("Тест 2 (Add/Remove Elements) пройден успешно");
        }
    }

    @AfterAll
    static void closePlaywright() {
        if (playwright != null) {
            playwright.close();
            System.out.println("Playwright closed");
        }
    }
}


