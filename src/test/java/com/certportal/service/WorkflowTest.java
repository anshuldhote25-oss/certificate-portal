package com.certportal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.certportal.model.Role;
import com.certportal.model.Status;
import com.certportal.service.Workflow.Action;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class WorkflowTest {

    @Test
    void happyPathMovesFromDraftToIssued() {
        assertEquals(Optional.of(Status.SUBMITTED), Workflow.next(Status.DRAFT, Action.SUBMIT));
        assertEquals(Optional.of(Status.APPROVED), Workflow.next(Status.SUBMITTED, Action.APPROVE));
        assertEquals(Optional.of(Status.ISSUED), Workflow.next(Status.APPROVED, Action.ISSUE));
    }

    @Test
    void rejectedRecordsCanBeResubmitted() {
        assertEquals(Optional.of(Status.REJECTED), Workflow.next(Status.SUBMITTED, Action.REJECT));
        assertEquals(Optional.of(Status.SUBMITTED), Workflow.next(Status.REJECTED, Action.SUBMIT));
    }

    @Test
    void invalidTransitionsAreRefused() {
        assertTrue(Workflow.next(Status.DRAFT, Action.APPROVE).isEmpty());
        assertTrue(Workflow.next(Status.DRAFT, Action.ISSUE).isEmpty());
        assertTrue(Workflow.next(Status.ISSUED, Action.SUBMIT).isEmpty());
        assertTrue(Workflow.next(Status.APPROVED, Action.REJECT).isEmpty());
    }

    @Test
    void operatorCanSubmitButNotApprove() {
        assertTrue(Workflow.canPerform(Role.OPERATOR, Status.DRAFT, Action.SUBMIT));
        assertFalse(Workflow.canPerform(Role.OPERATOR, Status.SUBMITTED, Action.APPROVE));
        assertFalse(Workflow.canPerform(Role.OPERATOR, Status.APPROVED, Action.ISSUE));
    }

    @Test
    void approverCanApproveAndRejectButNotCreateOrIssue() {
        assertTrue(Workflow.canPerform(Role.APPROVER, Status.SUBMITTED, Action.APPROVE));
        assertTrue(Workflow.canPerform(Role.APPROVER, Status.SUBMITTED, Action.REJECT));
        assertFalse(Workflow.canPerform(Role.APPROVER, Status.APPROVED, Action.ISSUE));
        assertFalse(Workflow.canCreate(Role.APPROVER));
    }

    @Test
    void onlyAdminCanIssue() {
        assertTrue(Workflow.canPerform(Role.ADMIN, Status.APPROVED, Action.ISSUE));
        assertFalse(Workflow.roleMayPerform(Role.OPERATOR, Action.ISSUE));
        assertFalse(Workflow.roleMayPerform(Role.APPROVER, Action.ISSUE));
    }

    @Test
    void availableActionsDependOnRoleAndStatus() {
        assertEquals(List.of(Action.APPROVE, Action.REJECT),
                Workflow.availableActions(Role.APPROVER, Status.SUBMITTED));
        assertEquals(List.of(Action.SUBMIT), Workflow.availableActions(Role.OPERATOR, Status.DRAFT));
        assertTrue(Workflow.availableActions(Role.OPERATOR, Status.ISSUED).isEmpty());
    }

    @Test
    void editingIsLimitedToDraftsAndRejectedRecords() {
        assertTrue(Workflow.canEdit(Role.OPERATOR, Status.DRAFT));
        assertTrue(Workflow.canEdit(Role.OPERATOR, Status.REJECTED));
        assertFalse(Workflow.canEdit(Role.OPERATOR, Status.SUBMITTED));
        assertFalse(Workflow.canEdit(Role.OPERATOR, Status.ISSUED));
        assertFalse(Workflow.canEdit(Role.APPROVER, Status.DRAFT));
    }
}
