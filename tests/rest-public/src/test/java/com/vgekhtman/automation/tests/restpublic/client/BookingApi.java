package com.vgekhtman.automation.tests.restpublic.client;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

/** Thin wrapper around the restful-booker endpoints this module exercises. */
public final class BookingApi {

    static {
        RestAssured.baseURI = env("REST_PUBLIC_BASE_URL", "https://restful-booker.herokuapp.com");
    }

    private BookingApi() {
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    public static int createBooking(Booking booking) {
        return createBookingResponse(booking).then().statusCode(200)
                .extract().path("bookingid");
    }

    public static Response createBookingResponse(Booking booking) {
        return given().contentType(JSON).body(booking)
                .when().post("/booking");
    }

    public static Response getBooking(int id) {
        return given()
                .when().get("/booking/{id}", id);
    }

    public static String authenticate() {
        return given().contentType(JSON).body(Map.of("username", "admin", "password", "password123"))
                .when().post("/auth")
                .then().statusCode(200)
                .extract().path("token");
    }

    public static Response updateBooking(int id, String token, Booking booking) {
        return given().cookie("token", token).contentType(JSON).body(booking)
                .when().put("/booking/{id}", id);
    }

    public static Response deleteBooking(int id, String token) {
        return given().cookie("token", token)
                .when().delete("/booking/{id}", id);
    }
}
