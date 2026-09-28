package com.vgekhtman.automation.tests.restpublic;

import com.vgekhtman.automation.tests.restpublic.client.Booking;
import com.vgekhtman.automation.tests.restpublic.client.BookingApi;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.instanceOf;

/**
 * Response shape validation via explicit field/type assertions.
 *
 * <p>restful-booker has no published OpenAPI/JSON-Schema spec to validate against (its /apidoc page is
 * prose and examples, not a machine-readable contract), so there was nothing to generate a schema from.
 * The field names and types asserted below were derived by inspecting real responses directly - curl
 * against {@code POST /auth}, {@code POST /booking}, and {@code GET /booking/{id}} - not from any
 * documentation:
 * <ul>
 *   <li>{@code firstname}, {@code lastname}, {@code additionalneeds}, {@code bookingdates.checkin/checkout}
 *       - JSON strings (dates are plain {@code "yyyy-MM-dd"} strings on the wire; not parsed/validated as
 *       dates here, just asserted as strings)</li>
 *   <li>{@code totalprice} - a JSON number with no decimal places observed in any response - asserted as
 *       {@code Integer}</li>
 *   <li>{@code depositpaid} - JSON boolean</li>
 * </ul>
 * {@code additionalneeds} is asserted as present because every fixture this module sends includes it -
 * the live API treats it as optional in general, which these assertions don't claim to prove either way.
 */
@Tag("public-api")
class BookingShapeTest {

    @Test
    void createBookingResponseHasExpectedShape() {
        BookingApi.createBookingResponse(Booking.aBooking()).then()
                .statusCode(200)
                .body("bookingid", instanceOf(Integer.class))
                .body("booking.firstname", instanceOf(String.class))
                .body("booking.lastname", instanceOf(String.class))
                .body("booking.totalprice", instanceOf(Integer.class))
                .body("booking.depositpaid", instanceOf(Boolean.class))
                .body("booking.additionalneeds", instanceOf(String.class))
                .body("booking.bookingdates.checkin", instanceOf(String.class))
                .body("booking.bookingdates.checkout", instanceOf(String.class));
    }

    @Test
    void getBookingResponseHasExpectedShape() {
        int bookingId = BookingApi.createBooking(Booking.aBooking());

        BookingApi.getBooking(bookingId).then()
                .statusCode(200)
                .body("firstname", instanceOf(String.class))
                .body("lastname", instanceOf(String.class))
                .body("totalprice", instanceOf(Integer.class))
                .body("depositpaid", instanceOf(Boolean.class))
                .body("additionalneeds", instanceOf(String.class))
                .body("bookingdates.checkin", instanceOf(String.class))
                .body("bookingdates.checkout", instanceOf(String.class));
    }
}
