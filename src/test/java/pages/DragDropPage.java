package pages;
import com.microsoft.playwright.*;

public class DragDropPage extends BasePage {
    private components.DragDropArea dragDropArea; // Поле для компонента

    public DragDropPage(Page page) {
        super(page);
    }

    // Метод с ленивой инициализацией компонента
    public components.DragDropArea dragDropArea() {
        if (dragDropArea == null) {
            dragDropArea = new components.DragDropArea(page); // Инициализация при первом вызове
        }
        return dragDropArea;
    }
}
