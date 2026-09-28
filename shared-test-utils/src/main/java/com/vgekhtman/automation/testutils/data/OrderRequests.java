package com.vgekhtman.automation.testutils.data;

/** Fluent builder for {@link OrderRequest}, defaulting to values that hit the seeded prod-abc inventory row. */
public final class OrderRequests {

    private OrderRequests() {
    }

    public static Builder anOrder() {
        return new Builder();
    }

    public static final class Builder {
        private String customerId = "cust-test";
        private String productId = "prod-abc";
        private int quantity = 1;

        public Builder withCustomerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder withProductId(String productId) {
            this.productId = productId;
            return this;
        }

        public Builder withQuantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public OrderRequest build() {
            return new OrderRequest(customerId, productId, quantity);
        }
    }
}
