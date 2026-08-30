package com.vgekhtman.automation.sut.inventory;

/** Outcome of an attempted stock reservation, decoupled from gRPC types. */
public record ReservationResult(Status status, int remainingQuantity) {

    public enum Status { SUCCESS, INSUFFICIENT, NOT_FOUND }

    public static ReservationResult success(int remaining) {
        return new ReservationResult(Status.SUCCESS, remaining);
    }

    public static ReservationResult insufficient(int remaining) {
        return new ReservationResult(Status.INSUFFICIENT, remaining);
    }

    public static ReservationResult notFound() {
        return new ReservationResult(Status.NOT_FOUND, 0);
    }
}
