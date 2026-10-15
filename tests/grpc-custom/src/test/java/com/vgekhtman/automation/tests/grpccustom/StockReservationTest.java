package com.vgekhtman.automation.tests.grpccustom;

import com.vgekhtman.automation.inventory.grpc.ReserveStockResponse;
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
class StockReservationTest {

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
     * Reservations change shared state: prod-def is seeded with 5 units (see
     * sut/src/main/resources/db/migration/V1__init.sql), and this is the only test that touches it.
     * Reserving 3 leaves 2, so a second reserve of 3 is insufficient. The reserve-then-exhaust sequence
     * lives in one test so it doesn't depend on test order.
     */
    @Test
    void reservingStockDecrementsItAndRefusesToGoBelowZero() {
        ReserveStockResponse first = client.reserveStock(
                InventoryRequests.reserveStock().withProductId("prod-def").withQuantity(3).build());

        assertThat(first.getSuccess()).isTrue();
        assertThat(first.getRemainingQuantity()).isEqualTo(2);

        // Insufficient stock is a business outcome carried in the response, not a gRPC error.
        ReserveStockResponse second = client.reserveStock(
                InventoryRequests.reserveStock().withProductId("prod-def").withQuantity(3).build());

        assertThat(second.getSuccess()).isFalse();
        assertThat(second.getMessage()).isNotBlank();
        assertThat(second.getRemainingQuantity()).isEqualTo(2);

        int stillAvailable = client.checkStock(
                InventoryRequests.checkStock().withProductId("prod-def").build()).getAvailableQuantity();
        assertThat(stillAvailable).isEqualTo(2);
    }

    @Test
    void zeroQuantityFailsWithInvalidArgument() {
        StatusRuntimeException error = catchThrowableOfType(StatusRuntimeException.class,
                () -> client.reserveStock(InventoryRequests.reserveStock().withProductId("prod-abc").withQuantity(0).build()));

        assertThat(error).isNotNull();
        assertThat(error.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
    }

    @Test
    void negativeQuantityFailsWithInvalidArgument() {
        StatusRuntimeException error = catchThrowableOfType(StatusRuntimeException.class,
                () -> client.reserveStock(InventoryRequests.reserveStock().withProductId("prod-abc").withQuantity(-1).build()));

        assertThat(error).isNotNull();
        assertThat(error.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
    }
}
