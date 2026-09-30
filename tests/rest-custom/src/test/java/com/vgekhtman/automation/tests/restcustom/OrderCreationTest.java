package com.vgekhtman.automation.tests.restcustom;

import com.vgekhtman.automation.tests.restcustom.client.OrdersApi;
import com.vgekhtman.automation.testutils.CustomSutExtension;
import com.vgekhtman.automation.testutils.data.OrderRequest;
import com.vgekhtman.automation.testutils.data.OrderRequests;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Tag("custom-sut")
@ExtendWith(CustomSutExtension.class)
class OrderCreationTest {

    @Test
    void validOrderIsConfirmedAgainstTheDefaultPaymentStub() {
        OrderRequest request = OrderRequests.anOrder().build();

        OrdersApi.createOrder(request).then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("customerId", equalTo(request.customerId()))
                .body("productId", equalTo(request.productId()))
                .body("quantity", equalTo(request.quantity()))
                .body("status", equalTo("CONFIRMED"));
    }

    @Test
    void blankCustomerIdIsRejected() {
        OrderRequest request = OrderRequests.anOrder().withCustomerId("").build();

        OrdersApi.createOrder(request).then()
                .statusCode(400)
                .body("error", notNullValue());
    }

    @Test
    void blankProductIdIsRejected() {
        OrderRequest request = OrderRequests.anOrder().withProductId("").build();

        OrdersApi.createOrder(request).then()
                .statusCode(400)
                .body("error", notNullValue());
    }

    @Test
    void zeroQuantityIsRejected() {
        OrderRequest request = OrderRequests.anOrder().withQuantity(0).build();

        OrdersApi.createOrder(request).then()
                .statusCode(400)
                .body("error", notNullValue());
    }

    @Test
    void negativeQuantityIsRejected() {
        OrderRequest request = OrderRequests.anOrder().withQuantity(-1).build();

        OrdersApi.createOrder(request).then()
                .statusCode(400)
                .body("error", notNullValue());
    }
}
