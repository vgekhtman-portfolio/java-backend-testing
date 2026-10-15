package com.vgekhtman.automation.tests.restpublic.client;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

/** Thin wrapper around the restful-booker endpoints this module exercises. */
public final class BookingApi {

    static {
        RestAssured.baseURI = env("REST_PUBLIC_BASE_URL", "https://restful-booker.herokuapp.com");
        // Every request/response pair is attached to the Allure report.
        RestAssured.filters(new AllureRestAssured());
    }

    private BookingApi() {
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    public static int createBooking(Booking booking) {
        return createBookingResponse(booking)
                .then()
                .statusCode(200)
                .extract()
                .path("bookingid");
    }

    public static Response createBookingResponse(Booking booking) {
        return given()
                .log().method()
                .log().uri()
                .contentType(JSON)
                .body(booking)
                .when()
                .post("/booking")
                .then()
                .log().status()
                .extract()
                .response();
    }

    public static Response getBooking(int id) {
        return given()
                .log().method()
                .log().uri()
                .when()
                .get("/booking/{id}", id)
                .then()
                .log().status()
                .extract()
                .response();
    }

    public static String authenticate() {
        return given()
                .log().method()
                .log().uri()
                .contentType(JSON)
                .body(Map.of("username", "admin", "password", "password123"))
                .when()
                .post("/auth")
                .then()
                .log().status()
                .statusCode(200)
                .extract()
                .path("token");
    }

    public static Response updateBooking(int id, String token, Booking booking) {
        return given()
                .log().method()
                .log().uri()
                .cookie("token", token)
                .contentType(JSON)
                .body(booking)
                .when()
                .put("/booking/{id}", id)
                .then()
                .log().status()
                .extract()
                .response();
    }

    public static Response deleteBooking(int id, String token) {
        return given()
                .log().method()
                .log().uri()
                .cookie("token", token)
                .when()
                .delete("/booking/{id}", id)
                .then()
                .log().status()
                .extract()
                .response();
    }
}
