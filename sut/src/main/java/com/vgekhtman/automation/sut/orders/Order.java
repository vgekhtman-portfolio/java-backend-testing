package com.vgekhtman.automation.sut.orders;

import java.time.Instant;
import java.util.UUID;

/** The order resource, serialized directly as the REST response body. */
public record Order(
        UUID id,
        String customerId,
        String productId,
        int quantity,
        OrderStatus status,
        Instant createdAt
) {
}
