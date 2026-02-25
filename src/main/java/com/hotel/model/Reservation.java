package com.hotel.model;

public class Reservation {

    private int reservationId;
    private int guestId;
    private int roomId;
    private String checkIn;
    private String checkOut;

    // Empty Constructor (REQUIRED)
    public Reservation() {
    }

    // Existing constructor for booking
    public Reservation(int guestId, int roomId, String checkIn, String checkOut) {
        this.guestId = guestId;
        this.roomId = roomId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    // Getters and Setters

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public int getGuestId() {
        return guestId;
    }

    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(String checkIn) {
        this.checkIn = checkIn;
    }

    public String getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(String checkOut) {
        this.checkOut = checkOut;
    }
}