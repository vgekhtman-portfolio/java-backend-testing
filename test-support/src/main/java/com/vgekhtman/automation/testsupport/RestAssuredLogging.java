package com.vgekhtman.automation.testsupport;

import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Routes Rest-Assured's {@code .log()} output through SLF4J (INFO), so REST calls are logged in the same
 * format and at the same level as everything else instead of going to stdout.
 *
 * <p>Install it once per client with {@code RestAssured.config = RestAssuredLogging.config()}, or per
 * request with {@code given().config(RestAssuredLogging.config())}.
 */
public final class RestAssuredLogging {

    private static final Logger log = LoggerFactory.getLogger(RestAssuredLogging.class);

    private static final PrintStream STREAM =
            new PrintStream(new LineLogger(), true, StandardCharsets.UTF_8);

    private RestAssuredLogging() {
    }

    public static RestAssuredConfig config() {
        return RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(STREAM));
    }

    /** Collects written bytes and logs one INFO message per completed line. */
    private static final class LineLogger extends OutputStream {

        private final ByteArrayOutputStream line = new ByteArrayOutputStream();

        @Override
        public synchronized void write(int b) {
            if (b == '\n') {
                String text = line.toString(StandardCharsets.UTF_8).strip().replace('\t', ' ');
                line.reset();
                if (!text.isEmpty()) {
                    log.info(text);
                }
            } else {
                line.write(b);
            }
        }
    }
}
