package com.vgekhtman.automation.tests.contract.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactDirectory;
import com.vgekhtman.automation.tests.contract.PactFiles;
import com.vgekhtman.automation.tests.contract.consumer.model.OrderRequest;
import com.vgekhtman.automation.tests.contract.consumer.model.OrderResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Defines what the hypothetical "orders-client" expects from the orders-service ({@code POST /orders} and
 * {@code GET /orders/{id}}), exercises {@link OrdersClient} against Pact's mock server, and writes the
 * resulting pact file for the provider verification to replay.
 *
 * <p>Every field declared in a pact response is asserted on after the client has parsed it, so a field the
 * client fails to map would fail here, and no field is declared that the client doesn't read.
 */
@Epic("API Protocols")
@Feature("Contract (Pact)")
@Story("Consumer contract")
@Tag("custom-sut")
@Order(1)
@PactDirectory(PactFiles.DIR)
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "orders-service", pactVersion = PactSpecVersion.V3)
class OrdersConsumerPactTest {

    /** The state the provider is put into for the "existing order" interaction, and what is expected back. */
    static final OrderResponse EXISTING_ORDER = new OrderResponse(
            "3f2c8b52-6c1a-4d8e-9b7a-5e1d2c9a4f10", "cust-contract", "prod-abc", 3, "CONFIRMED",
            "2026-01-01T00:00:00Z");
    static final String MISSING_ORDER_ID = "9d1b7e44-0a3c-4f5e-8c2b-6a7f1e3d5b88";

    private static final String ISO_INSTANT = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z";
    private static final Map<String, String> JSON_CONTENT = Map.of("Content-Type", "application/json");

    @Pact(consumer = "orders-client")
    RequestResponsePact createOrder(PactDslWithProvider builder) {
        return builder
                .uponReceiving("a request to create an order")
                .method("POST")
                .path("/orders")
                .headers(JSON_CONTENT)
                .body(newJsonBody(o -> {
                    o.stringType("customerId", "cust-1");
                    o.stringType("productId", "prod-abc");
                    o.integerType("quantity", 2);
                }).build())
                .willRespondWith()
                .status(201)
                .headers(JSON_CONTENT)
                .body(newJsonBody(o -> {
                    o.uuid("id");
                    o.stringType("customerId", "cust-1");
                    o.stringType("productId", "prod-abc");
                    o.integerType("quantity", 2);
                    // The payment outcome is a business result, so any documented status is acceptable here.
                    o.stringMatcher("status", "PENDING|CONFIRMED|PAYMENT_FAILED", "CONFIRMED");
                    o.stringMatcher("createdAt", ISO_INSTANT, "2026-01-01T00:00:00Z");
                }).build())
                .toPact();
    }

    @Pact(consumer = "orders-client")
    RequestResponsePact getExistingOrder(PactDslWithProvider builder) {
        return builder
                .given("an order exists", Map.of(
                        "id", EXISTING_ORDER.id(),
                        "customerId", EXISTING_ORDER.customerId(),
                        "productId", EXISTING_ORDER.productId(),
                        "quantity", EXISTING_ORDER.quantity(),
                        "status", EXISTING_ORDER.status(),
                        "createdAt", EXISTING_ORDER.createdAt()))
                .uponReceiving("a request for an existing order")
                .method("GET")
                .path("/orders/" + EXISTING_ORDER.id())
                .willRespondWith()
                .status(200)
                .headers(JSON_CONTENT)
                .body(newJsonBody(o -> {
                    o.stringValue("id", EXISTING_ORDER.id());
                    o.stringValue("customerId", EXISTING_ORDER.customerId());
                    o.stringValue("productId", EXISTING_ORDER.productId());
                    o.numberValue("quantity", EXISTING_ORDER.quantity());
                    o.stringValue("status", EXISTING_ORDER.status());
                    o.stringValue("createdAt", EXISTING_ORDER.createdAt());
                }).build())
                .toPact();
    }

    @Pact(consumer = "orders-client")
    RequestResponsePact getMissingOrder(PactDslWithProvider builder) {
        return builder
                .given("no order exists", Map.of("id", MISSING_ORDER_ID))
                .uponReceiving("a request for an order that does not exist")
                .method("GET")
                .path("/orders/" + MISSING_ORDER_ID)
                .willRespondWith()
                .status(404)
                .headers(JSON_CONTENT)
                .body(newJsonBody(o -> o.stringValue("error", "order not found")).build())
                .toPact();
    }

    @DisplayName("TC-PACT-001: consumer: creating an order matches the declared contract")
    @Description("The consumer client sends the declared POST /orders request and parses every field of the declared response.")
    @Test
    @PactTestFor(pactMethod = "createOrder")
    void createsAnOrder(MockServer mockServer) {
        OrderResponse order = new OrdersClient(mockServer.getUrl()).createOrder(new OrderRequest("cust-1", "prod-abc", 2));

        // The id and timestamp are generated/matched rather than fixed, so they're checked for form.
        assertThat(UUID.fromString(order.id())).isNotNull();
        assertThat(order.customerId()).isEqualTo("cust-1");
        assertThat(order.productId()).isEqualTo("prod-abc");
        assertThat(order.quantity()).isEqualTo(2);
        assertThat(order.status()).isIn("PENDING", "CONFIRMED", "PAYMENT_FAILED");
        assertThat(Instant.parse(order.createdAt())).isNotNull();
    }

    @DisplayName("TC-PACT-002: consumer: reading an existing order matches the declared contract")
    @Description("The consumer client sends the declared GET /orders/{id} request and parses the declared response exactly.")
    @Test
    @PactTestFor(pactMethod = "getExistingOrder")
    void readsAnExistingOrder(MockServer mockServer) {
        assertThat(new OrdersClient(mockServer.getUrl()).findOrder(EXISTING_ORDER.id()))
                .contains(EXISTING_ORDER);
    }

    @DisplayName("TC-PACT-003: consumer: a missing order is treated as absent")
    @Description("The consumer client maps the declared 404 response to an empty result.")
    @Test
    @PactTestFor(pactMethod = "getMissingOrder")
    void treatsAMissingOrderAsAbsent(MockServer mockServer) {
        assertThat(new OrdersClient(mockServer.getUrl()).findOrder(MISSING_ORDER_ID)).isEmpty();
    }
}
