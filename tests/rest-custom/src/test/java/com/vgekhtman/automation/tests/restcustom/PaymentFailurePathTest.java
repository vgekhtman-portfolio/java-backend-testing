package com.vgekhtman.automation.tests.restcustom;

import com.vgekhtman.automation.tests.restcustom.client.OrdersApi;
import com.vgekhtman.automation.testutils.CustomSut;
import com.vgekhtman.automation.testutils.CustomSutExtension;
import com.vgekhtman.automation.testutils.data.OrderRequest;
import com.vgekhtman.automation.testutils.data.OrderRequests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.Matchers.equalTo;

/**
 * The one place the WireMock skill is actually demonstrated: overriding the payment dependency's
 * behavior to reach a path that would be impossible to trigger against a real payment provider.
 */
@Tag("custom-sut")
@ExtendWith(CustomSutExtension.class)
class PaymentFailurePathTest {

    @AfterEach
    void resetPaymentStub() {
        CustomSut.paymentStubs().reset();
    }

    @Test
    void paymentFailureResultsInAPaymentFailedOrder() {
        CustomSut.paymentStubs().stubPaymentFailure(500);

        OrderRequest request = OrderRequests.anOrder().build();

        OrdersApi.createOrder(request).then()
                .statusCode(201) // a documented business outcome, not an API error
                .body("status", equalTo("PAYMENT_FAILED"));
    }

    @Test
    void paymentTimeoutResultsInAPaymentFailedOrder() {
        CustomSut.paymentStubs().stubPaymentTimeout();

        OrderRequest request = OrderRequests.anOrder().build();

        OrdersApi.createOrder(request).then()
                .statusCode(201)
                .body("status", equalTo("PAYMENT_FAILED"));
    }
}
