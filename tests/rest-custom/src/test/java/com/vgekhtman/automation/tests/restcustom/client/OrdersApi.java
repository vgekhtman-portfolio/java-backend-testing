package com.vgekhtman.automation.tests.restcustom.client;

import com.vgekhtman.automation.testlogging.RestAssuredLogging;
import com.vgekhtman.automation.testutils.CustomSut;
import com.vgekhtman.automation.testutils.data.OrderRequest;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

/**
 * Thin wrapper around the custom SUT's Orders REST API, resolved via {@link CustomSut} on every call.
 * Every call logs its method, URI and response status, and attaches the request/response pair to the Allure report.
 */
public final class OrdersApi {

    static {
        // .log() output goes through Logback instead of stdout.
        RestAssured.config = RestAssuredLogging.config();
    }

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

    public static Response getOrder(String id) {
        return given()
                .filter(new AllureRestAssured())
                .log().method()
                .log().uri()
                .baseUri(CustomSut.restBaseUrl())
                .when()
                .get("/orders/{id}", id)
                .then()
                .log().status()
                .extract()
                .response();
    }
}
