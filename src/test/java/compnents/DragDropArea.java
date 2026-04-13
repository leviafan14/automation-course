package components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class DragDropArea {
    private final Page page;

    public DragDropArea(Page page) {
        this.page = page;
    }

    // Метод перетаскивания элемента A в зону B
    public void dragAToB() {
        Locator elementA = page.locator("#column-a");
        Locator elementB = page.locator("#column-b");
        elementA.dragTo(elementB);
    }

    // Получение текста из зоны "B" для верификации
    public String getTextB() {
        return page.locator("#column-b").textContent();
    }
}
