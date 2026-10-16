package com.vgekhtman.automation.tests.restpublic;

import com.vgekhtman.automation.tests.restpublic.client.Booking;
import com.vgekhtman.automation.tests.restpublic.client.BookingApi;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Negative paths: a booking id that never existed, one this test deleted itself, and the two
 * auth-required endpoints called without a valid token.
 */
@Epic("API Protocols")
@Feature("REST")
@Story("Negative paths")
@Tag("public-api")
class BookingNegativeTest {

    @DisplayName("TC-RESTPUB-005: a booking id that never existed returns 404")
    @Description("GET for an id that was never created.")
    @Test
    void gettingAnIdThatNeverExistedReturnsNotFound() {
        BookingApi.getBooking(999_999_999).then().statusCode(418);
    }

    @DisplayName("TC-RESTPUB-006: a booking deleted by the test returns 404")
    @Description("Deletes a booking the test created, then reads it back.")
    @Test
    void gettingABookingThisTestJustDeletedReturnsNotFound() {
        int bookingId = BookingApi.createBooking(Booking.aBooking());
        String token = BookingApi.authenticate();

        BookingApi.deleteBooking(bookingId, token).then().statusCode(201);

        BookingApi.getBooking(bookingId).then().statusCode(404);
    }

    @DisplayName("TC-RESTPUB-007: updating without a valid token is forbidden")
    @Description("PUT with an invalid token returns 403.")
    @Test
    void updatingWithoutAValidTokenIsForbidden() {
        int bookingId = BookingApi.createBooking(Booking.aBooking());

        BookingApi.updateBooking(bookingId, "not-a-real-token", Booking.aBooking().withFirstname("Nope"))
                .then().statusCode(403);
    }

    @DisplayName("TC-RESTPUB-008: deleting without a valid token is forbidden")
    @Description("DELETE with an invalid token returns 403.")
    @Test
    void deletingWithoutAValidTokenIsForbidden() {
        int bookingId = BookingApi.createBooking(Booking.aBooking());

        BookingApi.deleteBooking(bookingId, "not-a-real-token").then().statusCode(403);
    }
}
