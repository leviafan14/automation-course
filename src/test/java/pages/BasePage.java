package pages;

import com.microsoft.playwright.Page;

public class BasePage {
    protected final Page page;

    public BasePage(Page page) {
        this.page = page;
    }

    // Пример метода навигации (может быть расширен)
    public void navigateTo(String url) {
        page.navigate(url);
    }
}

