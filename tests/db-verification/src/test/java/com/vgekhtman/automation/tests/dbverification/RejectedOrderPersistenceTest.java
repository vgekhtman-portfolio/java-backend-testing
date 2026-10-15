package com.vgekhtman.automation.tests.dbverification;

import com.vgekhtman.automation.tests.dbverification.client.OrdersApi;
import com.vgekhtman.automation.tests.dbverification.db.Database;
import com.vgekhtman.automation.testutils.CustomSutExtension;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the absence of a side effect: a rejected order must not leave a row behind. Each test tags its
 * requests with a unique product id and counts only rows for that id, so the result doesn't depend on
 * what other tests have inserted. A valid order with the same id is created afterwards as a control, to
 * show the count query can see a row that does exist - otherwise "0 rows" could simply be a broken check.
 */
@Epic("API Protocols")
@Feature("Database")
@Story("Absence of side effects")
@Tag("custom-sut")
@ExtendWith(CustomSutExtension.class)
class RejectedOrderPersistenceTest {

    @DisplayName("TC-DB-003: an order with a blank customer id is rejected and leaves no row")
    @Description("Proves the absence of a side effect, with a control order showing the count query can see rows.")
    @Test
    void orderWithBlankCustomerIdIsRejectedAndNotPersisted() {
        String productId = "absent-" + UUID.randomUUID();

        OrdersApi.createOrder(OrderRequests.anOrder().withProductId(productId).withCustomerId("").build()).then()
                .statusCode(400);

        assertThat(Database.countOrdersForProduct(productId)).isZero();
        assertControlOrderIsVisible(productId);
    }

    @DisplayName("TC-DB-004: an order with a non-positive quantity is rejected and leaves no row")
    @Description("Same absence check for the quantity validation rule.")
    @Test
    void orderWithNonPositiveQuantityIsRejectedAndNotPersisted() {
        String productId = "absent-" + UUID.randomUUID();

        OrdersApi.createOrder(OrderRequests.anOrder().withProductId(productId).withQuantity(0).build()).then()
                .statusCode(400);

        assertThat(Database.countOrdersForProduct(productId)).isZero();
        assertControlOrderIsVisible(productId);
    }

    private static void assertControlOrderIsVisible(String productId) {
        OrdersApi.createOrder(OrderRequests.anOrder().withProductId(productId).build()).then()
                .statusCode(201);

        assertThat(Database.countOrdersForProduct(productId)).isEqualTo(1);
    }
}
