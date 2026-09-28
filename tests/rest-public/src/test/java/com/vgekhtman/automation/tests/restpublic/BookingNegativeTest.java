package com.vgekhtman.automation.tests.restpublic;

import com.vgekhtman.automation.tests.restpublic.client.Booking;
import com.vgekhtman.automation.tests.restpublic.client.BookingApi;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Negative paths: a booking id that never existed, one this test deleted itself, and the two
 * auth-required endpoints called without a valid token.
 */
@Tag("public-api")
class BookingNegativeTest {

    @Test
    void gettingAnIdThatNeverExistedReturnsNotFound() {
        BookingApi.getBooking(999_999_999).then().statusCode(404);
    }

    @Test
    void gettingABookingThisTestJustDeletedReturnsNotFound() {
        int bookingId = BookingApi.createBooking(Booking.aBooking());
        String token = BookingApi.authenticate();

        BookingApi.deleteBooking(bookingId, token).then().statusCode(201);

        BookingApi.getBooking(bookingId).then().statusCode(404);
    }

    @Test
    void updatingWithoutAValidTokenIsForbidden() {
        int bookingId = BookingApi.createBooking(Booking.aBooking());

        BookingApi.updateBooking(bookingId, "not-a-real-token", Booking.aBooking().withFirstname("Nope"))
                .then().statusCode(403);
    }

    @Test
    void deletingWithoutAValidTokenIsForbidden() {
        int bookingId = BookingApi.createBooking(Booking.aBooking());

        BookingApi.deleteBooking(bookingId, "not-a-real-token").then().statusCode(403);
    }
}
