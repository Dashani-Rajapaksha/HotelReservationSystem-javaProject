package com.hotel.model;

public class Bill {

    private int reservationId;
    private double totalAmount;

    public Bill(int reservationId, double totalAmount) {
        this.reservationId = reservationId;
        this.totalAmount = totalAmount;
    }

    public int getReservationId() {
        return reservationId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}