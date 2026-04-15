package auto;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;


import pages.DynamicControlsPage;
import pages.TestContext;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class DynamicControlsTest {
    private TestContext context;
    private DynamicControlsPage controlsPage;

    @BeforeEach
    public void setup() {
        context = new TestContext();
        controlsPage = new DynamicControlsPage(context.getPage());
        context.getPage().navigate("https://the-internet.herokuapp.com/dynamic_controls");
    }

    @Test
    public void testCheckboxRemoval() {
        // Проверяем, что чекбокс изначально виден
        controlsPage.waitForCheckboxState(true);
        assertTrue(controlsPage.isCheckboxVisible(), "Чекбокс должен быть изначально виден");

        // Нажимаем кнопку Remove
        controlsPage.clickRemoveButton();

        // Ждём скрытия чекбокса и проверяем
        controlsPage.waitForCheckboxState(false);
        assertFalse(controlsPage.isCheckboxVisible(), "Чекбокс должен исчезнуть после нажатия кнопки Remove");
    }

    @AfterEach
    public void teardown() {
        context.close();
    }
}

