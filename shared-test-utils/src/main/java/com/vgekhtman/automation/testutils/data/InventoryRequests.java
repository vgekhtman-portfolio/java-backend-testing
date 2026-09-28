package com.vgekhtman.automation.testutils.data;

import com.vgekhtman.automation.inventory.grpc.CheckStockRequest;
import com.vgekhtman.automation.inventory.grpc.ReserveStockRequest;

/** Fluent builders for the InventoryService gRPC requests, defaulting to the seeded fixture rows. */
public final class InventoryRequests {

    private InventoryRequests() {
    }

    /** Defaults to prod-abc, which has plenty of stock (100). */
    public static CheckStockRequestBuilder checkStock() {
        return new CheckStockRequestBuilder();
    }

    /** Defaults to prod-def, which has barely enough stock (5) - useful for exhausting it in a test. */
    public static ReserveStockRequestBuilder reserveStock() {
        return new ReserveStockRequestBuilder();
    }

    public static final class CheckStockRequestBuilder {
        private String productId = "prod-abc";

        public CheckStockRequestBuilder withProductId(String productId) {
            this.productId = productId;
            return this;
        }

        public CheckStockRequest build() {
            return CheckStockRequest.newBuilder().setProductId(productId).build();
        }
    }

    public static final class ReserveStockRequestBuilder {
        private String productId = "prod-def";
        private int quantity = 1;

        public ReserveStockRequestBuilder withProductId(String productId) {
            this.productId = productId;
            return this;
        }

        public ReserveStockRequestBuilder withQuantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public ReserveStockRequest build() {
            return ReserveStockRequest.newBuilder().setProductId(productId).setQuantity(quantity).build();
        }
    }
}
