package com.vgekhtman.automation.tests.contract.consumer;

import com.vgekhtman.automation.tests.contract.consumer.model.OrderRequest;
import com.vgekhtman.automation.tests.contract.consumer.model.OrderResponse;
import com.vgekhtman.automation.testsupport.AllureHttpFilter;
import io.restassured.response.Response;

import java.util.Optional;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

/** The consumer-side client whose expectations of the orders API are captured in the pact. */
public final class OrdersClient {

    private final String baseUrl;

    public OrdersClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public OrderResponse createOrder(OrderRequest orderRequest) {
        Response response = given().filter(new AllureHttpFilter()).baseUri(baseUrl)
                .contentType(JSON).body(orderRequest)
                .when().post("/orders");
        if (response.statusCode() != 201) {
            throw new IllegalStateException("unexpected status " + response.statusCode());
        }
        return response.as(OrderResponse.class);
    }

    /** @return the order, or empty if the API answers 404. */
    public Optional<OrderResponse> findOrder(String id) {
        Response response = given().filter(new AllureHttpFilter()).baseUri(baseUrl)
                .when().get("/orders/{id}", id);
        return switch (response.statusCode()) {
            case 200 -> Optional.of(response.as(OrderResponse.class));
            case 404 -> Optional.empty();
            default -> throw new IllegalStateException("unexpected status " + response.statusCode());
        };
    }
}
