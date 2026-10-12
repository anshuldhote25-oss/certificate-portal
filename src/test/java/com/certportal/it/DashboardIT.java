package com.certportal.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Journey 5: the summary dashboard. */
class DashboardIT extends BaseIT {

    private int count(String name) {
        return Integer.parseInt(text("count-" + name));
    }

    private int sumOfStatusCounts() {
        return count("DRAFT") + count("SUBMITTED") + count("APPROVED") + count("REJECTED") + count("ISSUED");
    }

    @Test
    void totalEqualsTheSumOfAllStatusCounts() {
        loginAs("admin");
        driver.get(baseUrl + "/dashboard");
        assertTrue(count("total") >= 4, "Demo data contains at least four records");
        assertEquals(count("total"), sumOfStatusCounts());
    }

    @Test
    void creatingACertificateIncreasesTotalAndDraftCounts() {
        loginAs("operator");
        driver.get(baseUrl + "/dashboard");
        int totalBefore = count("total");
        int draftBefore = count("DRAFT");

        createCertificate(uniqueName("Dash "), "Dashboard Course");
        driver.get(baseUrl + "/dashboard");

        assertEquals(totalBefore + 1, count("total"));
        assertEquals(draftBefore + 1, count("DRAFT"));
    }

    @Test
    void footerShowsTheEnvironmentLabel() {
        loginAs("operator");
        assertTrue(text("env-badge").startsWith("Environment:"));
    }
}
