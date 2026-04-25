package auto;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class AuthHelper {
    private final Locator locatorLoginField;
    private final Locator locatorPasswordField;
    private final Locator locatorSubmitButton;
    private final Page page;

    public AuthHelper(Page page){
       this.locatorLoginField = page.locator("#username");
       this.locatorPasswordField = page.locator("password");
       this.locatorSubmitButton = page.locator("button[type='submit']");
       this.page = page;
    }

    public void loginInSite(String login, String password){
        this.locatorLoginField.fill(login);
        this.locatorLoginField.fill(password);
        this.locatorSubmitButton.click();
    }
}
