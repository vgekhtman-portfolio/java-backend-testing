package com.vgekhtman.automation.sut;

/**
 * Runtime configuration, sourced entirely from environment variables with local-development defaults.
 * The defaults let {@code Main} run against a locally started Postgres/WireMock without any env setup;
 * {@code docker-compose.yml} overrides all of them.
 */
public record Config(
        int httpPort,
        int grpcPort,
        String jdbcUrl,
        String dbUser,
        String dbPassword,
        String paymentServiceUrl,
        int unitPrice
) {
    public static Config fromEnv() {
        return new Config(
                intEnv("HTTP_PORT", 8080),
                intEnv("GRPC_PORT", 9090),
                env("DB_URL", "jdbc:postgresql://localhost:5432/orders"),
                env("DB_USER", "orders"),
                env("DB_PASSWORD", "orders"),
                env("PAYMENT_SERVICE_URL", "http://localhost:8081"),
                intEnv("UNIT_PRICE", 10)
        );
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    private static int intEnv(String key, int defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? defaultValue : Integer.parseInt(value.trim());
    }
}
