package com.certportal.it;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Shared set-up for the Selenium tests: starts the application once, opens a fresh Chrome per test and
 * offers small helpers (login, create certificate, read values by data-testid).
 *
 * Options (pass with -D on the Maven command line):
 *   -Dheadless=false        watch the browser
 *   -Dchrome.binary=PATH    use a specific Chrome/Chromium binary
 *   -DbaseUrl=URL           test an already deployed server instead of the embedded one
 */
@ExtendWith(ScreenshotOnFailure.class)
abstract class BaseIT {

    static String baseUrl;
    protected WebDriver driver;

    @BeforeAll
    static void startApplication() throws Exception {
        baseUrl = TestServer.start();
    }

    @BeforeEach
    void openBrowser() {
        ChromeOptions options = new ChromeOptions();
        if (!"false".equalsIgnoreCase(System.getProperty("headless"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--window-size=1400,1000");
        String binary = System.getProperty("chrome.binary");
        if (binary != null && !binary.isBlank()) {
            options.setBinary(binary);
        }
        driver = new ChromeDriver(options);
    }

    @AfterEach
    void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ---------- failure evidence ----------

    void captureFailure(String testName) {
        if (driver == null) {
            return;
        }
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path dir = Paths.get(System.getProperty("screenshotDir", "target/screenshots"));
        try {
            Files.createDirectories(dir);
            File png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path target = dir.resolve(testName + "-" + stamp + ".png");
            Files.copy(png.toPath(), target);
            Files.writeString(dir.resolve(testName + "-" + stamp + ".html"), driver.getPageSource(), StandardCharsets.UTF_8);
            System.out.println("[Screenshot] Saved failure evidence: " + target.toAbsolutePath());
        } catch (IOException | RuntimeException e) {
            System.out.println("[Screenshot] Could not save failure evidence: " + e.getMessage());
        }
    }

    // ---------- helpers ----------

    protected static By testId(String id) {
        return By.cssSelector("[data-testid='" + id + "']");
    }

    protected String text(String id) {
        return driver.findElement(testId(id)).getText().trim();
    }

    protected boolean exists(String id) {
        return !driver.findElements(testId(id)).isEmpty();
    }

    protected void type(By locator, String value) {
        driver.findElement(locator).clear();
        driver.findElement(locator).sendKeys(value);
    }

    protected void click(String id) {
        driver.findElement(testId(id)).click();
    }

    protected static String uniqueName(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8);
    }

    /** Logs in as a demo user ("operator", "approver" or "admin") starting from a clean session. */
    protected void loginAs(String username) {
        driver.get(baseUrl + "/login");
        driver.manage().deleteAllCookies();
        driver.get(baseUrl + "/login");
        type(By.id("username"), username);
        type(By.id("password"), username + "123");
        click("login-submit");
    }

    /** Creates a draft certificate (user must be logged in as operator/admin) and returns the view-page URL. */
    protected String createCertificate(String recipientName, String course) {
        driver.get(baseUrl + "/certificates/new");
        type(By.id("recipientName"), recipientName);
        type(By.id("recipientEmail"), "selenium.user@example.com");
        type(By.id("course"), course);
        type(By.id("issueDate"), java.time.LocalDate.now().toString());
        type(By.id("remarks"), "Created by Selenium");
        click("save");
        return driver.getCurrentUrl();
    }
}
