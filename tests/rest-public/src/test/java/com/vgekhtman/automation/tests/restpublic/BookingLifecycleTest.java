package com.vgekhtman.automation.tests.restpublic;

import com.vgekhtman.automation.tests.restpublic.client.Booking;
import com.vgekhtman.automation.tests.restpublic.client.BookingApi;
import io.restassured.response.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Full CRUD flow against a booking this test creates itself, including the extract-token-then-reuse-it
 * pattern for the two endpoints that require auth (PUT/DELETE).
 */
@Tag("public-api")
class BookingLifecycleTest {

    @Test
    void createReadUpdateDeleteBooking() {
        Booking original = Booking.aBooking();

        int bookingId = BookingApi.createBooking(original);

        BookingApi.getBooking(bookingId).then()
                .statusCode(200)
                .contentType(JSON)
                .body("firstname", equalTo(original.firstname()))
                .body("lastname", equalTo(original.lastname()))
                .body("totalprice", equalTo(original.totalprice()))
                .body("bookingdates.checkin", equalTo(original.bookingdates().checkin()));

        String token = BookingApi.authenticate();
        assertThat(token).isNotBlank();

        Booking updated = original.withFirstname("James");
        BookingApi.updateBooking(bookingId, token, updated).then()
                .statusCode(200)
                .body("firstname", equalTo("James"))
                .body("lastname", equalTo(original.lastname()));

        Response deleteResponse = BookingApi.deleteBooking(bookingId, token);
        deleteResponse.then().statusCode(201); // restful-booker's own documented (if unusual) success code

        BookingApi.getBooking(bookingId).then().statusCode(404);
    }

    @Test
    void authTokenIsNonEmptyAndUsableAcrossMultipleAuthenticatedCalls() {
        String token = BookingApi.authenticate();
        assertThat(token).matches("[a-zA-Z0-9]+");

        int firstBookingId = BookingApi.createBooking(Booking.aBooking());
        int secondBookingId = BookingApi.createBooking(Booking.aBooking().withFirstname("Alex"));

        // The same token authenticates two independent write calls - not just a single one-off use.
        BookingApi.updateBooking(firstBookingId, token, Booking.aBooking().withFirstname("Updated1"))
                .then().statusCode(200);
        BookingApi.updateBooking(secondBookingId, token, Booking.aBooking().withFirstname("Updated2"))
                .then().statusCode(200);

        BookingApi.deleteBooking(firstBookingId, token).then().statusCode(201);
        BookingApi.deleteBooking(secondBookingId, token).then().statusCode(201);
    }
}
