package com.vgekhtman.automation.tests.restpublic.client;

public record Booking(
        String firstname,
        String lastname,
        int totalprice,
        boolean depositpaid,
        BookingDates bookingdates,
        String additionalneeds
) {

    public static Booking aBooking() {
        return new Booking("Jim", "Brown", 111, true,
                new BookingDates("2026-01-01", "2026-01-05"), "Breakfast");
    }

    public Booking withFirstname(String firstname) {
        return new Booking(firstname, lastname, totalprice, depositpaid, bookingdates, additionalneeds);
    }
}
