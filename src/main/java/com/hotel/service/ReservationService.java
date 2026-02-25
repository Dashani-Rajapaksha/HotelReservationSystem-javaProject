package com.hotel.service;
import java.sql.Connection;
import com.hotel.dao.*;
import com.hotel.model.*;
import com.hotel.database.DatabaseManager;

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

    Connection conn = null;

    try {
        conn = DatabaseManager.getInstance().getConnection();

        conn.setAutoCommit(false); // 🔥 START TRANSACTION

        // 1️⃣ Guest
        Guest guest = new Guest(name, address, contact, nic);
        int guestId = guestDAO.saveOrGetGuest(conn, guest);
        if (guestId == -1) throw new Exception("Guest failed");

        // 2️⃣ Availability
        int roomId = roomDAO.findAvailableRoom(conn, typeId, checkIn, checkOut);
        if (roomId == -1) throw new Exception("No rooms available");

        // 3️⃣ Reservation
        Reservation reservation = new Reservation(guestId, roomId, checkIn, checkOut);
        int reservationId = reservationDAO.saveReservation(conn, reservation);
        if (reservationId == -1) throw new Exception("Reservation failed");

        // 4️⃣ Billing
        double total = billService.calculateTotal(conn, reservationId);
        Bill bill = new Bill(reservationId, total);
        int billId = billDAO.saveBill(conn, bill);
        if (billId == -1) throw new Exception("Bill failed");

        conn.commit(); // ✅ SUCCESS

        System.out.println("Booking successful!");
        System.out.println("Reservation ID: " + reservationId);
        System.out.println("Total Amount: " + total);

        return true;

    } catch (Exception e) {

        try {
            if (conn != null) {
                conn.rollback(); // ❌ FAILURE → ROLLBACK
            }
        } catch (Exception rollbackEx) {
            rollbackEx.printStackTrace();
        }

        System.out.println("Booking failed. Transaction rolled back.");
        e.printStackTrace();
        return false;

    } finally {

        try {
            if (conn != null) {
                conn.setAutoCommit(true); // reset
            }
        } catch (Exception ignored) {}
    }
}
}
