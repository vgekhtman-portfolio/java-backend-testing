package com.vgekhtman.automation.testutils;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Brings up the custom SUT's docker-compose stack once per test run and leaves it running until the JVM
 * exits. Extend test classes with {@code @ExtendWith(CustomSutExtension.class)}, then reach the running
 * stack through {@link CustomSut}'s static accessors.
 */
public final class CustomSutExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        CustomSut.ensureStarted();
    }
}
