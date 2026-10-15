package com.vgekhtman.automation.tests.grpccustom;

import com.vgekhtman.automation.inventory.grpc.CheckStockResponse;
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
@Story("Stock check")
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
    @DisplayName("TC-GRPC-001: CheckStock reports the stock of a seeded product")
    @Description("CheckStock for prod-abc returns its seeded quantity.")
    @Test
    void seededProductReportsItsStock() {
        CheckStockResponse response = client.checkStock(
                InventoryRequests.checkStock().withProductId("prod-abc").build());

        assertThat(response.getProductId()).isEqualTo("prod-abc");
        assertThat(response.getAvailableQuantity()).isEqualTo(100);
    }

    @DisplayName("TC-GRPC-002: CheckStock for an unknown product fails with NOT_FOUND")
    @Description("Asserts the gRPC status code itself, not just that the call failed.")
    @Test
    void unknownProductFailsWithNotFound() {
        StatusRuntimeException error = catchThrowableOfType(StatusRuntimeException.class,
                () -> client.checkStock(InventoryRequests.checkStock().withProductId("no-such-product").build()));

        assertThat(error).isNotNull();
        assertThat(error.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
    }
}
