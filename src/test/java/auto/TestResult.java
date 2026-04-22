package auto;

public class TestResult {
    public String testName;
    public long startTime;
    public long endTime;
    public long duration; // в миллисекундах
    public String status; // PASSED / FAILED
    public String screenshotPath; // путь к файлу скриншота (если есть)
    public String errorMessage; // текст ошибки

    public TestResult(String testName) {
        this.testName = testName;
    }
}
