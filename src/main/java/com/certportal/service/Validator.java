package com.certportal.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/** Server-side validation of the certificate form. */
public final class Validator {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private Validator() {
    }

    /** Returns field name -> error message. An empty map means the input is valid. */
    public static Map<String, String> validate(String name, String email, String course, String issueDate) {
        Map<String, String> errors = new LinkedHashMap<>();

        String n = name == null ? "" : name.trim();
        if (n.length() < 2 || n.length() > 100) {
            errors.put("recipientName", "Recipient name must be 2 to 100 characters.");
        }

        String e = email == null ? "" : email.trim();
        if (e.length() > 120 || !EMAIL.matcher(e).matches()) {
            errors.put("recipientEmail", "Enter a valid email address.");
        }

        String c = course == null ? "" : course.trim();
        if (c.isEmpty() || c.length() > 120) {
            errors.put("course", "Course is required and must be at most 120 characters.");
        }

        LocalDate date = parseDate(issueDate);
        if (date == null) {
            errors.put("issueDate", "Enter the issue date as YYYY-MM-DD.");
        } else if (date.isAfter(LocalDate.now())) {
            errors.put("issueDate", "Issue date cannot be in the future.");
        }
        return errors;
    }

    /** Parses an ISO date, returning null when the text is blank or malformed. */
    public static LocalDate parseDate(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}
