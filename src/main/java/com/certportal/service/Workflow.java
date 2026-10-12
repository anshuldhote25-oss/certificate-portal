package com.certportal.service;

import com.certportal.model.Role;
import com.certportal.model.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Role-based status workflow (pure logic, no web or database code).
 *
 * <pre>
 * DRAFT/REJECTED --submit--> SUBMITTED --approve--> APPROVED --issue--> ISSUED
 *                               \--reject--> REJECTED
 * </pre>
 * Operators create/edit/submit, approvers approve/reject, administrators can do everything and issue.
 */
public final class Workflow {

    public enum Action {
        SUBMIT("submit", "Submit for approval"),
        APPROVE("approve", "Approve"),
        REJECT("reject", "Reject"),
        ISSUE("issue", "Issue certificate");

        private final String path;
        private final String label;

        Action(String path, String label) {
            this.path = path;
            this.label = label;
        }

        public String getPath() {
            return path;
        }

        public String getLabel() {
            return label;
        }

        public static Action fromPath(String path) {
            for (Action a : values()) {
                if (a.path.equals(path)) {
                    return a;
                }
            }
            return null;
        }
    }

    private Workflow() {
    }

    /** The status reached by performing the action, or empty when the transition is not allowed from that status. */
    public static Optional<Status> next(Status from, Action action) {
        switch (action) {
            case SUBMIT:
                return (from == Status.DRAFT || from == Status.REJECTED)
                        ? Optional.of(Status.SUBMITTED) : Optional.empty();
            case APPROVE:
                return from == Status.SUBMITTED ? Optional.of(Status.APPROVED) : Optional.empty();
            case REJECT:
                return from == Status.SUBMITTED ? Optional.of(Status.REJECTED) : Optional.empty();
            case ISSUE:
                return from == Status.APPROVED ? Optional.of(Status.ISSUED) : Optional.empty();
            default:
                return Optional.empty();
        }
    }

    public static boolean roleMayPerform(Role role, Action action) {
        switch (action) {
            case SUBMIT:
                return role == Role.OPERATOR || role == Role.ADMIN;
            case APPROVE:
            case REJECT:
                return role == Role.APPROVER || role == Role.ADMIN;
            case ISSUE:
                return role == Role.ADMIN;
            default:
                return false;
        }
    }

    public static boolean canPerform(Role role, Status from, Action action) {
        return roleMayPerform(role, action) && next(from, action).isPresent();
    }

    /** Actions the role can take right now on a record in the given status. */
    public static List<Action> availableActions(Role role, Status from) {
        List<Action> result = new ArrayList<>();
        for (Action a : Action.values()) {
            if (canPerform(role, from, a)) {
                result.add(a);
            }
        }
        return result;
    }

    public static boolean canCreate(Role role) {
        return role == Role.OPERATOR || role == Role.ADMIN;
    }

    /** Records can only be edited while they are drafts or after rejection. */
    public static boolean canEdit(Role role, Status status) {
        return canCreate(role) && (status == Status.DRAFT || status == Status.REJECTED);
    }
}
