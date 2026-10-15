package com.vgekhtman.automation.tests.dbverification.client;

import com.vgekhtman.automation.testutils.CustomSut;
import com.vgekhtman.automation.testutils.data.OrderRequest;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

/** Minimal REST client for the call whose persistence is being verified. */
public final class OrdersApi {

    private OrdersApi() {
    }

    public static Response createOrder(OrderRequest request) {
        return given()
                .filter(new AllureRestAssured())
                .log().method()
                .log().uri()
                .baseUri(CustomSut.restBaseUrl())
                .contentType(JSON)
                .body(request)
                .when()
                .post("/orders")
                .then()
                .log().status()
                .extract()
                .response();
    }
}
