package auto;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;

public class FileUploadTest {
    Playwright playwright;
    APIRequestContext request;

    @BeforeEach
    void setUp() {
        playwright = Playwright.create();
        request = playwright.request().newContext();
    }

    @Test
    void testFileUploadAndDownload() throws Exception {
        // 1. Генерация тестового PNG-файла в памяти
        byte[] testFileBytes = generateTestPngImage();

        APIResponse uploadResponse = request.post(
                "https://httpbin.org/post",
                RequestOptions.create().setMultipart(
                        FormData.create().set(
                                "file",
                                new FilePayload("test.png", "image/png", testFileBytes)
                        )
                )
        );

        // Проверка статуса ответа
        assertEquals(200, uploadResponse.status(), "Статус ответа при загрузке должен быть 200");

        // 3. Проверка получения файла сервером (наличие base64-данных в ответе)
        String responseBody = uploadResponse.text();
        assertTrue(responseBody.contains("data:image/png;base64"),
                "Ответ должен содержать base64 данные PNG-файла");

        // 4. Верификация содержимого: сравнение исходного и загруженного файла
        String base64Data = extractBase64FromResponse(responseBody);
        byte[] receivedBytes = Base64.getDecoder().decode(base64Data);

        assertArrayEquals(testFileBytes, receivedBytes,
                "Содержимое загруженного файла должно точно соответствовать исходному");

        // 5. Скачивание эталонного PNG-файла
        APIResponse downloadResponse = request.get("https://httpbin.org/image/png");

        // Получаем байты из ответа
        byte[] downloadedContent = downloadResponse.body();

        // Сохраняем на диск
        try (FileOutputStream fos = new FileOutputStream("src/test/resources/test.png")) {
            fos.write(downloadedContent);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении файла", e);
        }

        // Проверка статуса ответа при скачивании
        assertEquals(200, downloadResponse.status(), "Статус ответа при скачивании должен быть 200");

        // 6. Проверка корректности MIME-типа
        String contentType = downloadResponse.headers().get("content-type");
        assertEquals("image/png", contentType, "MIME-тип должен быть image/png");

        // 7. Проверка валидности формата через сигнатуру файла
        validatePngSignature(downloadedContent);

        System.out.println("All PASS");
    }

    /**
     * Генерирует простой PNG-файл в памяти (1x1 пиксель, чёрный цвет)
     */
    private byte[] generateTestPngImage() throws Exception {
        // Создаём пустое изображение размером 1x1 пиксель
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

        // Устанавливаем цвет пикселя (например, красный — 0xFF0000)
        image.setRGB(0, 0, 0xFF0000); // RGB: FF (красный), 00 (зелёный), 00 (синий)

        // Конвертируем изображение в байтовый массив (PNG формат)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        baos.flush();

        // Получаем байты изображения
        byte[] testFileBytes = baos.toByteArray();
        baos.close();

        return testFileBytes;
    }

    /**
     * Извлекает base64-данные из ответа сервера
     */
    private String extractBase64FromResponse(String responseBody) {
        int start = responseBody.indexOf("data:image/png;base64,") + "data:image/png;base64,".length();
        int end = responseBody.indexOf("\"", start);
        if (end == -1) {
            end = responseBody.length();
        }
        return responseBody.substring(start, end);
    }

    /**
     * Проверяет сигнатуру PNG-файла (первые 8 байт)
     */
    private void validatePngSignature(byte[] content) {
        assertNotNull(content, "Содержимое файла не должно быть null");
        assertTrue(content.length >= 8, "Файл слишком короткий для PNG");

        assertEquals(0x89, content[0] & 0xFF, "Первый байт сигнатуры PNG должен быть 0x89");
        assertEquals(0x50, content[1] & 0xFF, "Второй байт сигнатуры PNG должен быть 0x50 (P)");
        assertEquals(0x4E, content[2] & 0xFF, "Третий байт сигнатуры PNG должен быть 0x4E (N)");
        assertEquals(0x47, content[3] & 0xFF, "Четвёртый байт сигнатуры PNG должен быть 0x47 (G)");
        assertEquals(0x0D, content[4] & 0xFF, "Пятый байт сигнатуры PNG должен быть 0x0D");
        assertEquals(0x0A, content[5] & 0xFF, "Шестой байт сигнатуры PNG должен быть 0x0A");
        assertEquals(0x1A, content[6] & 0xFF, "Седьмой байт сигнатуры PNG должен быть 0x1A");
        assertEquals(0x0A, content[7] & 0xFF, "Восьмой байт сигнатуры PNG должен быть 0x0A");
    }

    @AfterEach
    void tearDown() {
        if (request != null) {
            request.dispose();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}

