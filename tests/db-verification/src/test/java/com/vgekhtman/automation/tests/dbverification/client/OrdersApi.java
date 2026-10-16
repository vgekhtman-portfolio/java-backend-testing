package com.vgekhtman.automation.tests.dbverification.client;

import com.vgekhtman.automation.testsupport.AllureHttpFilter;
import com.vgekhtman.automation.testsupport.RestAssuredLogging;
import com.vgekhtman.automation.testutils.CustomSut;
import com.vgekhtman.automation.testutils.data.OrderRequest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

/** Minimal REST client for the call whose persistence is being verified. */
public final class OrdersApi {

    static {
        // .log() output goes through Logback instead of stdout.
        RestAssured.config = RestAssuredLogging.config();
    }

    private OrdersApi() {
    }

    public static Response createOrder(OrderRequest request) {
        return given()
                .filter(new AllureHttpFilter())
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
