package com.example;

import com.example.config.EnvironmentConfig;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatusCodeTest {
    private EnvironmentConfig config;
    private BrowserManager browserManager;
    private Browser browser;
    private Page page;

    private int responseStatus;

    @BeforeEach
    void setup() {
        config = ConfigFactory.create(EnvironmentConfig.class, System.getenv());
        browserManager = new BrowserManager(config);
        browser = browserManager.createBrowser();
        page = browser.newPage();
    }

    @Test
    void testStatusCode200() {
        // Подписываемся на событие получения ответа
        page.onResponse(response -> {
            // Фильтруем: берём статус только для основного документа (HTML)
            if ("document".equals(response.request().resourceType())) {
                responseStatus = response.status();
                System.out.println("Перехвачен статус-код: " + responseStatus);
            }
        });

        // Навигацию выполняем после подписки на события
        page.navigate(config.baseUrl() );

        // Ждём завершения сетевой активности
        page.waitForLoadState(LoadState.NETWORKIDLE);

        // Проверяем ожидаемый статус-код
        assertEquals(200, responseStatus, "Статус-код должен быть 200");
    }

    @AfterEach
    void teardown() {
        if (page != null) {
            page.close();
        }
        if (browser != null) {
            browser.close();
        }
    }
}

