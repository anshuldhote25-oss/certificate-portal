package com.certportal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ValidatorTest {

    private static final String TODAY = LocalDate.now().toString();

    @Test
    void validInputHasNoErrors() {
        assertTrue(Validator.validate("Priya Sharma", "priya@example.com", "Java", TODAY).isEmpty());
    }

    @Test
    void reportsEveryInvalidField() {
        Map<String, String> errors = Validator.validate("A", "not-an-email", " ", "31/12/2026");
        assertEquals(4, errors.size());
        assertTrue(errors.containsKey("recipientName"));
        assertTrue(errors.containsKey("recipientEmail"));
        assertTrue(errors.containsKey("course"));
        assertTrue(errors.containsKey("issueDate"));
    }

    @Test
    void futureDatesAreRejected() {
        String tomorrow = LocalDate.now().plusDays(1).toString();
        Map<String, String> errors = Validator.validate("Priya Sharma", "priya@example.com", "Java", tomorrow);
        assertEquals(1, errors.size());
        assertTrue(errors.get("issueDate").contains("future"));
    }

    @Test
    void nullInputsDoNotThrow() {
        assertEquals(4, Validator.validate(null, null, null, null).size());
    }

    @Test
    void parseDateReturnsNullForBadText() {
        assertNull(Validator.parseDate("tomorrow"));
        assertNull(Validator.parseDate(""));
        assertEquals(LocalDate.of(2026, 10, 12), Validator.parseDate(" 2026-10-12 "));
    }
}
