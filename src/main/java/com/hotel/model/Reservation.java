package com.hotel.model;

public class Reservation {

    private int reservationId;
    private int guestId;
    private int roomId;
    private String checkIn;
    private String checkOut;

    public Reservation(int guestId, int roomId, String checkIn, String checkOut) {
        this.guestId = guestId;
        this.roomId = roomId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
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
}