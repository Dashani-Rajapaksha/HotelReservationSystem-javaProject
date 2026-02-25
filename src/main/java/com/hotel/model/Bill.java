package com.hotel.model;

public class Bill {

    private int billId;
    private int reservationId;
    private double totalAmount;

    //Empty Constructor (REQUIRED)
    public Bill() {
    }

    // Existing constructor for saving bill
    public Bill(int reservationId, double totalAmount) {
        this.reservationId = reservationId;
        this.totalAmount = totalAmount;
    }

    // Getters and Setters

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}