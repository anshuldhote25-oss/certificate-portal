package com.certportal.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

/** Journey 1: signing in and out. */
class LoginIT extends BaseIT {

    @Test
    void validLoginShowsDashboardForTheUser() {
        loginAs("operator");
        assertTrue(driver.getCurrentUrl().endsWith("/dashboard"), "Expected dashboard but was " + driver.getCurrentUrl());
        assertEquals("Olivia Operator", text("current-user"));
        assertEquals("Operator", text("current-role"));
    }

    @Test
    void wrongPasswordShowsAnErrorAndStaysOnLogin() {
        driver.get(baseUrl + "/login");
        type(By.id("username"), "operator");
        type(By.id("password"), "not-the-password");
        click("login-submit");
        visible(testId("login-error"));
        assertTrue(text("login-error").contains("Invalid username or password"));
        assertTrue(driver.getCurrentUrl().endsWith("/login"));
    }

    @Test
    void protectedPageRedirectsAnonymousVisitorsToLogin() {
        driver.get(baseUrl + "/login");
        driver.manage().deleteAllCookies();
        driver.get(baseUrl + "/certificates");
        assertTrue(driver.getCurrentUrl().endsWith("/login"), "Expected login but was " + driver.getCurrentUrl());
    }

    @Test
    void logoutEndsTheSession() {
        loginAs("admin");
        click("logout");
        awaitUrl(".*/login$");
        driver.get(baseUrl + "/dashboard");
        assertTrue(driver.getCurrentUrl().endsWith("/login"));
    }
}
