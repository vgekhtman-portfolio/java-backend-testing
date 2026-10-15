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

import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;

@Epic("API Protocols")
@Feature("REST")
@Story("Order retrieval")
@Tag("custom-sut")
@ExtendWith(CustomSutExtension.class)
class OrderRetrievalTest {

    @DisplayName("TC-RESTCUSTOM-006: an existing order is returned by id")
    @Description("Creates an order, then reads it back through GET /orders/{id}.")
    @Test
    void existingOrderIsReturned() {
        OrderRequest request = OrderRequests.anOrder().build();
        String id = OrdersApi.createOrder(request).then()
                .statusCode(201)
                .extract().path("id");

        OrdersApi.getOrder(id).then()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("customerId", equalTo(request.customerId()))
                .body("productId", equalTo(request.productId()));
    }

    @DisplayName("TC-RESTCUSTOM-007: an unknown order id returns 404")
    @Description("GET /orders/{id} for a random UUID returns the documented not-found response.")
    @Test
    void randomUuidReturnsNotFound() {
        OrdersApi.getOrder(UUID.randomUUID().toString()).then()
                .statusCode(404)
                .body("error", equalTo("order not found"));
    }
}
