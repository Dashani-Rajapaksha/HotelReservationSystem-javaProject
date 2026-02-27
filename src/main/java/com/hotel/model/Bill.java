package com.hotel.model;

public class Bill {

    private int billId;
    private int reservationId;
    private double totalAmount;

    //Empty Constructor (Required for DAO mapping)
    public Bill() {
    }

    //Constructor used when saving new bill
    public Bill(int reservationId, double totalAmount) {
        this.reservationId = reservationId;
        this.totalAmount = totalAmount;
    }

    // ==========================
    // GETTERS
    // ==========================

    public int getBillId() {
        return billId;
    }

    public int getReservationId() {
        return reservationId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    // ==========================
    // SETTERS
    // ==========================

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}