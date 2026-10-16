package com.vgekhtman.automation.testutils;

import com.vgekhtman.automation.testutils.wiremock.PaymentStubs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Singleton holder for the custom SUT's docker-compose stack (postgres + wiremock + orders-service).
 * Started once per JVM - the classic Testcontainers "singleton container" pattern - and torn down by a
 * JVM shutdown hook, never per test class, so every test class of a module shares one running stack
 * (each module's test task is its own JVM). Use {@link CustomSutExtension} to trigger the start; use the
 * static accessors here to reach it.
 */
public final class CustomSut {

    private static final Logger log = LoggerFactory.getLogger(CustomSut.class);

    private static final String ORDERS_SERVICE = "orders-service";
    private static final String POSTGRES = "postgres";
    private static final String WIREMOCK = "wiremock";
    private static final int REST_PORT = 8080;
    private static final int GRPC_PORT = 9090;
    private static final int POSTGRES_PORT = 5432;
    private static final int WIREMOCK_PORT = 8080;

    private static final AtomicInteger startCount = new AtomicInteger(0);

    private static volatile ComposeContainer compose;
    private static volatile PaymentStubs paymentStubs;
    private static volatile RuntimeException startupFailure;

    private CustomSut() {
    }

    public static synchronized void ensureStarted() {
        if (compose != null) {
            return;
        }
        // A first attempt that fails partway (e.g. a slow cold build exceeding a wait timeout) must not
        // let every subsequent test class each retry the whole bring-up independently - that produces
        // confusing, unrelated-looking failures instead of the one real cause. Fail fast instead.
        if (startupFailure != null) {
            throw new IllegalStateException("custom SUT failed to start on an earlier attempt", startupFailure);
        }

        log.info("starting custom SUT compose stack (postgres + wiremock + orders-service)");

        ComposeContainer environment = new ComposeContainer(
                new File("docker-compose.yml"), new File("docker-compose.test.yml"))
                // Use the host's own `docker compose` CLI instead of Testcontainers' containerized
                // compose runner - avoids Docker-outside-of-Docker entirely.
                .withLocalCompose(true)
                .withExposedService(POSTGRES, POSTGRES_PORT,
                        Wait.forHealthcheck().withStartupTimeout(Duration.ofMinutes(2)))
                .withExposedService(WIREMOCK, WIREMOCK_PORT,
                        Wait.forHealthcheck().withStartupTimeout(Duration.ofMinutes(2)))
                .withExposedService(ORDERS_SERVICE, REST_PORT,
                        // Generous: this is the service with a Dockerfile build behind it, and a cold
                        // Gradle dependency resolution inside that build can take several minutes.
                        // forPort is required: the service also exposes gRPC, and without it the probe may
                        // pick that port and send HTTP/1.1 to it.
                        Wait.forHttp("/orders").forPort(REST_PORT).forStatusCode(200)
                                .withStartupTimeout(Duration.ofMinutes(5)))
                .withExposedService(ORDERS_SERVICE, GRPC_PORT);

        try {
            environment.start();
        } catch (RuntimeException e) {
            startupFailure = e;
            throw e;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(environment::stop, "custom-sut-shutdown"));

        compose = environment;
        paymentStubs = new PaymentStubs(wiremockAdminHost(), wiremockAdminPort());
        startCount.incrementAndGet();

        log.info("custom SUT is up: REST {} | gRPC {}:{} | wiremock admin {}",
                restBaseUrl(), grpcHost(), grpcPort(), wiremockAdminBaseUrl());
    }

    public static String restBaseUrl() {
        return "http://" + compose.getServiceHost(ORDERS_SERVICE, REST_PORT) + ":"
                + compose.getServicePort(ORDERS_SERVICE, REST_PORT);
    }

    public static String grpcHost() {
        return compose.getServiceHost(ORDERS_SERVICE, GRPC_PORT);
    }

    public static int grpcPort() {
        return compose.getServicePort(ORDERS_SERVICE, GRPC_PORT);
    }

    public static String jdbcUrl() {
        return "jdbc:postgresql://" + compose.getServiceHost(POSTGRES, POSTGRES_PORT) + ":"
                + compose.getServicePort(POSTGRES, POSTGRES_PORT) + "/" + postgresEnv().get("POSTGRES_DB");
    }

    public static String dbUser() {
        return postgresEnv().get("POSTGRES_USER");
    }

    public static String dbPassword() {
        return postgresEnv().get("POSTGRES_PASSWORD");
    }

    public static String wiremockAdminBaseUrl() {
        return "http://" + wiremockAdminHost() + ":" + wiremockAdminPort() + "/__admin";
    }

    public static PaymentStubs paymentStubs() {
        return paymentStubs;
    }

    /** How many times the compose stack has actually been started in this JVM. Should never exceed 1. */
    public static int startCount() {
        return startCount.get();
    }

    public static String ordersServiceContainerId() {
        return compose.getContainerByServiceName(ORDERS_SERVICE)
                .orElseThrow(() -> new IllegalStateException("orders-service container not found"))
                .getContainerId();
    }

    private static String wiremockAdminHost() {
        return compose.getServiceHost(WIREMOCK, WIREMOCK_PORT);
    }

    private static int wiremockAdminPort() {
        return compose.getServicePort(WIREMOCK, WIREMOCK_PORT);
    }

    /**
     * Reads the postgres container's actual environment variables (POSTGRES_DB/_USER/_PASSWORD) rather
     * than duplicating docker-compose.yml's values as literals here - so this can never drift from what
     * the stack is actually configured with.
     */
    private static Map<String, String> postgresEnv() {
        String[] entries = compose.getContainerByServiceName(POSTGRES)
                .orElseThrow(() -> new IllegalStateException("postgres container not found"))
                .getContainerInfo()
                .getConfig()
                .getEnv();
        Map<String, String> env = new HashMap<>();
        for (String entry : entries) {
            int separator = entry.indexOf('=');
            if (separator > 0) {
                env.put(entry.substring(0, separator), entry.substring(separator + 1));
            }
        }
        return env;
    }
}
