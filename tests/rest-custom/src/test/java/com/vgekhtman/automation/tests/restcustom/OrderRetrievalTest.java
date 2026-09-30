package com.vgekhtman.automation.tests.restcustom;

import com.vgekhtman.automation.tests.restcustom.client.OrdersApi;
import com.vgekhtman.automation.testutils.CustomSutExtension;
import com.vgekhtman.automation.testutils.data.OrderRequest;
import com.vgekhtman.automation.testutils.data.OrderRequests;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;

@Tag("custom-sut")
@ExtendWith(CustomSutExtension.class)
class OrderRetrievalTest {

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

    @Test
    void randomUuidReturnsNotFound() {
        OrdersApi.getOrder(UUID.randomUUID().toString()).then()
                .statusCode(404)
                .body("error", equalTo("order not found"));
    }
}
