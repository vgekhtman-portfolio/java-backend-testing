package com.vgekhtman.automation.tests.contract.consumer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The order fields the hypothetical consumer relies on - its view of the contract, not the provider's model.
 * Tolerant reader: fields the consumer doesn't use may appear without breaking it.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderResponse(String id, String customerId, String productId, int quantity, String status,
                        String createdAt) {
}
