package auto;

import com.example.config.AuthTestConfig;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class AuthHelper {
    private final Locator locatorLoginField;
    private final Locator locatorPasswordField;
    private final Locator locatorSubmitButton;
    private final Locator successDiv;
    private final Locator errorDiv;
    private final Page page;
    private final String url;



    public AuthHelper(Page page){
       this.locatorLoginField = page.locator("#username");
       this.locatorPasswordField = page.locator("#password");
       this.locatorSubmitButton = page.locator("button[type='submit']");
       this.successDiv = page.locator(".flash.success");
       this.errorDiv = page.locator(".flash.error");
       this.page = page;
       this.url = AuthTestConfig.getUrlProperty();

    }

    public void loginInSite(String login, String password){
        String loginUrl = this.url + "/login";
        page.navigate(loginUrl);
        this.locatorLoginField.fill(login);
        this.locatorPasswordField.fill(password);
        this.locatorSubmitButton.click();
    }

    public Locator getSuccessDiv(){
        return this.successDiv;
    }

    public Locator getErrorDiv(){
        return this.errorDiv;
    }
}
