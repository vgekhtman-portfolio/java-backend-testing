package com.vgekhtman.automation.tests.dbverification;

import com.vgekhtman.automation.inventory.grpc.ReserveStockResponse;
import com.vgekhtman.automation.tests.dbverification.db.Database;
import com.vgekhtman.automation.testutils.CustomSutExtension;
import com.vgekhtman.automation.testutils.data.InventoryRequests;
import com.vgekhtman.automation.testutils.grpc.InventoryClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

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
    @Test
    void reservingStockDecrementsTheInventoryRow() {
        int quantityBefore = Database.inventoryQuantity("prod-abc");

        ReserveStockResponse response = client.reserveStock(
                InventoryRequests.reserveStock().withProductId("prod-abc").withQuantity(2).build());
        assertThat(response.getSuccess()).isTrue();

        int quantityAfter = Database.inventoryQuantity("prod-abc");
        assertThat(quantityAfter).isEqualTo(quantityBefore - 2);
        // The response's own claim is checked against the database, not the other way round.
        assertThat(response.getRemainingQuantity()).isEqualTo(quantityAfter);
    }
}
