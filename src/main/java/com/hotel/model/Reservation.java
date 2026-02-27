package com.hotel.model;

public class Reservation {

    private int reservationId;
    private int guestId;
    private int roomId;
    private String checkIn;
    private String checkOut;

    // Empty Constructor (Required for DAO mapping)
    public Reservation() {
    }

    // Constructor used when creating new reservation
    public Reservation(int guestId, int roomId, String checkIn, String checkOut) {
        this.guestId = guestId;
        this.roomId = roomId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    // ==========================
    // GETTERS
    // ==========================

    public int getReservationId() {
        return reservationId;
    }

    public int getGuestId() {
        return guestId;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getCheckIn() {
        return checkIn;
    }

    public String getCheckOut() {
        return checkOut;
    }

    // ==========================
    // SETTERS
    // ==========================

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public void setCheckIn(String checkIn) {
        this.checkIn = checkIn;
    }

    public void setCheckOut(String checkOut) {
        this.checkOut = checkOut;
    }
}