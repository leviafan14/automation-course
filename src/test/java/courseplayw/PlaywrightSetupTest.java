package courseplayw;

import base.BaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PlaywrightSetupTest extends BaseTest {

    @Test
    void testPlaywrightSetup() {
        page.navigate("https://example.com");
        String title = page.title();
        Assertions.assertEquals("Example Domain", title);
    }
}