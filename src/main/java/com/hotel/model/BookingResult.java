package com.hotel.model;

public class BookingResult {

    private boolean success;
    private int reservationId;
    private int roomId;
    private double totalAmount;

    public BookingResult(boolean success, int reservationId, int roomId, double totalAmount) {
        this.success = success;
        this.reservationId = reservationId;
        this.roomId = roomId;
        this.totalAmount = totalAmount;
    }

    public boolean isSuccess() { return success; }
    public int getReservationId() { return reservationId; }
    public int getRoomId() { return roomId; }
    public double getTotalAmount() { return totalAmount; }
}