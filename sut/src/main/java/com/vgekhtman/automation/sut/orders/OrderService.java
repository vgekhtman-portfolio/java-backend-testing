package com.vgekhtman.automation.sut.orders;

import com.vgekhtman.automation.sut.payment.PaymentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository repository;
    private final PaymentClient paymentClient;
    private final int unitPrice;

    public OrderService(OrderRepository repository, PaymentClient paymentClient, int unitPrice) {
        this.repository = repository;
        this.paymentClient = paymentClient;
        this.unitPrice = unitPrice;
    }

    public Order create(CreateOrderRequest request) {
        validate(request);

        Order pending = new Order(
                UUID.randomUUID(),
                request.customerId().trim(),
                request.productId().trim(),
                request.quantity(),
                OrderStatus.PENDING,
                Instant.now());
        repository.insert(pending);
        log.info("order {} created in status PENDING", pending.id());

        int amount = request.quantity() * unitPrice;
        boolean approved = paymentClient.charge(pending.id().toString(), amount);
        OrderStatus finalStatus = approved ? OrderStatus.CONFIRMED : OrderStatus.PAYMENT_FAILED;
        repository.updateStatus(pending.id(), finalStatus);
        log.info("order {} resolved to {}", pending.id(), finalStatus);

        return repository.findById(pending.id()).orElseThrow();
    }

    private static void validate(CreateOrderRequest request) {
        if (request == null) {
            throw new ValidationException("request body is required");
        }
        if (request.customerId() == null || request.customerId().isBlank()) {
            throw new ValidationException("customerId must not be blank");
        }
        if (request.productId() == null || request.productId().isBlank()) {
            throw new ValidationException("productId must not be blank");
        }
        if (request.quantity() == null) {
            throw new ValidationException("quantity is required");
        }
        if (request.quantity() <= 0) {
            throw new ValidationException("quantity must be positive");
        }
    }
}
