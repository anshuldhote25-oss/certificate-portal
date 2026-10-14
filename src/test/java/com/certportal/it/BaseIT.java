package com.certportal.it;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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

    /** How many times this test has logged in; every login after the first gets a brand-new browser. */
    private int logins;

    @BeforeEach
    void openBrowser() {
        logins = 0;
        startChrome();
    }

    private void startChrome() {
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
        printDiagnostics(testName);
    }

    /** Prints what the browser was showing and the last requests the server received, to explain a failure. */
    private void printDiagnostics(String testName) {
        try {
            String body = driver.findElement(By.tagName("body")).getText().replaceAll("\\s+", " ");
            System.out.println("[Diagnostics] " + testName + ": url=" + driver.getCurrentUrl()
                    + " title='" + driver.getTitle() + "' page text='" + (body.length() > 300 ? body.substring(0, 300) : body) + "'");
        } catch (RuntimeException e) {
            System.out.println("[Diagnostics] could not read the page: " + e.getMessage());
        }
        try (java.util.stream.Stream<Path> files = Files.list(Paths.get("target"))) {
            Path log = files.filter(f -> f.getFileName().toString().startsWith("it-access")).findFirst().orElse(null);
            if (log != null) {
                java.util.List<String> lines = Files.readAllLines(log);
                System.out.println("[Diagnostics] last server requests:");
                for (String line : lines.subList(Math.max(0, lines.size() - 25), lines.size())) {
                    System.out.println("[Diagnostics]   " + line);
                }
            }
        } catch (IOException | RuntimeException e) {
            System.out.println("[Diagnostics] could not read the access log: " + e.getMessage());
        }
    }

    // ---------- helpers ----------

    protected static By testId(String id) {
        return By.cssSelector("[data-testid='" + id + "']");
    }

    /** Pages are compiled on first use, so be patient: 30 s is far longer than any normal response. */
    private static final Duration PATIENCE = Duration.ofSeconds(30);

    /** A wait that keeps trying when Chrome reports a passing error while a page is being replaced. */
    protected WebDriverWait waiter() {
        WebDriverWait w = new WebDriverWait(driver, PATIENCE);
        w.ignoring(WebDriverException.class);
        return w;
    }

    /** Waits until the element is on the page and visible, then returns it. */
    protected WebElement visible(By locator) {
        return waiter().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected String text(String id) {
        return visible(testId(id)).getText().trim();
    }

    /** Immediate check, so call it only once the page has finished loading (driver.get() and the await* helpers make sure of that). */
    protected boolean exists(String id) {
        return !driver.findElements(testId(id)).isEmpty();
    }

    /**
     * Types into a field and checks that the field really holds the text afterwards, typing again if the
     * page replaced the field in the meantime.
     */
    protected void type(By locator, String value) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                WebElement field = visible(locator);
                field.clear();
                field.sendKeys(value);
                if (value.equals(field.getAttribute("value"))) {
                    return;
                }
            } catch (WebDriverException e) {
                // the page changed under us; try again
            }
        }
        WebElement field = visible(locator);
        assertEquals(value, field.getAttribute("value"), "Could not type into " + locator);
    }

    /** Clicks an element. What the click leads to is checked by the caller with awaitUrl / awaitText / visible. */
    protected void click(String id) {
        waiter().until(ExpectedConditions.elementToBeClickable(testId(id))).click();
    }

    /** Waits until the browser URL matches the regular expression, then until that page has finished loading. */
    protected void awaitUrl(String regex) {
        waiter().withMessage("URL never matched " + regex).until(ExpectedConditions.urlMatches(regex));
        waitForPageLoad();
    }

    /** Waits until the element shows exactly this text (the old page showing something else proves the page changed). */
    protected void awaitText(String id, String expected) {
        String[] last = {"(element not found)"};
        waiter().withMessage(() -> "Expected '" + id + "' to show '" + expected + "' but it showed '" + last[0] + "'")
                .until(d -> {
                    last[0] = "(element not found)";
                    last[0] = d.findElement(testId(id)).getText().trim();
                    return expected.equals(last[0]);
                });
        waitForPageLoad();
    }

    /** Waits until the element's text contains the given part. */
    protected void awaitTextContains(String id, String part) {
        String[] last = {"(element not found)"};
        waiter().withMessage(() -> "Expected '" + id + "' to contain '" + part + "' but it showed '" + last[0] + "'")
                .until(d -> {
                    last[0] = "(element not found)";
                    last[0] = d.findElement(testId(id)).getText().trim();
                    return last[0].contains(part);
                });
        waitForPageLoad();
    }

    protected void waitForPageLoad() {
        waiter().until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
    }

    protected static String uniqueName(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8);
    }

    /** Logs in as a demo user ("operator", "approver" or "admin") in a clean browser. */
    protected void loginAs(String username) {
        if (logins++ > 0) {
            // A second sign-in in the same Chrome window proved unreliable (the login fields stopped
            // accepting text). A new browser per user is slower by about a second but always starts clean.
            driver.quit();
            startChrome();
        }
        driver.get(baseUrl + "/login");
        type(By.id("username"), username);
        type(By.id("password"), username + "123");
        click("login-submit");
        awaitUrl(".*/dashboard$");
        visible(testId("current-user"));
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
        awaitUrl(".*/certificates/\\d+$");
        visible(testId("cert-no"));
        return driver.getCurrentUrl();
    }
}
