package com.vgekhtman.automation.testutils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vgekhtman.automation.inventory.grpc.CheckStockResponse;
import com.vgekhtman.automation.testutils.data.InventoryRequests;
import com.vgekhtman.automation.testutils.grpc.InventoryClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke-tests shared-test-utils itself: the compose stack comes up, both SUT protocols respond, the
 * WireMock admin API is reachable, and a stub can be registered then reset.
 *
 * <p>Tagged {@code custom-sut} (blocks CI same as the real custom-SUT suites) and {@code framework}
 * (a harness self-test, not a demonstrated protocol/scenario test).
 */
@Tag("custom-sut")
@Tag("framework")
@ExtendWith(CustomSutExtension.class)
class CustomSutSmokeTest {

    private static final HttpClient http = HttpClient.newHttpClient();
    private static final ObjectMapper json = new ObjectMapper();
    private static InventoryClient inventory;

    @BeforeAll
    static void connectGrpc() {
        inventory = InventoryClient.connect();
    }

    @AfterAll
    static void disconnectGrpc() {
        inventory.close();
    }

    @AfterEach
    void resetPaymentStub() {
        CustomSut.paymentStubs().reset();
    }

    @Test
    void ordersServiceRespondsOnRest() throws Exception {
        HttpResponse<String> response = get(CustomSut.restBaseUrl() + "/orders");
        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    void ordersServiceRespondsOnGrpc() {
        CheckStockResponse response = inventory.checkStock(
                InventoryRequests.checkStock().withProductId("prod-abc").build());
        assertThat(response.getAvailableQuantity()).isEqualTo(100);
    }

    @Test
    void wireMockAdminIsReachable() throws Exception {
        HttpResponse<String> response = get(CustomSut.wiremockAdminBaseUrl() + "/mappings");
        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    void stubCanBeRegisteredThenReset() throws Exception {
        assertThat(mappingsCount()).isEqualTo(1); // just the shipped charge-success.json

        CustomSut.paymentStubs().stubPaymentFailure(503);
        assertThat(mappingsCount()).isEqualTo(2); // shipped default + the override just added

        CustomSut.paymentStubs().reset();
        assertThat(mappingsCount()).isEqualTo(1); // back to just the shipped default
    }

    @Test
    void postgresCredentialsAreDerivedFromTheRunningContainer() {
        // Values are read off the running postgres container's own env, not a literal duplicated in
        // CustomSut - asserting here against what docker-compose.yml actually declares for postgres.
        assertThat(CustomSut.dbUser()).isEqualTo("orders");
        assertThat(CustomSut.dbPassword()).isEqualTo("orders");
        assertThat(CustomSut.jdbcUrl()).matches("jdbc:postgresql://[^:/]+:\\d+/orders");
    }

    private static int mappingsCount() throws Exception {
        HttpResponse<String> response = get(CustomSut.wiremockAdminBaseUrl() + "/mappings");
        JsonNode body = json.readTree(response.body());
        return body.get("meta").get("total").asInt();
    }

    private static HttpResponse<String> get(String url) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
}
