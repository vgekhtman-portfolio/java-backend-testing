package com.vgekhtman.automation.testutils;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A second, independent test class also extending {@link CustomSutExtension}. If the singleton
 * container pattern in {@link CustomSut} works, this class's beforeAll must not start a second compose
 * stack - proven here by asserting the stack was started exactly once for the whole JVM, not by simply
 * letting tests pass.
 *
 * <p>Tagged {@code custom-sut} (blocks CI same as the real custom-SUT suites, since a broken harness
 * should block just as hard) and {@code framework} (an independent axis marking this as a harness
 * self-test rather than a demonstrated protocol/scenario test).
 */
@Tag("custom-sut")
@Tag("framework")
@ExtendWith(CustomSutExtension.class)
class ContainerReuseTest {

    private static final Logger log = LoggerFactory.getLogger(ContainerReuseTest.class);

    @Test
    void composeStackIsStartedExactlyOnceAndSharedAcrossTestClasses() {
        assertThat(CustomSut.startCount())
                .as("compose stack should be started once per JVM and reused across test classes")
                .isEqualTo(1);

        String containerId = CustomSut.ordersServiceContainerId();
        assertThat(containerId).isNotBlank();
        log.info("orders-service container id observed from a second test class (proves reuse, not a fresh start): {}",
                containerId);
    }
}
