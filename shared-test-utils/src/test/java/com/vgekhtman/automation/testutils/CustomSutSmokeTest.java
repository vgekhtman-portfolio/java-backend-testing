package com.vgekhtman.automation.testutils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vgekhtman.automation.inventory.grpc.CheckStockResponse;
import com.vgekhtman.automation.testutils.data.InventoryRequests;
import com.vgekhtman.automation.testutils.grpc.InventoryClient;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
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
@Epic("Test Infrastructure")
@Feature("SUT orchestration")
@Story("Smoke checks")
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

    @DisplayName("TC-INFRA-002: orders-service responds on its REST endpoint")
    @Description("Calls GET /orders on the endpoint the harness resolved for the running stack.")
    @Test
    void ordersServiceRespondsOnRest() throws Exception {
        HttpResponse<String> response = get(CustomSut.restBaseUrl() + "/orders");
        assertThat(response.statusCode()).isEqualTo(200);
    }

    @DisplayName("TC-INFRA-003: orders-service responds on its gRPC endpoint")
    @Description("Calls CheckStock through the shared gRPC client against the resolved gRPC endpoint.")
    @Test
    void ordersServiceRespondsOnGrpc() {
        CheckStockResponse response = inventory.checkStock(
                InventoryRequests.checkStock().withProductId("prod-abc").build());
        assertThat(response.getAvailableQuantity()).isEqualTo(100);
    }

    @DisplayName("TC-INFRA-004: the WireMock admin API is reachable")
    @Description("Lists WireMock's stub mappings through the admin API resolved by the harness.")
    @Test
    void wireMockAdminIsReachable() throws Exception {
        HttpResponse<String> response = get(CustomSut.wiremockAdminBaseUrl() + "/mappings");
        assertThat(response.statusCode()).isEqualTo(200);
    }

    @DisplayName("TC-INFRA-005: a payment stub can be registered and then reset to the default")
    @Description("Counts WireMock mappings before and after registering an override and after resetting it.")
    @Test
    void stubCanBeRegisteredThenReset() throws Exception {
        assertThat(mappingsCount()).isEqualTo(1); // just the shipped charge-success.json

        CustomSut.paymentStubs().stubPaymentFailure(503);
        assertThat(mappingsCount()).isEqualTo(2); // shipped default + the override just added

        CustomSut.paymentStubs().reset();
        assertThat(mappingsCount()).isEqualTo(1); // back to just the shipped default
    }

    @DisplayName("TC-INFRA-006: Postgres credentials are derived from the running container")
    @Description("The harness reads the database user, password and name from the container's own environment instead of duplicating them.")
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
