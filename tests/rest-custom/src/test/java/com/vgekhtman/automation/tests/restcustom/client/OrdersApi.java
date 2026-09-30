package com.vgekhtman.automation.tests.restcustom.client;

import com.vgekhtman.automation.testutils.CustomSut;
import com.vgekhtman.automation.testutils.data.OrderRequest;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

/** Thin wrapper around the custom SUT's Orders REST API, resolved via {@link CustomSut} on every call. */
public final class OrdersApi {

    private OrdersApi() {
    }

    public static Response createOrder(OrderRequest request) {
        return given().baseUri(CustomSut.restBaseUrl()).contentType(JSON).body(request)
                .when().post("/orders");
    }

    public static Response getOrder(String id) {
        return given().baseUri(CustomSut.restBaseUrl())
                .when().get("/orders/{id}", id);
    }
}
