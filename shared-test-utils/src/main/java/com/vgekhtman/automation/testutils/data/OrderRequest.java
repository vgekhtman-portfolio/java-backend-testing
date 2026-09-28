package com.vgekhtman.automation.testutils.data;

/** Mirrors the JSON body accepted by {@code POST /orders}. */
public record OrderRequest(String customerId, String productId, int quantity) {
}
