package com.certportal.model;

/** Roles used for the role-based status workflow. */
public enum Role {
    OPERATOR("Operator"),
    APPROVER("Approver"),
    ADMIN("Administrator");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
