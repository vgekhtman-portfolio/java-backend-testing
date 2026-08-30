package com.vgekhtman.automation.sut.orders;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** HTTP layer for the orders resource: one plain-Java handler method per route. */
public final class OrderApi {

    private final OrderService service;
    private final OrderRepository repository;
    private final ObjectMapper json;

    public OrderApi(OrderService service, OrderRepository repository, ObjectMapper json) {
        this.service = service;
        this.repository = repository;
        this.json = json;
    }

    public HttpHandler router() {
        return new BlockingHandler(Handlers.routing()
                .post("/orders", this::create)
                .get("/orders", this::list)
                .get("/orders/{id}", this::getById));
    }

    private void create(HttpServerExchange exchange) throws IOException {
        CreateOrderRequest request;
        try {
            request = json.readValue(exchange.getInputStream(), CreateOrderRequest.class);
        } catch (JacksonException e) {
            send(exchange, 400, Map.of("error", "malformed JSON body"));
            return;
        }
        try {
            Order order = service.create(request);
            send(exchange, 201, order);
        } catch (ValidationException e) {
            send(exchange, 400, Map.of("error", e.getMessage()));
        }
    }

    private void list(HttpServerExchange exchange) throws IOException {
        send(exchange, 200, repository.findAll());
    }

    private void getById(HttpServerExchange exchange) throws IOException {
        String rawId = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY).getParameters().get("id");
        Optional<Order> order;
        try {
            order = repository.findById(UUID.fromString(rawId));
        } catch (IllegalArgumentException e) {
            send(exchange, 404, Map.of("error", "order not found"));
            return;
        }
        if (order.isPresent()) {
            send(exchange, 200, order.get());
        } else {
            send(exchange, 404, Map.of("error", "order not found"));
        }
    }

    private void send(HttpServerExchange exchange, int status, Object body) throws IOException {
        exchange.setStatusCode(status);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(json.writeValueAsString(body));
    }
}
