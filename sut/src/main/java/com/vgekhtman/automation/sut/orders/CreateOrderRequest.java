package com.vgekhtman.automation.sut.orders;

public record CreateOrderRequest(String customerId, String productId, Integer quantity) {
}
