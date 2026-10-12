package com.certportal.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

/** Journey 2: create, view and update a certificate record. */
class CertificateCrudIT extends BaseIT {

    @Test
    void createdCertificateIsShownAsDraftWithANumber() {
        loginAs("operator");
        String name = uniqueName("Create ");
        createCertificate(name, "DevOps Essentials");

        assertTrue(text("flash").contains("created"));
        assertEquals(name, text("name"));
        assertEquals("DevOps Essentials", text("course"));
        assertEquals("Draft", text("status"));
        assertTrue(text("cert-no").startsWith("CERT-"));
    }

    @Test
    void invalidFormShowsValidationMessagesAndSavesNothing() {
        loginAs("operator");
        driver.get(baseUrl + "/certificates/new");
        type(By.id("recipientEmail"), "not-an-email");
        click("save");

        assertTrue(exists("error-recipientName"));
        assertTrue(exists("error-recipientEmail"));
        assertTrue(exists("error-course"));
        assertTrue(exists("error-issueDate"));
        assertTrue(driver.getCurrentUrl().endsWith("/certificates"), "Form should be shown again: " + driver.getCurrentUrl());
    }

    @Test
    void editingADraftUpdatesTheRecord() {
        loginAs("operator");
        createCertificate(uniqueName("Edit "), "Git Basics");

        click("edit-link");
        type(By.id("course"), "Advanced Git");
        click("save");

        assertTrue(text("flash").contains("updated"));
        assertEquals("Advanced Git", text("course"));
        assertEquals("Draft", text("status"));
    }
}
