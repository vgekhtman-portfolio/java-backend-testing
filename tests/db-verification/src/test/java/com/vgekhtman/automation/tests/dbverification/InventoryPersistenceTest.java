package com.vgekhtman.automation.tests.dbverification;

import com.vgekhtman.automation.inventory.grpc.ReserveStockResponse;
import com.vgekhtman.automation.tests.dbverification.db.Database;
import com.vgekhtman.automation.testutils.CustomSutExtension;
import com.vgekhtman.automation.testutils.data.InventoryRequests;
import com.vgekhtman.automation.testutils.grpc.InventoryClient;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Protocols")
@Feature("Database")
@Story("Persisted state")
@Tag("custom-sut")
@ExtendWith(CustomSutExtension.class)
class InventoryPersistenceTest {

    private static InventoryClient client;

    @BeforeAll
    static void connect() {
        client = InventoryClient.connect();
    }

    @AfterAll
    static void disconnect() {
        client.close();
    }

    /**
     * Measured as a before/after difference at the data layer rather than against the seeded quantity
     * (prod-abc starts at 100 in sut/src/main/resources/db/migration/V1__init.sql), so it holds regardless
     * of what else has touched the row.
     */
    @DisplayName("TC-DB-002: reserving stock decrements the inventory row")
    @Description("Measures the inventory row before and after a ReserveStock call and checks the response's claim against the database.")
    @Test
    void reservingStockDecrementsTheInventoryRow() {
        int quantityBefore = Allure.step("Read the inventory row", () -> Database.inventoryQuantity("prod-abc"));

        ReserveStockResponse response = Allure.step("Reserve 2 units over gRPC", () -> {
            ReserveStockResponse reserved = client.reserveStock(
                    InventoryRequests.reserveStock().withProductId("prod-abc").withQuantity(2).build());
            assertThat(reserved.getSuccess()).isTrue();
            return reserved;
        });

        int quantityAfter = Allure.step("Read the inventory row again", () -> Database.inventoryQuantity("prod-abc"));
        assertThat(quantityAfter).isEqualTo(quantityBefore - 2);
        // The response's own claim is checked against the database, not the other way round.
        assertThat(response.getRemainingQuantity()).isEqualTo(quantityAfter);
    }
}
