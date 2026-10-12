package com.certportal.model;

/** Life cycle of a certificate record. */
public enum Status {
    DRAFT("Draft", "grey"),
    SUBMITTED("Submitted", "blue"),
    APPROVED("Approved", "green"),
    REJECTED("Rejected", "red"),
    ISSUED("Issued", "purple");

    private final String label;
    private final String css;

    Status(String label, String css) {
        this.label = label;
        this.css = css;
    }

    public String getLabel() {
        return label;
    }

    public String getCss() {
        return css;
    }

    /** Returns the matching status or null for blank/unknown text. */
    public static Status fromText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Status.valueOf(text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
