package tests;

import com.microsoft.playwright.BrowserType;
import org.junit.jupiter.api.Test;
import pages.DragDropPage;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Page;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DragDropTest {
    private Playwright playwright = Playwright.create();
    private Page page;

    @Test
    public void testDragAndDrop() {
        page = playwright.chromium().launch()
                .newPage(); // Создаём объект Page

        // Инициализация страницы
        DragDropPage dragDropPage = new DragDropPage(page);

        // Переход на страницу
        dragDropPage.navigateTo("https://the-internet.herokuapp.com/drag_and_drop");

        // Цепочка вызовов
        dragDropPage.dragDropArea().dragAToB();

        // Проверка, что текст в зоне "B" стал "A"
        String textInB = dragDropPage.dragDropArea().getTextB();
        assertEquals("A", textInB, "Текст в зоне B должен быть A после перетаскивания");
    }
}

