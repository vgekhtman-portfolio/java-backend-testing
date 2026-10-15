package com.vgekhtman.automation.tests.grpccustom;

import com.vgekhtman.automation.inventory.grpc.ReserveStockResponse;
import com.vgekhtman.automation.testutils.CustomSutExtension;
import com.vgekhtman.automation.testutils.data.InventoryRequests;
import com.vgekhtman.automation.testutils.grpc.InventoryClient;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
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
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@Epic("API Protocols")
@Feature("gRPC")
@Story("Stock reservation")
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
    @DisplayName("TC-GRPC-003: ReserveStock decrements stock and refuses to go below zero")
    @Description("Insufficient stock is a business outcome in the response, not a gRPC error, and stock never goes negative.")
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

    @DisplayName("TC-GRPC-004: ReserveStock with zero quantity fails with INVALID_ARGUMENT")
    @Description("Asserts the INVALID_ARGUMENT status code for a zero quantity.")
    @Test
    void zeroQuantityFailsWithInvalidArgument() {
        StatusRuntimeException error = catchThrowableOfType(StatusRuntimeException.class,
                () -> client.reserveStock(InventoryRequests.reserveStock().withProductId("prod-abc").withQuantity(0).build()));

        assertThat(error).isNotNull();
        assertThat(error.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
    }

    @DisplayName("TC-GRPC-005: ReserveStock with a negative quantity fails with INVALID_ARGUMENT")
    @Description("Asserts the INVALID_ARGUMENT status code for a negative quantity.")
    @Test
    void negativeQuantityFailsWithInvalidArgument() {
        StatusRuntimeException error = catchThrowableOfType(StatusRuntimeException.class,
                () -> client.reserveStock(InventoryRequests.reserveStock().withProductId("prod-abc").withQuantity(-1).build()));

        assertThat(error).isNotNull();
        assertThat(error.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
    }
}
