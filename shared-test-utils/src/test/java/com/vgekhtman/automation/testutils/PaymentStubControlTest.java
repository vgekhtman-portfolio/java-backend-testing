package com.vgekhtman.automation.testutils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vgekhtman.automation.testutils.data.OrderRequests;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves stubPaymentFailure/stubPaymentTimeout aren't just admin-API bookkeeping: they demonstrably
 * change the outcome of a real POST /orders call against the running SUT.
 *
 * <p>Tagged {@code custom-sut} (blocks CI same as the real custom-SUT suites) and {@code framework}
 * (a harness self-test, not a demonstrated protocol/scenario test).
 */
@Epic("Test Infrastructure")
@Feature("WireMock control")
@Story("Payment stub overrides")
@Tag("custom-sut")
@Tag("framework")
@ExtendWith(CustomSutExtension.class)
class PaymentStubControlTest {

    private static final HttpClient http = HttpClient.newHttpClient();
    private static final ObjectMapper json = new ObjectMapper();

    @AfterEach
    void resetPaymentStub() {
        CustomSut.paymentStubs().reset();
    }

    @DisplayName("TC-INFRA-007: with the default stub an order is CONFIRMED")
    @Description("Baseline for the override tests: the shipped payment stub approves the charge.")
    @Test
    void defaultStubConfirmsTheOrder() throws Exception {
        assertThat(orderStatus(postOrder())).isEqualTo("CONFIRMED");
    }

    @DisplayName("TC-INFRA-008: a failure stub turns the order outcome into PAYMENT_FAILED")
    @Description("Shows the stub override changes the behaviour of a real POST /orders call, not just WireMock's bookkeeping.")
    @Test
    void stubPaymentFailureChangesTheOrderOutcome() throws Exception {
        CustomSut.paymentStubs().stubPaymentFailure(500);
        assertThat(orderStatus(postOrder())).isEqualTo("PAYMENT_FAILED");
    }

    @DisplayName("TC-INFRA-009: a timeout stub turns the order outcome into PAYMENT_FAILED")
    @Description("A payment call that hangs past the service's timeout is reported as PAYMENT_FAILED.")
    @Test
    void stubPaymentTimeoutChangesTheOrderOutcome() throws Exception {
        CustomSut.paymentStubs().stubPaymentTimeout();
        assertThat(orderStatus(postOrder())).isEqualTo("PAYMENT_FAILED");
    }

    private static HttpResponse<String> postOrder() throws Exception {
        String body = json.writeValueAsString(OrderRequests.anOrder().build());
        HttpRequest request = HttpRequest.newBuilder(URI.create(CustomSut.restBaseUrl() + "/orders"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String orderStatus(HttpResponse<String> response) throws Exception {
        assertThat(response.statusCode()).isEqualTo(201);
        return json.readTree(response.body()).get("status").asText();
    }
}
