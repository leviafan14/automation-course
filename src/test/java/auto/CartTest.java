package auto;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CartTest {
    private BrowserContext context;
    private Page page;
    private Browser browser;
    private Playwright playwright;
    private static final String VIDEOS_DIR = "videos/";
    private Path timestampDir; // Директория для артефактов с датой

    @BeforeEach
    void setup() throws IOException {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();

        // Создаём директорию с текущей датой/временем
        timestampDir = Paths.get(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")));
        Path videoPath = Path.of(timestampDir + "/" + VIDEOS_DIR );
        Files.createDirectories(videoPath);
        // Видео сохраняется в ту же директорию
        context = browser.newContext(new Browser.NewContextOptions()
                .setRecordVideoDir(videoPath));
        page = context.newPage();
    }

    @Test
    void testCartActions() {
        page.navigate("https://the-internet.herokuapp.com/add_remove_elements/");
        int cartCountStart = 0;
        int cartCurrentCount = 0;

        // Флаг проверки добавления товара в корзину
        Boolean isAdded = false;
        // Флаг проверки удаления товара из корзины
        Boolean isDeleted = false;
        // Добавление товара
        page.getByText("Add Element").click();

        // Скриншот при добавлении в корзину
        page.locator("#content").screenshot(new Locator.ScreenshotOptions()
                .setPath(getArtifactPath("cart_after_add.png")));

        Locator deleteButtons = page.getByText("Delete");
        // Ждём появления хотя бы одной кнопки
        deleteButtons.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        // Получение количества кнопок Delete на экране
        cartCurrentCount = deleteButtons.count();

        // Проверка что кнопок Delete на экране больше чем при старте
        if (cartCurrentCount > cartCountStart){
            isAdded = true;
            cartCountStart = cartCurrentCount;
        }
        else{
            isAdded = false;
        }
        // Проверка, что товар добавлен в корзину
        Assertions.assertTrue(isAdded, "Товар не добавлен в корзину");

        // Удаление товара
        deleteButtons.first().click();
        // Ожидание стабилизации страницы
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        // Скриншот страницы
        page.locator("#content").screenshot(new Locator.ScreenshotOptions()
                    .setPath(getArtifactPath("cart_after_remove.png")));

        // Получение количества кнопок Delete на экране после нажатия на одну из них
        cartCurrentCount = deleteButtons.count();

        if (cartCurrentCount < cartCountStart){
            isDeleted = true;
        }
        else{
            isDeleted = false;
        }

        Assertions.assertTrue(isDeleted, "Товар не удален из корзины");

    }

    private Path getArtifactPath(String filename) {
        return timestampDir.resolve(filename);

    }



    @AfterEach
    void teardown() {
        context.close();
    }

}
