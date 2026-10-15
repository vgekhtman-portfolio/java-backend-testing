package com.vgekhtman.automation.tests.grpccustom;

import com.vgekhtman.automation.inventory.grpc.CheckStockResponse;
import com.vgekhtman.automation.testutils.CustomSutExtension;
import com.vgekhtman.automation.testutils.data.InventoryRequests;
import com.vgekhtman.automation.testutils.grpc.InventoryClient;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@Tag("custom-sut")
@ExtendWith(CustomSutExtension.class)
class StockCheckTest {

    private static InventoryClient client;

    @BeforeAll
    static void connect() {
        client = InventoryClient.connect();
    }

    @AfterAll
    static void disconnect() {
        client.close();
    }

    // prod-abc's quantity of 100 is seeded in sut/src/main/resources/db/migration/V1__init.sql.
    @Test
    void seededProductReportsItsStock() {
        CheckStockResponse response = client.checkStock(
                InventoryRequests.checkStock().withProductId("prod-abc").build());

        assertThat(response.getProductId()).isEqualTo("prod-abc");
        assertThat(response.getAvailableQuantity()).isEqualTo(100);
    }

    @Test
    void unknownProductFailsWithNotFound() {
        StatusRuntimeException error = catchThrowableOfType(StatusRuntimeException.class,
                () -> client.checkStock(InventoryRequests.checkStock().withProductId("no-such-product").build()));

        assertThat(error).isNotNull();
        assertThat(error.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
    }
}
