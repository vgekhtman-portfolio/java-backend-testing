package com.vgekhtman.automation.testsupport;

import io.qameta.allure.Allure;
import io.qameta.allure.AttachmentOptions;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * Attaches text evidence (requests, responses, SQL, message logs) to the current Allure step, or to the test when
 * no step is open. The attachment sits directly on that step, so the report shows it inline when the step is
 * expanded, instead of nesting it in an extra step of its own as {@code Allure.attachment} does.
 */
public final class AllureEvidence {

    private AllureEvidence() {
    }

    public static void text(String name, String content) {
        Allure.getLifecycle().addAttachment(name, "text/plain",
                new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
                AttachmentOptions.withFileExtension("txt"));
    }
}
