package com.hotel.service;

import com.hotel.dao.*;
import com.hotel.model.*;

public class ReservationService {

    private GuestDAO guestDAO = new GuestDAO();
    private RoomDAO roomDAO = new RoomDAO();
    private ReservationDAO reservationDAO = new ReservationDAO();
    private BillDAO billDAO = new BillDAO();
    private BillService billService = new BillService();
    
    private boolean validateInput(
        String name,
        String contact,
        String nic,
        String checkIn,
        String checkOut
) {

    if (name == null || name.isBlank()) {
        System.out.println("Name cannot be empty.");
        return false;
    }

    if (!contact.matches("\\d{10}")) {
        System.out.println("Contact must be 10 digits.");
        return false;
    }

    if (!nic.matches("\\d{12}")) {
        System.out.println("NIC must be 12 digits.");
        return false;
    }

    try {
        java.time.LocalDate inDate = java.time.LocalDate.parse(checkIn);
        java.time.LocalDate outDate = java.time.LocalDate.parse(checkOut);

        if (!outDate.isAfter(inDate)) {
            System.out.println("Check-out must be after check-in.");
            return false;
        }

        if (inDate.isBefore(java.time.LocalDate.now())) {
            System.out.println("Cannot book past dates.");
            return false;
        }

    } catch (Exception e) {
        System.out.println("Invalid date format. Use YYYY-MM-DD.");
        return false;
    }

    return true;
}

    public boolean bookRoom(
            String name,
            String address,
            String contact,
            String nic,
            int typeId,
            String checkIn,
            String checkOut
    ) {
        if (!validateInput(name, contact, nic, checkIn, checkOut)) {
    return false;
}

        // 1️⃣ Save or get guest
        Guest guest = new Guest(name, address, contact, nic);
        int guestId = guestDAO.saveOrGetGuest(guest);

        if (guestId == -1) {
            System.out.println("Guest processing failed.");
            return false;
        }

        // 2️⃣ Check room availability
        int roomId = roomDAO.findAvailableRoom(typeId, checkIn, checkOut);

        if (roomId == -1) {
            System.out.println("No rooms available.");
            return false;
        }

        // 3️⃣ Save reservation
        Reservation reservation = new Reservation(
                guestId,
                roomId,
                checkIn,
                checkOut
        );

        int reservationId = reservationDAO.saveReservation(reservation);

        if (reservationId == -1) {
            System.out.println("Reservation failed.");
            return false;
        }

        // 4️⃣ Calculate bill
        double total = billService.calculateTotal(reservationId);

        Bill bill = new Bill(reservationId, total);

        int billId = billDAO.saveBill(bill);

        if (billId == -1) {
            System.out.println("Bill generation failed.");
            return false;
        }

        System.out.println("Booking successful!");
        System.out.println("Reservation ID: " + reservationId);
        System.out.println("Total Amount: " + total);

        return true;
    }
}
