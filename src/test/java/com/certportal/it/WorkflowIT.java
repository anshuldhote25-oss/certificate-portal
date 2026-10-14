package com.certportal.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Journey 4: the role-based status workflow (operator -> approver -> admin). */
class WorkflowIT extends BaseIT {

    @Test
    void certificateTravelsFromDraftToIssuedAcrossThreeRoles() {
        loginAs("operator");
        String url = createCertificate(uniqueName("Flow "), "Jenkins Pipelines");
        assertEquals("Draft", text("status"));
        click("action-submit");
        awaitText("status", "Submitted");

        loginAs("approver");
        driver.get(url);
        click("action-approve");
        awaitText("status", "Approved");

        loginAs("admin");
        driver.get(url);
        click("action-issue");
        awaitText("status", "Issued");
        assertFalse(exists("action-submit"), "No further actions once issued");
        assertFalse(exists("edit-link"), "Issued records cannot be edited");
    }

    @Test
    void rejectedCertificateCanBeEditedAndResubmitted() {
        loginAs("operator");
        String url = createCertificate(uniqueName("Reject "), "Ansible Basics");
        click("action-submit");
        awaitText("status", "Submitted");

        loginAs("approver");
        driver.get(url);
        click("action-reject");
        awaitText("status", "Rejected");

        loginAs("operator");
        driver.get(url);
        assertTrue(exists("edit-link"), "Rejected records are editable");
        click("action-submit");
        awaitText("status", "Submitted");
    }

    @Test
    void operatorHasNoApproveButton() {
        loginAs("operator");
        String url = createCertificate(uniqueName("Perm "), "Selenium Testing");
        click("action-submit");
        awaitText("status", "Submitted");
        driver.get(url);
        assertEquals("Submitted", text("status"));
        assertFalse(exists("action-approve"));
        assertFalse(exists("action-reject"));
    }

    @Test
    void approverCannotCreateCertificates() {
        loginAs("approver");
        assertFalse(exists("nav-new"), "The approver menu has no New certificate link");
        driver.get(baseUrl + "/certificates/new");
        assertTrue(text("error-code").contains("403"), "Direct access must be forbidden");
    }
}
