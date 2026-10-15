package com.vgekhtman.automation.tests.restcustom;

import com.vgekhtman.automation.tests.restcustom.client.OrdersApi;
import com.vgekhtman.automation.testutils.CustomSut;
import com.vgekhtman.automation.testutils.CustomSutExtension;
import com.vgekhtman.automation.testutils.data.OrderRequest;
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

import static org.hamcrest.Matchers.equalTo;

/**
 * The one place the WireMock skill is actually demonstrated: overriding the payment dependency's
 * behavior to reach a path that would be impossible to trigger against a real payment provider.
 */
@Epic("API Protocols")
@Feature("REST")
@Story("Payment failure paths")
@Tag("custom-sut")
@ExtendWith(CustomSutExtension.class)
class PaymentFailurePathTest {

    @AfterEach
    void resetPaymentStub() {
        CustomSut.paymentStubs().reset();
    }

    @DisplayName("TC-RESTCUSTOM-008: a failing payment dependency results in a PAYMENT_FAILED order")
    @Description("WireMock makes the payment call fail on demand, a path that can't be provoked on a real payment provider; the API still answers 201.")
    @Test
    void paymentFailureResultsInAPaymentFailedOrder() {
        CustomSut.paymentStubs().stubPaymentFailure(500);

        OrderRequest request = OrderRequests.anOrder().build();

        OrdersApi.createOrder(request).then()
                .statusCode(201) // a documented business outcome, not an API error
                .body("status", equalTo("PAYMENT_FAILED"));
    }

    @DisplayName("TC-RESTCUSTOM-009: a payment dependency that times out results in a PAYMENT_FAILED order")
    @Description("WireMock delays the payment response past the service's timeout.")
    @Test
    void paymentTimeoutResultsInAPaymentFailedOrder() {
        CustomSut.paymentStubs().stubPaymentTimeout();

        OrderRequest request = OrderRequests.anOrder().build();

        OrdersApi.createOrder(request).then()
                .statusCode(201)
                .body("status", equalTo("PAYMENT_FAILED"));
    }
}
