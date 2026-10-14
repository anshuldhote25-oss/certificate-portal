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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
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

    /**
     * A 10-second wait that keeps trying when Chrome reports a passing error while a page is being
     * replaced (stale or "node does not belong to the document" errors).
     */
    private WebDriverWait waiter() {
        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(10));
        w.ignoring(WebDriverException.class);
        return w;
    }

    /** Waits (up to 10 s) until the element is on the page and visible, then returns it. */
    protected WebElement visible(By locator) {
        return waiter().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected String text(String id) {
        return visible(testId(id)).getText().trim();
    }

    /** Immediate check, so call it only once the page has finished loading (click() and driver.get() make sure of that). */
    protected boolean exists(String id) {
        return !driver.findElements(testId(id)).isEmpty();
    }

    protected void type(By locator, String value) {
        WebElement field = visible(locator);
        field.clear();
        field.sendKeys(value);
    }

    /**
     * Clicks an element that navigates (a link or a submit button) and waits until the browser is showing
     * the next page, fully loaded. Without this wait the next step can run while the form post is still in
     * flight, which makes tests fail at random.
     *
     * The old page is marked before the click; a freshly loaded page does not carry the mark. This does not
     * rely on stale-element errors, which Chrome does not report reliably.
     */
    protected void click(String id) {
        WebElement element = waiter().until(ExpectedConditions.elementToBeClickable(testId(id)));
        awaitPaint();
        script("document.documentElement.setAttribute('data-nav-mark', '1')");
        element.click();

        if (!changedWithin(4)) {
            try {
                if (!pageIsNew()) {
                    System.out.println("[Retry] clicking '" + id + "' had no effect after 4 s; clicking again");
                    element.click();
                }
            } catch (WebDriverException e) {
                // the old element is gone, so the page did change after all
            }
        }
        waiter().withMessage("the page did not change after clicking '" + id + "'")
                .until(d -> pageIsNew());
        waitForPageLoad();
    }

    private boolean changedWithin(int seconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(seconds)).until(d -> pageIsNew());
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Lets the browser draw the page before we click (two animation frames, or 0.5 s at most). */
    private void awaitPaint() {
        try {
            ((JavascriptExecutor) driver).executeAsyncScript(
                    "var done = arguments[arguments.length - 1];"
                    + "var finished = false;"
                    + "function finish() { if (!finished) { finished = true; done(true); } }"
                    + "requestAnimationFrame(function () { requestAnimationFrame(finish); });"
                    + "setTimeout(finish, 500);");
        } catch (WebDriverException e) {
            // not essential; carry on
        }
    }

    private boolean pageIsNew() {
        try {
            return script("return document.documentElement.getAttribute('data-nav-mark')") == null;
        } catch (WebDriverException e) {
            return false;   // the old page is being torn down; ask again
        }
    }

    protected void waitForPageLoad() {
        waiter().until(d -> {
            try {
                return "complete".equals(script("return document.readyState"));
            } catch (WebDriverException e) {
                return false;   // page is switching documents; try again
            }
        });
    }

    private Object script(String javascript) {
        return ((JavascriptExecutor) driver).executeScript(javascript);
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
        waiter().until(ExpectedConditions.urlContains("/dashboard"));
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
        waiter().until(ExpectedConditions.urlMatches(".*/certificates/\\d+$"));
        return driver.getCurrentUrl();
    }
}
