package com.vgekhtman.automation.tests.restcustom;

import com.vgekhtman.automation.tests.restcustom.client.OrdersApi;
import com.vgekhtman.automation.testutils.CustomSutExtension;
import com.vgekhtman.automation.testutils.data.OrderRequest;
import com.vgekhtman.automation.testutils.data.OrderRequests;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Epic("API Protocols")
@Feature("REST")
@Story("Order creation")
@Tag("custom-sut")
@ExtendWith(CustomSutExtension.class)
class OrderCreationTest {

    @DisplayName("TC-RESTCUSTOM-001: a valid order is CONFIRMED against the default payment stub")
    @Description("The happy path: POST /orders with valid input and the shipped payment stub in place.")
    @Test
    void validOrderIsConfirmedAgainstTheDefaultPaymentStub() {
        OrderRequest request = OrderRequests.anOrder().build();

        OrdersApi.createOrder(request).then()
                .statusCode(202)
                .body("id", notNullValue())
                .body("customerId", equalTo(request.customerId()))
                .body("productId", equalTo(request.productId()))
                .body("quantity", equalTo(request.quantity()))
                .body("status", equalTo("CONFIRMED"));
    }

    @DisplayName("TC-RESTCUSTOM-002: a blank customer id is rejected with 400")
    @Description("Validation: customerId must not be blank.")
    @Test
    void blankCustomerIdIsRejected() {
        OrderRequest request = OrderRequests.anOrder().withCustomerId("").build();

        OrdersApi.createOrder(request).then()
                .statusCode(400)
                .body("error", notNullValue());
    }

    @DisplayName("TC-RESTCUSTOM-003: a blank product id is rejected with 400")
    @Description("Validation: productId must not be blank.")
    @Test
    void blankProductIdIsRejected() {
        OrderRequest request = OrderRequests.anOrder().withProductId("").build();

        OrdersApi.createOrder(request).then()
                .statusCode(400)
                .body("error", notNullValue());
    }

    @DisplayName("TC-RESTCUSTOM-004: a zero quantity is rejected with 400")
    @Description("Validation: quantity must be positive.")
    @Test
    void zeroQuantityIsRejected() {
        OrderRequest request = OrderRequests.anOrder().withQuantity(0).build();

        OrdersApi.createOrder(request).then()
                .statusCode(400)
                .body("error", notNullValue());
    }

    @DisplayName("TC-RESTCUSTOM-005: a negative quantity is rejected with 400")
    @Description("Validation: quantity must be positive.")
    @Test
    void negativeQuantityIsRejected() {
        OrderRequest request = OrderRequests.anOrder().withQuantity(-1).build();

        OrdersApi.createOrder(request).then()
                .statusCode(400)
                .body("error", notNullValue());
    }
}
