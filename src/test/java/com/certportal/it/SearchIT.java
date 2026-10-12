package com.certportal.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/** Journey 3: searching and filtering the certificate list. */
class SearchIT extends BaseIT {

    @Test
    void searchFindsARecordByPartialLowercaseName() {
        loginAs("operator");
        String name = uniqueName("Searchable ");
        createCertificate(name, "Docker Basics");

        driver.get(baseUrl + "/certificates");
        type(By.cssSelector("[data-testid='search-input']"), name.toLowerCase());
        click("search-submit");

        List<WebElement> rows = driver.findElements(testId("cert-row"));
        assertEquals(1, rows.size(), "Exactly one record should match");
        assertEquals(name, rows.get(0).findElement(testId("row-name")).getText().trim());
    }

    @Test
    void searchWithNoMatchShowsTheEmptyState() {
        loginAs("operator");
        driver.get(baseUrl + "/certificates");
        type(By.cssSelector("[data-testid='search-input']"), "zzz-no-such-record-" + System.nanoTime());
        click("search-submit");

        assertTrue(exists("empty-state"));
        assertTrue(text("result-count").startsWith("0 record"));
    }

    @Test
    void statusFilterShowsOnlyRecordsWithThatStatus() {
        loginAs("operator");
        driver.get(baseUrl + "/certificates");
        new Select(driver.findElement(testId("status-filter"))).selectByValue("ISSUED");
        click("search-submit");

        List<WebElement> rows = driver.findElements(testId("cert-row"));
        assertTrue(rows.size() >= 1, "The demo data contains at least one issued certificate");
        for (WebElement row : rows) {
            assertEquals("Issued", row.findElement(testId("row-status")).getText().trim());
        }
    }
}
