package com.vgekhtman.automation.tests.contract.consumer.model;

/**
 * What the hypothetical consumer sends to create an order. Deliberately its own type rather than the
 * {@code OrderRequest} in shared-test-utils: a Pact consumer defines its own model of the API instead of
 * borrowing types from the provider's test tooling.
 */
public record OrderRequest(String customerId, String productId, int quantity) {
}
