package com.certportal.it;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

/** Saves a screenshot and the page HTML when a Selenium test fails, then lets the failure continue. */
public class ScreenshotOnFailure implements TestExecutionExceptionHandler {

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        Object instance = context.getTestInstance().orElse(null);
        if (instance instanceof BaseIT) {
            String name = context.getRequiredTestClass().getSimpleName() + "." + context.getRequiredTestMethod().getName();
            ((BaseIT) instance).captureFailure(name);
        }
        throw throwable;
    }
}
