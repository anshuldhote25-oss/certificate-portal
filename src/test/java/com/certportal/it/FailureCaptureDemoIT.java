package com.certportal.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.Test;

/**
 * Evidence helper for the "failure screenshot mechanism". It is skipped normally and fails on purpose when you run:
 * <pre>mvn verify -Dit.test=FailureCaptureDemoIT -Dforce.fail=true</pre>
 * Look in target/screenshots for the PNG and HTML that were captured.
 */
class FailureCaptureDemoIT extends BaseIT {

    @Test
    void deliberateFailureProducesAScreenshot() {
        assumeTrue(Boolean.getBoolean("force.fail"), "Run with -Dforce.fail=true to demonstrate screenshot capture");
        loginAs("operator");
        assertEquals("Wrong Name On Purpose", text("current-user"));
    }
}
