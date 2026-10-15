package com.vgekhtman.automation.tests.dbverification;

import com.vgekhtman.automation.tests.dbverification.client.OrdersApi;
import com.vgekhtman.automation.tests.dbverification.db.Database.OrderRow;
import com.vgekhtman.automation.tests.dbverification.db.Database;
import com.vgekhtman.automation.testutils.CustomSutExtension;
import com.vgekhtman.automation.testutils.data.OrderRequest;
import com.vgekhtman.automation.testutils.data.OrderRequests;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Protocols")
@Feature("Database")
@Story("Persisted state")
@Tag("custom-sut")
@ExtendWith(CustomSutExtension.class)
class OrderPersistenceTest {

    @DisplayName("TC-DB-001: a created order is persisted with the fields the API returned")
    @Description("Queries the orders table directly and compares the row with the API response instead of trusting the response alone.")
    @Test
    void createdOrderIsPersistedWithTheFieldsTheApiReturned() {
        // A unique customer id ties the assertion to this test's own row, whatever else is in the table.
        OrderRequest request = OrderRequests.anOrder().withCustomerId("db-" + UUID.randomUUID()).build();

        Response response = OrdersApi.createOrder(request);
        response.then()
                .statusCode(201);
        UUID id = UUID.fromString(response.path("id"));

        OrderRow row = Database.findOrder(id).orElseThrow(
                () -> new AssertionError("API returned order " + id + " but no such row exists"));

        assertThat(row.customerId()).isEqualTo(request.customerId());
        assertThat(row.productId()).isEqualTo(request.productId());
        assertThat(row.quantity()).isEqualTo(request.quantity());
        assertThat(row.status()).isEqualTo(response.path("status"));
        assertThat(row.createdAt().toInstant()).isEqualTo(Instant.parse(response.path("createdAt")));
    }
}
